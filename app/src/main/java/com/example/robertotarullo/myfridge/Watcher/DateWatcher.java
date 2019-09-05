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

import java.util.Date;


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
    // Mostra eventuale warning alert all'inserimento di una data
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(TextUtils.isDateFieldValidable(dateField)){
            // TODO considerare apertura, expiryDays e expiryDate, anche con quale selezionato se prodotto fresco !
            // TODO alla pressione del tasto 'cambio' se il valore immesso non è valido rispetto agli altri, avvisare o valutare cosa fare
            // TODO confrontare con le date prese da createProductFromFields

            EditText expiryDateField = context.findViewById(R.id.expiryDateField);
            EditText purchaseDateField = context.findViewById(R.id.purchaseDateField);
            EditText openingDateField = context.findViewById(R.id.openingDateField);
            EditText packagingDateField = context.findViewById(R.id.packagingDateField);
            EditText consumingDateField = context.findViewById(R.id.consumptionDateField);

            Date expiryDate = TextUtils.getDate(expiryDateField);
            Date purchaseDate = TextUtils.getDate(purchaseDateField);
            Date openingDate = TextUtils.getDate(openingDateField);
            Date packagingDate = TextUtils.getDate(packagingDateField);
            Date consumingDate = TextUtils.getDate(consumingDateField);

            String msg = null;

            if(dateField==consumingDateField){
                if(expiryDate!=null){                                                                       // consumingDate >= expiryDate
                    if(consumingDate.after(expiryDate))
                        msg = "La data di consumazione selezionata è:\n- successiva alla data di scadenza";
                    else if(consumingDate.equals(expiryDate))
                        msg = "La data di consumazione selezionata è:\n- uguale alla data di scadenza";
                }
            } else if(dateField==expiryDateField){
                String defaultMsg = "La data di scadenza selezionata è:";

                msg = defaultMsg;
                if(expiryDate.equals(packagingDate))                                                        // expiryDate == packagingDate
                    msg += "\n- uguale alla data di produzione/lotto";
                if(expiryDate.equals(DateUtils.getCurrentDate()))                                           // expiryDate <= now
                    msg += "\n- uguale alla data ordierna";
                else if(expiryDate.before(DateUtils.getCurrentDate()))
                    msg += "\n- precedente alla data ordierna";
                if(consumingDate!=null){                                                                    // expiryDate <= consumingDate
                    if(expiryDate.before(consumingDate))
                        msg += "\n- precedente alla data di consumazione";
                    else if(expiryDate.equals(consumingDate))
                        msg += "\n- uguale alla data di consumazione";
                }
                if(openingDate!=null){                                                                      // expiryDate <= openingDate
                    if(expiryDate.before(openingDate))
                        msg += "\n- precedente alla data di apertura";
                    else if(expiryDate.equals(openingDate))
                        msg += "\n- uguale alla data di apertura";
                }
                if(purchaseDate!=null) {                                                                    // expiryDate <= purchaseDate
                    if (expiryDate.before(purchaseDate))
                        msg += "\n- precedente alla data di acquisto";
                    else if (expiryDate.equals(purchaseDate))
                        msg += "\n- uguale alla data di acquisto";
                }
                if(msg.equals(defaultMsg))
                    msg = null;

            } else if(dateField==packagingDateField){
                if(packagingDate.equals(expiryDate))                                                        // packagingDate == expiryDate
                    msg = "La data di produzione/lotto selezionata è:\n- uguale alla data di scadenza";
            } else if(dateField==openingDateField){                                                         // openingDate >= expiryDate
                if(expiryDate!=null){
                    if(openingDate.after(expiryDate))
                        msg = "La data di apertura selezionata è:\n- successiva alla data di scadenza";
                    else if(openingDate.equals(expiryDate))
                        msg = "La data di apertura selezionata è:\n- uguale alla data di scadenza";
                }
            } else if(dateField==purchaseDateField){                                                        // purchaseDate >= expiryDate
                if(expiryDate!=null){
                    if(purchaseDate.after(expiryDate))
                        msg = "La data di acquisto selezionata è:\n- successiva alla data di scadenza";
                    else if(purchaseDate.equals(expiryDate))
                        msg = "La data di acquisto selezionata è:\n- uguale alla data di scadenza";
                }
            }

            if(msg!=null) {
                msg += "\n\nContinuare comunque?";
                showDateWarning(previousDate, dateField, msg, context);
            }
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