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

import org.robovm.apple.uikit.UITextField;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.objc.annotation.IBAction;
import org.robovm.objc.annotation.IBOutlet;
import org.robovm.samples.contractr.core.service.AppManager;
import org.robovm.samples.contractr.core.service.Client;
import org.robovm.samples.contractr.ios.views.CurrencyTextField;

import java.math.BigDecimal;

import javax.inject.Inject;

/**
 * 
 */
@CustomClass("EditClientViewController")
public class EditClientViewController extends InjectedTableViewController {
    @Inject
    AppManager appManager;

    @IBOutlet
    UITextField nameTextField;
    @IBOutlet
    CurrencyTextField hourlyRateTextField;

    @Override
    public void viewWillAppear(boolean animated) {
        super.viewWillAppear(animated);

        Client client = appManager.getDatabaseHelper().getSelectedClient();
        if (client == null) {
            getNavigationItem().setTitle("Add client");
            updateViewValuesWithClient(null);
        } else {
            getNavigationItem().setTitle("Edit client");
            updateViewValuesWithClient(client);
        }
    }

    @IBAction
    private void save() {
        Client client = appManager.getDatabaseHelper().getSelectedClient();
        if (client == null) {
            appManager.getDatabaseHelper().saveClient(saveViewValuesToClient(new Client()));
        } else {
            appManager.getDatabaseHelper().saveClient(saveViewValuesToClient(client));
        }
        getNavigationController().popViewController(true);
    }

    @IBAction
    private void nameChanged() {
        String name = nameTextField.getText();
        name = name == null ? "" : name.trim();
        getNavigationItem().getRightBarButtonItem().setEnabled(!name.isEmpty());
    }

    @Override
    public void viewWillDisappear(boolean animated) {
        nameTextField.resignFirstResponder();
        hourlyRateTextField.resignFirstResponder();

        super.viewWillDisappear(animated);
    }

    private void updateViewValuesWithClient(Client client) {
        nameTextField.setText(client == null ? "" : client.name);
        hourlyRateTextField.setAmount(client == null ? BigDecimal.ZERO : client.hourlyRate);
        nameChanged();
    }

    private Client saveViewValuesToClient(Client client) {
        String name = nameTextField.getText();
        name = name == null ? "" : name.trim();

        client.name = (name);
        client.hourlyRate = (hourlyRateTextField.getAmount());

        return client;
    }
}
