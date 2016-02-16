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

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSIndexPath;
import org.robovm.apple.uikit.NSIndexPathExtensions;
import org.robovm.apple.uikit.UITableView;
import org.robovm.apple.uikit.UITableViewCell;
import org.robovm.apple.uikit.UITableViewCellAccessoryType;
import org.robovm.apple.uikit.UITableViewCellStyle;
import org.robovm.apple.uikit.UITableViewRowAnimation;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.samples.contractr.core.service.AppManager;
import org.robovm.samples.contractr.core.service.Client;

import javax.inject.Inject;

/**
 * 
 */
@CustomClass("ClientsViewController")
public class ClientsViewController extends ListViewController {

    @Inject
    AppManager appManager;

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();

        getNavigationItem().setTitle("Clients");
    }

    @Override
    protected void onAdd() {
        appManager.getDatabaseHelper().selectClient(null);
        performSegue("editClientSegue", this);
    }

    @Override
    protected void onEdit(int section, int row) {
        appManager.getDatabaseHelper().selectClient(appManager.getDatabaseHelper().getClientAt(row));
        performSegue("editClientSegue", this);
    }

    @Override
    protected void onDelete(int section, int row) {
        Client client = appManager.getDatabaseHelper().getClientAt(row);
        appManager.getDatabaseHelper().delete(client);
        getTableView().deleteRows(
                new NSArray<>(NSIndexPathExtensions.createIndexPathForRowInSection(row, section)),
                UITableViewRowAnimation.Automatic);
    }

    @Override
    public UITableViewCell getCellForRow(UITableView tableView, NSIndexPath indexPath) {
        UITableViewCell cell = tableView.dequeueReusableCell("cell");
        if (cell == null) {
            cell = new UITableViewCell(UITableViewCellStyle.Value1, "cell");
            cell.setAccessoryType(UITableViewCellAccessoryType.DisclosureIndicator);
        }
        Client client = appManager.getDatabaseHelper().getClientAt(indexPath.getRow());
        cell.getTextLabel().setText(client.name);
        return cell;
    }

    @Override
    public long getNumberOfRowsInSection(UITableView tableView, long section) {
        return appManager.getDatabaseHelper().getClientCount();
    }
}
