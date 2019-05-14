package com.example.robertotarullo.myfridge.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Bean.PointOfPurchase;

import java.util.List;

public class DateSpinnerAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private List<String> entries;

    public DateSpinnerAdapter(Context context, int resourceId, List<String> entries) {
        super(context, resourceId, entries);
        this.inflater = LayoutInflater.from(context);
        this.entries = entries;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
    		
        //PointOfPurchase p = getItem(position);

        TextView tv = (TextView) v;

        if (position == 0) {
            tv.setTextColor(Color.GRAY);
        } else
            tv.setTextColor(Color.BLACK);
        return v;
    }
}

