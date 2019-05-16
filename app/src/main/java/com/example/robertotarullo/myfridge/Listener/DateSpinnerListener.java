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
    // Avvisa se la data attuale non esiste
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String date;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Controlla ad ogni cambiamento se sono compilati tutti e tre i campi
        if(daySpinner.getSelectedItemPosition()>0 && monthSpinner.getSelectedItemPosition()>0 && yearSpinner.getSelectedItemPosition()>0) { // Se tutti e tre gli spinner hanno un valore
            date = daySpinner.getSelectedItem().toString() + "/" + monthSpinner.getSelectedItem().toString() + "/" + yearSpinner.getSelectedItem().toString(); // TODO Permettere di settare il formato della data

            Date convertedDate = DateUtils.getDate(daySpinner.getSelectedItem().toString(), monthSpinner.getSelectedItem().toString(), yearSpinner.getSelectedItem().toString()); // Leggi data

            // Controlla se la data letta corrisponde a quella inserita
            if(!date.equals(dateFormat.format(convertedDate)))
                illegalExpiryDateTextView.setVisibility(View.VISIBLE); // Non corrisponde, mostra il messaggio di errore
            else
                illegalExpiryDateTextView.setVisibility(View.GONE); // Corrisponde, rimuovi il messaggio di errore
        } else
            illegalExpiryDateTextView.setVisibility(View.GONE); // I campi non sono tutti compilati, rimuovi il messaggio di errore
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
