package org.robovm.samples.contractr.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import org.robovm.samples.contractr.android.R;
import org.robovm.samples.contractr.android.adapter.ClientListAdapter;
import org.robovm.samples.contractr.core.common.SQLiteException;
import org.robovm.samples.contractr.core.service.AppManager;
import org.robovm.samples.contractr.core.service.Client;
import org.robovm.samples.contractr.core.service.Task;
import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public abstract class AbstractTaskFragment extends RoboDialogFragment implements AdapterView.OnItemSelectedListener {
    @Inject
    LayoutInflater inflater;
    @InjectView(R.id.clientPicker)
    protected Spinner clientPicker;
    @InjectView(R.id.title)
    protected EditText titleTextField;
    @InjectView(R.id.notes)
    protected EditText notesTextField;
    @InjectView(R.id.finished)
    protected ToggleButton finishedToggle;
    @InjectView(R.id.action_ok)
    Button okButton;
    @InjectView(R.id.action_cancel)
    Button cancelButton;

    @Inject
    AppManager appManager;

    protected Task task;
    protected Client client;
    protected SpinnerAdapter mAdapter;

    NumberFormat formatter = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    TaskFragmentListener taskFragmentListener;

    public AbstractTaskFragment() {}

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewValuesWithTask(task);
    }

    protected abstract String getTitle();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(getTitle());
        return inflater.inflate(R.layout.fragment_edit_task, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new ClientListAdapter(appManager, inflater);
        clientPicker.setAdapter(mAdapter);
        clientPicker.setOnItemSelectedListener(this);
        okButton.setOnClickListener(v -> onSave());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            taskFragmentListener = (TaskFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TaskFragmentListener");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        client =  appManager.getDatabaseHelper().getClientAt(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        client = null;
    }

    protected void onSave() {
        dismiss();
        taskFragmentListener.taskSaved();
    }

    protected void updateSaveButtonEnabled() {
        String title = titleTextField.getText().toString();
        title = title == null ? "" : title.trim();
        boolean canSave = !title.isEmpty() && task != null;
        //        getNavigationItem().getRightBarButtonItem().setEnabled(canSave);
    }

    protected void updateViewValuesWithTask(Task task) {
        client = task == null ? null : task.client;
        int selectedRow = 0;
        if (client != null) {
            List<Client> clients = appManager.getDatabaseHelper().getAllClients();

            long clientCount = clients.size();
            for (int i = 0; i < clientCount; i++) {
                if (clients.get(i).equals(client)) {
                    selectedRow = i;
                    break;
                }
            }
        }
        clientPicker.setSelection(selectedRow);
        //clientTextField.setText(task == null ? "" : task.getClient().getName());
        titleTextField.setText(task == null ? "" : task.title);
        notesTextField.setText(task == null ? "" : task.notes);
        finishedToggle.setChecked(task != null && task.finished);
        updateSaveButtonEnabled();
    }

    protected Task saveViewValuesToTask(Task task) {
        String title = titleTextField.getText().toString();
        title = title == null ? "" : title.trim();
        String notes = notesTextField.getText().toString();
        notes = notes == null ? "" : notes.trim();

        Client client = appManager.getDatabaseHelper().getClientAt(clientPicker.getSelectedItemPosition());
        task.client = (client);
        task.title = (title);
        task.notes = (notes);
        task.finished = (finishedToggle.isChecked());

        return task;
    }

    public interface TaskFragmentListener {
        void taskSaved();
    }
}
