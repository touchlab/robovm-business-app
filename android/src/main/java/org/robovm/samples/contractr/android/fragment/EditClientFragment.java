package org.robovm.samples.contractr.android.fragment;

import android.app.Fragment;
import org.robovm.samples.contractr.core.common.SQLiteException;

import java.sql.SQLException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditClientFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditClientFragment extends AbstractClientFragment {

    public static EditClientFragment newInstance() {
        return new EditClientFragment();
    }

    public EditClientFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Edit client";
    }

    @Override
    protected void onSave() {
        appManager.getDatabaseHelper().saveClient(saveViewValuesToClient(client));
        super.onSave();
    }
}
