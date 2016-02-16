/*
 * Copyright (C) 2014 RoboVM AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.samples.contractr.ios.viewcontrollers;

import net.engio.mbassy.listener.Handler;

import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.uikit.UIButton;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIControlState;
import org.robovm.apple.uikit.UILabel;
import org.robovm.apple.uikit.UIView;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.objc.annotation.IBAction;
import org.robovm.objc.annotation.IBOutlet;
import org.robovm.samples.contractr.core.service.AppManager;
import org.robovm.samples.contractr.core.service.DatabaseHelper;
import org.robovm.samples.contractr.core.service.Task;
import org.robovm.samples.contractr.ios.IOSColors;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

@CustomClass("WorkViewController")
public class WorkViewController extends InjectedViewController {

    @Inject
           AppManager appManager;

    @IBOutlet UIButton startStopButton;
    @IBOutlet UILabel currentClientLabel;
    @IBOutlet UIView currentClientColorView;
    @IBOutlet UILabel currentTaskLabel;
    @IBOutlet UILabel earnedLabel;
    @IBOutlet UILabel timerLabel;
    private boolean showing = true;

    @Handler
    public void workStarted(DatabaseHelper.WorkStartedEvent event) {
        DispatchQueue.getMainQueue().async(this::updateUIComponents);
    }

    @Handler
    public void workStopped(DatabaseHelper.WorkStoppedEvent event) {
        DispatchQueue.getMainQueue().async(() -> {
            updateUIComponents();
            tick(); // Resets timer to 00:00:00
        });
    }

    @Override
    public void viewWillAppear(boolean animated) {
        super.viewWillAppear(animated);
        appManager.getDatabaseHelper().subscribeTask(this);
        showing = true;
        updateUIComponents();
        tick();
    }

    @Override
    public void viewWillDisappear(boolean animated) {
        appManager.getDatabaseHelper().unsubscribeTask(this);
        showing = false;
        super.viewWillDisappear(animated);
    }

    @IBAction void startStopClicked() {
        Task workingTask = appManager.getDatabaseHelper().getWorkingTask();
        if (workingTask == null) {
            performSegue("selectTaskSegue", this);
        } else {
            appManager.getDatabaseHelper().stopWork();
        }
    }

    private void updateUIComponents() {
        Task task = appManager.getDatabaseHelper().getWorkingTask();
        UIColor startStopColor = null;
        String startStopTitle = null;
        String currentTaskText = null;
        if (task == null) {
            startStopTitle = "Start work";
            startStopColor = IOSColors.START_WORK;
            currentTaskText = "None";
            currentClientLabel.setHidden(true);
            currentClientColorView.setHidden(true);
        } else {
            startStopTitle = "Stop work";
            startStopColor = IOSColors.STOP_WORK;
            currentTaskText = task.title;
            currentClientLabel.setText(task.client.name);
            currentClientLabel.setHidden(false);
            //TODO: client index needed
            currentClientColorView.setBackgroundColor(
                    IOSColors.getClientColor(0));
            currentClientColorView.setHidden(false);
        }
        startStopButton.setTitle(startStopTitle, UIControlState.Normal);
        startStopButton.setBackgroundColor(startStopColor);
        currentTaskLabel.setText(currentTaskText);
    }

    private void tick() {
        if (!showing) {
            return;
        }
        Task task = appManager.getDatabaseHelper().getWorkingTask();
        if (task != null) {
            timerLabel.setText(task.getTimeElapsed());
            earnedLabel.setText(task.getAmountEarned(Locale.US));
            DispatchQueue.getMainQueue().after(1, TimeUnit.SECONDS, this::tick);
        } else {
            timerLabel.setText("00:00:00");
            earnedLabel.setText(NumberFormat.getCurrencyInstance(Locale.US).format(0));
        }
    }
}
