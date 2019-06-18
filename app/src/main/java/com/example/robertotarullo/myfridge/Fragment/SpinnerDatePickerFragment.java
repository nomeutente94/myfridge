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

    private Spinner daySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private EditText dateField;

    private List<String> days;
    private List<String> months;
    private List<String> years;

    private DateSpinnerAdapter dayAdapter;
    private DateSpinnerAdapter monthAdapter;
    private DateSpinnerAdapter yearAdapter;

    // Aggiorna gli spinner solo se l'utente interagisce con esso
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

        SpinnerInteractionListener monthListener = new SpinnerInteractionListener();
        monthSpinner.setOnTouchListener(monthListener);
        monthSpinner.setOnItemSelectedListener(monthListener);

        SpinnerInteractionListener yearListener = new SpinnerInteractionListener();
        yearSpinner.setOnTouchListener(yearListener);
        yearSpinner.setOnItemSelectedListener(yearListener);

        String title = "Seleziona data";
        if(dateField==getActivity().findViewById(R.id.packagingDateField))
            title += " di produzione/lotto";
        else if(dateField==getActivity().findViewById(R.id.expiryDateField))
            title += " di scadenza";

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

        daySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days, DateSpinnerAdapter.DAY_SPINNER, dateField, getCurrentMonth(), getCurrentYear()));
        monthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months, DateSpinnerAdapter.MONTH_SPINNER, dateField, getCurrentMonth(), getCurrentYear()));

        daySpinner.setSelection(dayPosition);
        monthSpinner.setSelection(monthPosition);
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
        days = new ArrayList<>();
        months = new ArrayList<>();
        years = new ArrayList<>();

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

        dayAdapter = new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days, DateSpinnerAdapter.DAY_SPINNER, dateField, getCurrentMonth(), getCurrentYear());
        monthAdapter = new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months, DateSpinnerAdapter.MONTH_SPINNER, dateField, getCurrentMonth(), getCurrentYear());
        yearAdapter = new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years, DateSpinnerAdapter.YEAR_SPINNER, dateField, getCurrentMonth(), getCurrentYear());

        daySpinner.setAdapter(dayAdapter);
        monthSpinner.setAdapter(monthAdapter);
        yearSpinner.setAdapter(yearAdapter);
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
