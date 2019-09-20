package com.example.robertotarullo.myfridge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class StorageSpinnerArrayAdapter extends ArrayAdapter<String> {

    public StorageSpinnerArrayAdapter(Context context, int resourceId, List<String> storageConditions) {
        super(context, resourceId, storageConditions);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        ((TextView) view).setTextColor(Color.BLACK);

        return view;
    }
}

