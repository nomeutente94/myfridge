package com.example.robertotarullo.myfridge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class DateSpinnerAdapter extends ArrayAdapter<String> {

    private int spinnerType;
/*    private static final String COLOR_DISABLED = "#cecece";

    private EditText dateField;
    private List<String> entries;
    private Context context;
    private int currentYear, currentMonth;*/

    public DateSpinnerAdapter(Context context, int resourceId, List<String> entries, int spinnerType) {
        super(context, resourceId, entries);
        this.spinnerType = spinnerType;
/*        this.context = context;
        this.dateField = dateField;
        this.entries = entries;
        this.currentMonth = currentMonth;
        this.currentYear = currentYear;*/
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);

        TextView tv = (TextView) v;

        if (position == 0 && spinnerType!=DateUtils.YEAR_SPINNER)
            tv.setTextColor(Color.GRAY);
        /*else if(!DateUtils.isValueValid(spinnerType, position, dateField, context, currentMonth, currentYear, entries))
            tv.setTextColor(Color.parseColor(COLOR_DISABLED));*/
        else
            tv.setTextColor(Color.BLACK);

        return v;
    }

   /* @Override
    public boolean isEnabled(int position) {
        return DateUtils.isValueValid(spinnerType, position, dateField, context, currentMonth, currentYear, entries);
    }*/
}

