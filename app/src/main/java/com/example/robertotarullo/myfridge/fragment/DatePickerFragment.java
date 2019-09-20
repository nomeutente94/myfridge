package com.example.robertotarullo.myfridge.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.DateUtils;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private EditText dateField; // Data attuale

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;

        dateField = getActivity().findViewById(getArguments().getInt("dateFieldId"));

        // inizializza giorno, mese ed anno
        if(DateUtils.isDateEmpty(dateField)){
            // TODO controllare se la data è legale altrimenti scegli la più vicina (??)
            Calendar c = Calendar.getInstance();
            month = c.get(Calendar.MONTH);
            year = c.get(Calendar.YEAR);
            day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            year = DateUtils.getYear(dateField);
            month = DateUtils.getMonth(dateField)-1;
            day = DateUtils.getDay(dateField);
        }

        DatePickerDialog dpd;
        if(getArguments().getBoolean("spinnerMode"))
            dpd = new DatePickerDialog(getActivity(), R.style.MySpinnerDatePickerStyle, this, year, month, day);
        else
            dpd = new DatePickerDialog(getActivity(), this, year, month, day);

        dpd.getDatePicker().setMaxDate(DateUtils.getCalendar(DateUtils.getMaxDateAllowed(dateField, getActivity())).getTimeInMillis());
        dpd.getDatePicker().setMinDate(DateUtils.getCalendar(DateUtils.getMinDateAllowed(dateField, getActivity())).getTimeInMillis());

        return dpd;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        dateField.setText(DateUtils.getFormattedDate(c));
    }
}
