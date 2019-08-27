package com.example.robertotarullo.myfridge.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.DateSpinnerAdapter;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpinnerDatePickerFragment extends DialogFragment{

    boolean dayAutoSelection;

    private Spinner daySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private EditText dateField;

    private List<String> days;
    private List<String> months;

    public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
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
    }

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
        updateSpinners();

        // Aggiungi listener agli spinner giorno / mese / anno
        /*SpinnerInteractionListener dayListener = new SpinnerInteractionListener();
        daySpinner.setOnTouchListener(dayListener);*/
        daySpinner.setOnItemSelectedListener(new DaySpinnerInteractionListener());

        SpinnerInteractionListener monthListener = new SpinnerInteractionListener();
        monthSpinner.setOnTouchListener(monthListener);
        monthSpinner.setOnItemSelectedListener(monthListener);

        SpinnerInteractionListener yearListener = new SpinnerInteractionListener();
        yearSpinner.setOnTouchListener(yearListener);
        yearSpinner.setOnItemSelectedListener(yearListener);

        String title = "Data ";
        if(dateField==getActivity().findViewById(R.id.packagingDateField))
            title += "di produzione/lotto";
        else if(dateField==getActivity().findViewById(R.id.expiryDateField))
            title += "di scadenza";

        builder.setView(view)
            .setTitle(title)
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
                Date formDate = null;

                if(dateField==getActivity().findViewById(R.id.packagingDateField))
                    formDate = getPackagingDate();
                else if(dateField==getActivity().findViewById(R.id.expiryDateField))
                    formDate = getExpiryDate();

                if(formDate!=null){
                    System.out.println("formDate: " + formDate);
                    dateField.setText(DateUtils.getFormattedDate(formDate));
                    dismiss();
                } else
                    Toast.makeText(this.getActivity(), "La data immessa non è valida", Toast.LENGTH_LONG).show();
            });
        }
    }

    private void updateSpinners(){
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

        if(dayPosition > lastDayOfMonth)
            dayAutoSelection = true; // TODO al variare di mese/anno si setta sempre l'ultimo del mese finchè l'utente non cambia manualmente il giorno, anche se seleziona lo stesso
        
        // TODO controlla che il giorno selezionato sia enabled, se sì selezionane un altro
        // TODO oltre il giorno, controlla che il mese corrente sia enabled
        if(dayAutoSelection) // setta all'ultimo giorno disponibile e non oscurato
            daySpinner.setSelection(lastDayOfMonth); // TODO controlla se la nuova scelta non è oscurata
        else
            daySpinner.setSelection(dayPosition); // setta al giorno precedentemente selezionato
        monthSpinner.setSelection(monthPosition); // TODO controlla se la nuova scelta non è oscurata
    }

    // Validazione per data di produzione
    private Date getPackagingDate(){
        int daySpinnerPosition = daySpinner.getSelectedItemPosition();
        int monthSpinnerPosition = monthSpinner.getSelectedItemPosition();
        int yearSpinnerPosition = yearSpinner.getSelectedItemPosition();

        String dayAsString = daySpinner.getSelectedItem().toString();
        String monthAsString = monthSpinner.getSelectedItem().toString();
        String yearAsString = yearSpinner.getSelectedItem().toString();

        if(dateField==getActivity().findViewById(R.id.packagingDateField)){
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

        if(dateField==getActivity().findViewById(R.id.expiryDateField)){
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

    private void initializeExpiryDateSpinner() {
        days = getDays(DateUtils.MIN_DAY, DateUtils.MAX_DAY);
        months = getMonths(DateUtils.MIN_MONTH, DateUtils.MAX_MONTH);
        List<String> years = getYears(DateUtils.MIN_YEAR, DateUtils.MAX_YEAR);

        DateSpinnerAdapter dayAdapter = new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days, DateUtils.DAY_SPINNER, dateField, getCurrentMonth(), getCurrentYear());
        DateSpinnerAdapter monthAdapter = new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months, DateUtils.MONTH_SPINNER, dateField, getCurrentMonth(), getCurrentYear());
        DateSpinnerAdapter yearAdapter = new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years, DateUtils.YEAR_SPINNER, dateField, getCurrentMonth(), getCurrentYear());

        daySpinner.setAdapter(dayAdapter);
        monthSpinner.setAdapter(monthAdapter);
        yearSpinner.setAdapter(yearAdapter);
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
        fillEntries(entries, min, max); // Inizializza giorni
        return entries;
    }

    private List<String> getYears(int min, int max){
        List<String> entries = new ArrayList<>();
        entries.add("AAAA");
        fillEntries(entries, min, max); // Inizializza giorni
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
        int currentMonth = -1;
        if(monthSpinner.getSelectedItemPosition()>0)
            currentMonth = Integer.valueOf(monthSpinner.getSelectedItem().toString());
        return currentMonth;
    }

    private int getCurrentYear(){
        int currentYear = -1;
        if(yearSpinner.getSelectedItemPosition()>0)
            currentYear = Integer.valueOf(yearSpinner.getSelectedItem().toString());
        return currentYear;
    }
}
