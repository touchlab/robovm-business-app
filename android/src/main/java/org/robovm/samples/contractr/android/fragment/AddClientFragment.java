package org.robovm.samples.contractr.android.fragment;

import android.app.Fragment;
import org.robovm.samples.contractr.core.service.Client;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddClientFragment extends AbstractClientFragment {

    public AddClientFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Add client";
    }

    public static AddClientFragment newInstance() {
        return new AddClientFragment();
    }

    @Override
    protected void onSave() {
        Client client = saveViewValuesToClient(new Client());
        appManager.getDatabaseHelper().saveClient(client);
        super.onSave();
    }
}
