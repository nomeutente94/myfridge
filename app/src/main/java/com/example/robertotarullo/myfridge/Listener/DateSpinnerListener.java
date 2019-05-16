package com.example.robertotarullo.myfridge.Listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.robertotarullo.myfridge.Utils.DateUtils;

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
    // Avvisa se la combinazione giorno-mese è illegale (es. 31/04/YYYY) // TODO controllare solo per calendario gregoriano
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(daySpinner.getSelectedItemPosition()>0 && monthSpinner.getSelectedItemPosition()>0) { // Se giorno e mese hanno un valore
            String year;

            if(yearSpinner.getSelectedItemPosition()>0){
                year = yearSpinner.getSelectedItem().toString();
            } else {
                year = "2020"; // max 29 feb (anno bisestile)
            }

            if(DateUtils.isDateValid(daySpinner.getSelectedItem().toString(), monthSpinner.getSelectedItem().toString(), year))
                illegalExpiryDateTextView.setVisibility(View.GONE); // Permetti, rimuovi il messaggio di errore
            else
                illegalExpiryDateTextView.setVisibility(View.VISIBLE); // Non permettere, mostra il messaggio di errore
        } else
            illegalExpiryDateTextView.setVisibility(View.GONE); // day o month non è compilato, rimuovi il messaggio di errore
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
