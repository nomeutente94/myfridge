package com.example.robertotarullo.myfridge.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.DateSpinnerAdapter;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SpinnerDatePickerFragment extends DialogFragment{

    private Spinner daySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private EditText dateField;

    private int day;
    private int month;
    private int year;

    private String dayAsString;
    private String monthAsString;
    private String yearAsString;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.spinner_date_picker, null);

        dateField = getActivity().findViewById(getArguments().getInt("id"));

        daySpinner = view.findViewById(R.id.expiryDateDaySpinner);
        monthSpinner = view.findViewById(R.id.expiryDateMonthSpinner);
        yearSpinner = view.findViewById(R.id.expiryDateYearSpinner);

        initializeExpiryDateSpinner(); // Popola i campi
        DateUtils.setDate(daySpinner, monthSpinner, yearSpinner, TextUtils.getDate(dateField)); // Setta alla data presente nel form

        builder.setView(view)
            .setTitle("Seleziona data")
            .setPositiveButton("Ok", (dialog, id) -> {})
            .setNegativeButton("Annulla", (dialog, id) -> SpinnerDatePickerFragment.this.getDialog().cancel());
        return builder.create();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(getDialog() != null){
            Button positiveButton = ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                day = daySpinner.getSelectedItemPosition();
                month = monthSpinner.getSelectedItemPosition();
                year = yearSpinner.getSelectedItemPosition();

                dayAsString = daySpinner.getSelectedItem().toString();
                monthAsString = monthSpinner.getSelectedItem().toString();
                yearAsString = yearSpinner.getSelectedItem().toString();

                Date formDate = getDateFromForm();

                if(formDate!=null){
                    dateField.setText(DateUtils.getFormattedDate(formDate));
                    dismiss();
                } else
                    Toast.makeText(this.getActivity(), "La data immessa non è valida", Toast.LENGTH_LONG).show();
            });
        }
    }

    private Date getDateFromForm(){
        Date currentDate = null;
        Calendar maxDate = DateUtils.getCalendar(DateUtils.getMaxDateAllowed(dateField, getActivity()));
        Calendar minDate = DateUtils.getCalendar(DateUtils.getMinDateAllowed(dateField, getActivity()));

        if(dateField==getActivity().findViewById(R.id.packagingDateField))
            currentDate = getPackagingDate();
        else if(dateField==getActivity().findViewById(R.id.expiryDateField))
            currentDate = getExpiryDate();

        if(currentDate!=null && minDate.getTime().before(currentDate) && maxDate.getTime().after(currentDate))
            return currentDate;

        return null;
    }

    // Validazione per data di produzione
    private Date getPackagingDate(){
        if(dateField==getActivity().findViewById(R.id.packagingDateField)){
            if(
                (day>0 && month>0 && year>0) && // Se tutti i campi sono compilati
                (DateUtils.isDateValid(dayAsString, monthAsString, yearAsString)) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return DateUtils.getDate(daySpinner, monthSpinner, yearSpinner);
            }
        }
        return null;
    }

    // Validazione per data di scadenza
    private Date getExpiryDate(){
        if(dateField==getActivity().findViewById(R.id.expiryDateField)){
            if(
                (day==0 && month==0 && year==0) || // Se non è stato compilato nessun campo
                (day>0 && month==0 && year==0) || // Se è stato compilato solo il giorno di scadenza
                (day==0 && month>0 && year==0) || // Se è stato compilato solo il mese di scadenza
                ((day>0 && month>0 && year==0) && (!DateUtils.isDateValid(dayAsString, monthAsString, "2019"))) || // Se è stato compilato 29/02 ma l'anno corrente non è bisestile // TODO settare anno corrente
                ((day>0 && month>0 && year>0) && (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString))) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return DateUtils.getExpiryDate(daySpinner, monthSpinner, yearSpinner);
            }
        }
        return null;
    }

    private void initializeExpiryDateSpinner() {
        List<String> days = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> years = new ArrayList<>();

        days.add("DD");
        months.add("MM");
        years.add("YYYY");

        // Inizializza anni
        for(int i=DateUtils.MIN_YEAR; i<=DateUtils.MAX_YEAR; i++)
            years.add(String.valueOf(i));

        // Inizializza mesi
        for(int i = DateUtils.MIN_MONTH; i<= DateUtils.MAX_MONTH; i++) {
            if (String.valueOf(i).length() == 1)
                months.add("0" + i);
            else
                months.add(String.valueOf(i));
        }

        // Inizializza giorni
        for(int i = DateUtils.MIN_DAY; i<= DateUtils.MAX_DAY; i++){
            if(String.valueOf(i).length()==1)
                days.add("0" + i);
            else
                days.add(String.valueOf(i));
        }

        daySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days));
        monthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months));
        yearSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years));
    }
}
