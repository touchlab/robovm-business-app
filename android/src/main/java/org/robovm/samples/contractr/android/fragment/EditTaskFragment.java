package org.robovm.samples.contractr.android.fragment;

import org.robovm.samples.contractr.core.common.SQLiteException;

import java.sql.SQLException;

public class EditTaskFragment extends AbstractTaskFragment {

    public static EditTaskFragment newInstance() {
        return new EditTaskFragment();
    }

    public EditTaskFragment() {}

    @Override
    protected String getTitle() {
        return "Edit task";
    }

    @Override
    public void onResume() {
        updateViewValuesWithTask(task);
        super.onResume();
    }

    @Override
    protected void onSave() {
        appManager.getDatabaseHelper().saveTask(saveViewValuesToTask(task));
        super.onSave();
    }
}
