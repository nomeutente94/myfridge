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

    private TextView dateField;                                             // Data attuale
    private TextView expiryDateField, purchaseDateField, openingDateField;  // Campi data

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;

        // inizializza campi data
        //expiryDateField = getActivity().findViewById(R.id.expiryDateField);
        purchaseDateField = getActivity().findViewById(R.id.purchaseDateField);
        openingDateField = getActivity().findViewById(R.id.openingDateField);

        dateField = getActivity().findViewById(getArguments().getInt("id"));

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

        if(dateField == purchaseDateField) {          // purchaseDate <= openingDate && purchaseDate <= now
            Calendar max = Calendar.getInstance();

            if(!DateUtils.isDateEmpty(openingDateField)){
                Calendar openingDate = DateUtils.getDate(openingDateField);
                if(openingDate.before(max))
                    max = openingDate;
            }

            dpd.getDatePicker().setMaxDate(max.getTimeInMillis());
        } else if(dateField == openingDateField) {    // openingDate <= now && openingDate >= purchaseDate
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
