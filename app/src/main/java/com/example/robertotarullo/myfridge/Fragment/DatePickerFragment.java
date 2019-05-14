package com.example.robertotarullo.myfridge.Fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.R;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private String tag;                                                     // Nome del campo data
    private TextView dateField;                                             // Data attuale
    private TextView expiryDateField, purchaseDateField, openingDateField;  // Campi data

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;

        // Nome del campo in uso
        tag = getArguments().getString("tag");

        // inizializza campi data
        //expiryDateField = getActivity().findViewById(R.id.expiryDateField);
        purchaseDateField = getActivity().findViewById(R.id.purchaseDateField);
        openingDateField = getActivity().findViewById(R.id.openingDateField);
        //if(tag.equalsIgnoreCase("expiryDate"))
          //  dateField = getActivity().findViewById(R.id.expiryDateField);
        if(tag.equalsIgnoreCase("purchaseDate"))
            dateField = getActivity().findViewById(R.id.purchaseDateField);
        else if(tag.equalsIgnoreCase("openingDate"))
            dateField = getActivity().findViewById(R.id.openingDateField);

        // inizializza giorno, mese ed anno
        if(DateUtils.isDateEmpty(dateField)){  // STRINGS.XML
            // Create a new instance of DatePickerDialog with current date
            Calendar c = Calendar.getInstance();
            month = c.get(Calendar.MONTH);
            year = c.get(Calendar.YEAR);
            day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            year = DateUtils.getYear(dateField);
            month = DateUtils.getMonth(dateField)-1;
            day = DateUtils.getDay(dateField);
        }

        DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);

        if(tag.equalsIgnoreCase("purchaseDate")) {          // purchaseDate <= openingDate && purchaseDate <= now
            Calendar max = Calendar.getInstance();

            if(!DateUtils.isDateEmpty(openingDateField)){
                Calendar openingDate = DateUtils.getDate(openingDateField);
                if(openingDate.before(max))
                    max = openingDate;
            }

            dpd.getDatePicker().setMaxDate(max.getTimeInMillis());
        } else if(tag.equalsIgnoreCase("openingDate")) {    // openingDate <= now && openingDate >= purchaseDate
            Calendar max = Calendar.getInstance();

            if (!DateUtils.isDateEmpty(purchaseDateField)) {
                Calendar min = DateUtils.getDate(purchaseDateField);
                dpd.getDatePicker().setMinDate(min.getTimeInMillis());
            }

            dpd.getDatePicker().setMaxDate(max.getTimeInMillis());
        }

        return dpd;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        dateField.setText(DateUtils.getFormattedDate(c));
    }
}
