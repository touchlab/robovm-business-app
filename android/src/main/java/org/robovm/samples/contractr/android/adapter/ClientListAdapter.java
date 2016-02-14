package org.robovm.samples.contractr.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.robovm.samples.contractr.core.common.SQLiteException;
import org.robovm.samples.contractr.core.service.AppManager;
import org.robovm.samples.contractr.core.service.Client;

import java.sql.SQLException;

public class ClientListAdapter extends BaseAdapter {
    AppManager appManager;
    LayoutInflater inflater;

    public ClientListAdapter(AppManager appManager, LayoutInflater inflater) {
        this.appManager = appManager;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return appManager.getDatabaseHelper().getClientCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Client c = appManager.getDatabaseHelper().getClientAt(position);
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(c.name);

        return view;

    }

    @Override
    public Object getItem(int position) {
        return appManager.getDatabaseHelper().getClientAt(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }
}
