package com.example.robertotarullo.myfridge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.bean.Filter;

import java.util.List;

public class StorageSpinnerArrayAdapter extends ArrayAdapter<Filter> {

    public StorageSpinnerArrayAdapter(Context context, int resourceId, List<Filter> storageConditions) {
        super(context, resourceId, storageConditions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        Filter f = getItem(position);

        //((TextView) view).setTextColor(Color.BLACK);
        ((TextView) view).setText(f.getName());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);

        Filter f = getItem(position);

        ((TextView) view).setTextColor(Color.BLACK);
        ((TextView) view).setText(f.getName());

        return view;
    }
}

