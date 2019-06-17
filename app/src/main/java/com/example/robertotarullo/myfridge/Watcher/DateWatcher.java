package com.example.robertotarullo.myfridge.Watcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

// Mostra eventuale warning alert all'inserimento di una data
public class DateWatcher implements TextWatcher {

    private EditText dateField;
    private String previousDate;
    private Activity context;

    public DateWatcher(EditText dateField, Activity context){
        this.dateField = dateField;
        this.context = context;
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {previousDate = s.toString();}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(TextUtils.isDateFieldValidable(dateField)){
            // TODO considerare apertura, expiryDays e expiryDate, anche con quale selezionato se prodotto fresco !
            // TODO alla pressione del tasto 'cambio' se il valore immesso non è valido rispetto agli altri, avvisare o valutare cosa fare
            // TODO confrontare con le date prese da createProductFromFields

            // se la data immessa è compresa in un range valido
            if(DateUtils.getMinWarningDateAllowed(dateField, context).before(TextUtils.getDate(dateField)) && (DateUtils.getMaxWarningDateAllowed(dateField, context).before(TextUtils.getDate(dateField))))
                showDateWarning(previousDate, dateField, "La data selezionata non è sicura, continuare comunque?", context);
        }
    }

    private void showDateWarning(String previousValue, EditText dateField, String message, Activity context) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dateField.setTag(R.id.warningEdit, "warningEdit");
                    dateField.setText(previousValue);
                    dateField.setTag(R.id.warningEdit,null);
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("Attenzione")
                .setPositiveButton("Ok", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }
}