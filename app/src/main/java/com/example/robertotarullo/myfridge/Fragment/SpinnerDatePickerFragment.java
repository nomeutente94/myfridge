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

        initializeExpiryDateSpinner(daySpinner, monthSpinner, yearSpinner); // Popola i campi
        DateUtils.setDate(daySpinner, monthSpinner, yearSpinner, TextUtils.getDate(dateField)); // Setta gli spinner alla data precedente

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

                if(isDateValid()){
                    dateField.setText(DateUtils.getFormattedDate(DateUtils.getExpiryDate(daySpinner, monthSpinner, yearSpinner)));
                    dismiss();
                } else{
                    Toast.makeText(this.getActivity(), "La data di scadenza immessa non è valida", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean isDateValid(){
        if(isExpiryDateValid() || isPackagingDateValid())
            return true;
        return false;
    }

    // Validazione per data di produzione
    private boolean isPackagingDateValid(){
        if(dateField==getActivity().findViewById(R.id.packagingDateField)){
            if(
                (day==0 || month==0 || year==0) || // Se qualche campo non è compilato
                (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString)) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    // Validazione per data di scadenza
    private boolean isExpiryDateValid(){
        if(dateField==getActivity().findViewById(R.id.expiryDateField)){
            if(
                (day==0 && month==0 && year==0) || // Se non è stato compilato nessun campo
                (day>0 && month==0 && year==0) || // Se è stato compilato solo il giorno di scadenza
                (day==0 && month>0 && year==0) || // Se è stato compilato solo il mese di scadenza
                ((day>0 && month>0 && year==0) && (!DateUtils.isDateValid(dayAsString, monthAsString, "2019"))) || // Se è stato compilato 29/02 ma l'anno corrente non è bisestile // TODO settare anno corrente
                ((day>0 && month>0 && year>0) && (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString))) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void initializeExpiryDateSpinner(Spinner expiryDateDaySpinner, Spinner expiryDateMonthSpinner, Spinner expiryDateYearSpinner) {
        List<String> days = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> years = new ArrayList<>();

        days.add("DD");
        months.add("MM");
        years.add("YYYY");
        for(int i=1; i<=31; i++){
            if(String.valueOf(i).length()==1)
                days.add("0" + String.valueOf(i));
            else
                days.add(String.valueOf(i));
        }

        for(int i=1; i<=12; i++)
            if(String.valueOf(i).length()==1)
                months.add("0" + String.valueOf(i));
            else
                months.add(String.valueOf(i));
        for(int i=2019; i<=2099; i++) // TODO settare anno corrente e un range a partire da esso
            years.add(String.valueOf(i));

        expiryDateDaySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days));
        expiryDateMonthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months));
        expiryDateYearSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years));
    }
}
