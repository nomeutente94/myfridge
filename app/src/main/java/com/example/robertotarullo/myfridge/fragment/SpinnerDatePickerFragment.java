package com.example.robertotarullo.myfridge.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.robertotarullo.myfridge.adapter.DateSpinnerAdapter;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.utils.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SpinnerDatePickerFragment extends DialogFragment {

    //private boolean dayAutoSelection;

    private Spinner daySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private EditText dateField, expiryDateField, packagingDateField;

    private Calendar maxDate, minDate;

    private List<String> days;
    private List<String> months;
    private List<String> years;

    /*public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if(userSelect) {
                updateSpinners();
                userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    public class DaySpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            dayAutoSelection = false;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.spinner_date_picker, null);

        dateField = getActivity().findViewById(getArguments().getInt("dateFieldId"));

        expiryDateField = getActivity().findViewById(R.id.expiryDateField);
        packagingDateField = getActivity().findViewById(R.id.packagingDateField);

        daySpinner = view.findViewById(R.id.expiryDateDaySpinner);
        monthSpinner = view.findViewById(R.id.expiryDateMonthSpinner);
        yearSpinner = view.findViewById(R.id.expiryDateYearSpinner);

        maxDate = DateUtils.getMaxDateAllowedAsCalendar(dateField, getActivity());
        minDate = DateUtils.getMinDateAllowedAsCalendar(dateField, getActivity());

        // Setta il titolo
        String title = "Data";
        if(dateField==packagingDateField)
            title += " di produzione/lotto";
        else if(dateField==expiryDateField)
            title += " di scadenza";

        populateSpinners();
        DateUtils.setDate(daySpinner, monthSpinner, yearSpinner, TextUtils.getDate(dateField)); // Setta alla data presente nell'elemento del form

        /*// Aggiungi listener agli spinner giorno / mese / anno
        *//*SpinnerInteractionListener dayListener = new SpinnerInteractionListener();
        daySpinner.setOnTouchListener(dayListener);*//*
        daySpinner.setOnItemSelectedListener(new DaySpinnerInteractionListener());

        SpinnerInteractionListener monthListener = new SpinnerInteractionListener();
        monthSpinner.setOnTouchListener(monthListener);
        monthSpinner.setOnItemSelectedListener(monthListener);

        SpinnerInteractionListener yearListener = new SpinnerInteractionListener();
        yearSpinner.setOnTouchListener(yearListener);
        yearSpinner.setOnItemSelectedListener(yearListener);*/

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setView(view)
            .setTitle(title)
            .setPositiveButton("Ok", (dialog, id) -> {})
            .setNegativeButton("Annulla", (dialog, id) -> SpinnerDatePickerFragment.this.getDialog().cancel());
        return builder.create();
    }

    private void populateSpinners(){
        // Popola gli spinner
        Date currentDate = TextUtils.getDate(dateField);
        int minDay = DateUtils.MIN_DAY;
        int maxDay = DateUtils.MAX_DAY;
        int minMonth = DateUtils.MIN_MONTH;
        int maxMonth = DateUtils.MAX_MONTH;

        // restringe il range dei singoli spinner se possibile
        if(currentDate!=null) {
            // se l'anno corrente è uguale all'anno di maxDate -> imposta il limite max del mese al mese di maxDate
            if (maxDate.get(Calendar.YEAR) == getCurrentYear()) {
                maxMonth = maxDate.get(Calendar.MONTH)+1;
                // se la condizione sopra è vera e anche il mese corrente è uguale al mese di maxDate -> imposta il limite max del giorno al giorno di maxDate
                if (maxDate.get(Calendar.MONTH) == getCurrentMonth())
                    maxDay = maxDate.get(Calendar.DAY_OF_MONTH)+1;
            }

            // se l'anno corrente è uguale all'anno di minDate -> imposta il limite min del mese al mese di minDate
            if (minDate.get(Calendar.YEAR) == getCurrentYear()) {
                minMonth = minDate.get(Calendar.MONTH)+1;
                // se la condizione sopra è vera e anche il mese corrente è uguale al mese di minDate -> imposta il limite min del giorno al giorno di minDate
                if (minDate.get(Calendar.MONTH) == getCurrentMonth())
                    minDay = minDate.get(Calendar.DAY_OF_MONTH)+1;
            }
        }

        days = getDays(minDay, maxDay);
        months = getMonths(minMonth, maxMonth);
        years = getYears(minDate.get(Calendar.YEAR), maxDate.get(Calendar.YEAR));

        daySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days));
        monthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months));
        yearSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years));
    }

    @Override
    public void onStart(){
        super.onStart();
        if(getDialog() != null){
            // Setta il comportamento del pulsante di conferma
            ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Date formDate = null;

                if(dateField==packagingDateField)
                    formDate = getPackagingDate();
                else if(dateField==expiryDateField)
                    formDate = getExpiryDate();

                if(formDate!=null){
                    dateField.setText(DateUtils.getFormattedDate(formDate));
                    dismiss();
                } else
                    Toast.makeText(this.getActivity(), "La data immessa non è valida", Toast.LENGTH_LONG).show();
            });
        }
    }

    /*private void updateSpinners(){
        int dayPosition = daySpinner.getSelectedItemPosition();
        int monthPosition = monthSpinner.getSelectedItemPosition();

        int lastDayOfMonth = DateUtils.getLastDayOfMonthAsInt(getCurrentMonth(), getCurrentYear());

        List<String> actualDays;
        if(lastDayOfMonth>0)
            actualDays = getDays(DateUtils.MIN_DAY, lastDayOfMonth);
        else
            actualDays = days;
        // TODO calcolare anche actualYears

        daySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, actualDays, DateUtils.DAY_SPINNER, dateField, getCurrentMonth(), getCurrentYear()));
        monthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months, DateUtils.MONTH_SPINNER, dateField, getCurrentMonth(), getCurrentYear()));

        *//*if(dayPosition > lastDayOfMonth)
            dayAutoSelection = true; // TODO al variare di mese/anno si setta sempre l'ultimo del mese finchè l'utente non cambia manualmente il giorno, anche se seleziona lo stesso
        
        // TODO controlla che il giorno selezionato sia enabled, se sì selezionane un altro
        // TODO oltre il giorno, controlla che il mese corrente sia enabled
        if(dayAutoSelection) // setta all'ultimo giorno disponibile e non oscurato
            daySpinner.setSelection(lastDayOfMonth); // TODO controlla se la nuova scelta non è oscurata
        else
            daySpinner.setSelection(dayPosition); // setta al giorno precedentemente selezionato
        monthSpinner.setSelection(monthPosition); // TODO controlla se la nuova scelta non è oscurata*//*
    }*/

    // Validazione per data di produzione
    private Date getPackagingDate(){
        int daySpinnerPosition = daySpinner.getSelectedItemPosition();
        int monthSpinnerPosition = monthSpinner.getSelectedItemPosition();
        int yearSpinnerPosition = yearSpinner.getSelectedItemPosition();

        String dayAsString = daySpinner.getSelectedItem().toString();
        String monthAsString = monthSpinner.getSelectedItem().toString();
        String yearAsString = yearSpinner.getSelectedItem().toString();

        if(dateField==packagingDateField){
            if(
                (daySpinnerPosition >0 && monthSpinnerPosition >0 && yearSpinnerPosition >0) && // Se tutti i campi sono compilati
                (DateUtils.isDateValid(dayAsString, monthAsString, yearAsString)) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return DateUtils.getDate(daySpinner, monthSpinner, yearSpinner);
            }
        }
        return null;
    }

    // Validazione per data di scadenza
    private Date getExpiryDate(){
        int daySpinnerPosition = daySpinner.getSelectedItemPosition();
        int monthSpinnerPosition = monthSpinner.getSelectedItemPosition();
        int yearSpinnerPosition = yearSpinner.getSelectedItemPosition();

        String dayAsString = daySpinner.getSelectedItem().toString();
        String monthAsString = monthSpinner.getSelectedItem().toString();
        String yearAsString = yearSpinner.getSelectedItem().toString();

        if(dateField==expiryDateField){
            if(
                (daySpinnerPosition==0 && monthSpinnerPosition==0 && yearSpinnerPosition==0) || // Se non è stato compilato nessun campo
                (daySpinnerPosition>0 && monthSpinnerPosition==0 && yearSpinnerPosition==0) || // Se è stato compilato solo il giorno di scadenza
                (daySpinnerPosition==0 && monthSpinnerPosition>0 && yearSpinnerPosition==0) || // Se è stato compilato solo il mese di scadenza
                ((daySpinnerPosition>0 && monthSpinnerPosition>0 && yearSpinnerPosition==0) && (!DateUtils.isDateValid(dayAsString, monthAsString, "2019"))) || // Se è stato compilato 29/02 ma l'anno corrente non è bisestile // TODO settare anno corrente
                ((daySpinnerPosition>0 && monthSpinnerPosition>0 && yearSpinnerPosition>0) && (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString))) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return null;
            }
        }
        return DateUtils.getExpiryDate(daySpinner, monthSpinner, yearSpinner);
    }

    private List<String> getDays(int min, int max){
        List<String> entries = new ArrayList<>();
        entries.add("GG");
        fillEntries(entries, min, max); // Inizializza giorni
        return entries;
    }

    private List<String> getMonths(int min, int max){
        List<String> entries = new ArrayList<>();
        entries.add("MM");
        fillEntries(entries, min, max); // Inizializza mesi
        return entries;
    }

    private List<String> getYears(int min, int max){
        List<String> entries = new ArrayList<>();
        entries.add("AAAA");
        fillEntries(entries, min, max); // Inizializza anni
        return entries;
    }

    private void fillEntries(List<String> entries, int min, int max){
        for(int i = min; i<=max; i++){
            if(String.valueOf(i).length()==1)
                entries.add("0" + i);
            else
                entries.add(String.valueOf(i));
        }
    }

    private int getCurrentMonth(){
        return DateUtils.getCalendar(TextUtils.getDate(dateField)).get(Calendar.MONTH);
    }

    private int getCurrentYear(){
        return DateUtils.getCalendar(TextUtils.getDate(dateField)).get(Calendar.YEAR);
    }
}
