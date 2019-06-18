package com.example.robertotarullo.myfridge.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.Utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateSpinnerAdapter extends ArrayAdapter<String> {

    public static final int DAY_SPINNER = 0;
    public static final int MONTH_SPINNER = 1;
    public static final int YEAR_SPINNER = 2;

    private static final String COLOR_DISABLED = "#cecece";

    private int spinnerType;

    private Date maxDate, minDate;

    private List<String> entries;

    private int currentYear, currentMonth;

    public DateSpinnerAdapter(Context context, int resourceId, List<String> entries, int spinnerType, EditText dateField, int currentMonth, int currentYear) {
        super(context, resourceId, entries);

        this.spinnerType = spinnerType;
        this.entries = entries;
        this.currentMonth = currentMonth;
        this.currentYear = currentYear;

        maxDate = DateUtils.getMaxDateAllowed(dateField, (Activity)context);
        minDate = DateUtils.getMinDateAllowed(dateField, (Activity)context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);

        TextView tv = (TextView) v;

        if (position == 0)
            tv.setTextColor(Color.GRAY);
        else if(!isValueValid(position))
            tv.setTextColor(Color.parseColor(COLOR_DISABLED));
        else
            tv.setTextColor(Color.BLACK);

        return v;
    }

    @Override
    public boolean isEnabled(int position) {
        return isValueValid(position);
    }

    private boolean isValueValid(int position){
        int minDay = DateUtils.getCalendar(minDate).get(Calendar.DAY_OF_MONTH);
        int maxDay = DateUtils.getCalendar(maxDate).get(Calendar.DAY_OF_MONTH);
        int maxMonth = DateUtils.getCalendar(maxDate).get(Calendar.MONTH);
        int minMonth = DateUtils.getCalendar(minDate).get(Calendar.MONTH);
        int minYear = DateUtils.getCalendar(minDate).get(Calendar.YEAR);
        int maxYear = DateUtils.getCalendar(maxDate).get(Calendar.YEAR);

        if(spinnerType==DAY_SPINNER){
            if(position>0 && currentYear>-1 && currentMonth>-1){
                int value = Integer.valueOf(entries.get(position));
                if((value < minDay && minMonth==currentMonth && minYear==currentYear) || (value > maxDay && maxMonth==currentMonth && maxYear==currentYear))
                    return false;
            }
        } else if(spinnerType==MONTH_SPINNER){
            if(position>0 && currentYear>-1){
                int value = Integer.valueOf(entries.get(position));
                if((value < minMonth && minYear==currentYear) || (value > maxMonth && maxYear==currentYear))
                    return false;
            }
        } else if(spinnerType==YEAR_SPINNER){
            if(position>0){
                int value = Integer.valueOf(entries.get(position));
                if(value < minYear || value > maxYear)
                    return false;
            }
        }

        return true;
    }
}

