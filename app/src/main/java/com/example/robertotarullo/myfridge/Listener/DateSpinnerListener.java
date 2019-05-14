package com.example.robertotarullo.myfridge.Listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.robertotarullo.myfridge.Adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.Utils.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateSpinnerListener implements AdapterView.OnItemSelectedListener {
    Spinner daySpinner;
    Spinner monthSpinner;
    Spinner yearSpinner;
    View illegalExpiryDateTextView;

    public DateSpinnerListener(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner, View illegalExpiryDateTextView){
        this.daySpinner = daySpinner;
        this.monthSpinner = monthSpinner;
        this.yearSpinner = yearSpinner;
        this.illegalExpiryDateTextView =  illegalExpiryDateTextView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String date;
        String day, month, year;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        if(daySpinner.getSelectedItemPosition()>0 && monthSpinner.getSelectedItemPosition()>0 && yearSpinner.getSelectedItemPosition()>0) {
            date = daySpinner.getSelectedItem().toString() + "/" + monthSpinner.getSelectedItem().toString() + "/" + yearSpinner.getSelectedItem().toString();
            try {
                Date convertedDate = dateFormat.parse(date);
                if(!date.equals(dateFormat.format(convertedDate)))
                    illegalExpiryDateTextView.setVisibility(View.VISIBLE);
                else
                    illegalExpiryDateTextView.setVisibility(View.GONE);
            } catch (ParseException e) {
                System.out.println("Data non valida: " + date);
            }
        } else
            illegalExpiryDateTextView.setVisibility(View.GONE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
