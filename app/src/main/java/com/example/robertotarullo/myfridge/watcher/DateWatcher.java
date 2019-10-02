package com.example.robertotarullo.myfridge.watcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.utils.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateWatcher implements TextWatcher {

    private EditText dateField;
    private String previousDate;
    private Activity context;

    private EditText expiryDateField, purchaseDateField, openingDateField, packagingDateField, consumingDateField;
    private Date purchaseDate, openingDate, packagingDate, consumingDate, expiryDate;

    private StringBuilder msg;
    private List<String> errorMessages;

    private boolean isExpiryDateBlockHidden;

    public DateWatcher(EditText dateField, Activity context){
        this.dateField = dateField;
        this.context = context;
        this.expiryDateField = context.findViewById(R.id.expiryDateField);
        this.purchaseDateField = context.findViewById(R.id.purchaseDateField);
        this.openingDateField = context.findViewById(R.id.openingDateField);
        this.packagingDateField = context.findViewById(R.id.packagingDateField);
        this.consumingDateField = context.findViewById(R.id.consumptionDateField);
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {previousDate = s.toString();}

    @Override
    // Mostra eventuale warning alert all'inserimento di una data
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(context.findViewById(R.id.expiryDateField).getTag(R.id.expirySwitchControl)!=null || TextUtils.isDateFieldValidable(dateField)){
            errorMessages = new ArrayList<>();

            purchaseDate = TextUtils.getDate(purchaseDateField);
            openingDate = TextUtils.getDate(openingDateField);
            packagingDate = TextUtils.getDate(packagingDateField);
            consumingDate = TextUtils.getDate(consumingDateField);

            // TODO codice ripetuto in DateUtils
            if(context.findViewById(R.id.expiryDateBlock).getVisibility() == View.VISIBLE && !((CheckBox)context.findViewById(R.id.packagedCheckBox)).isChecked()) { // se prodotto fresco con data di scadenza visibile
                isExpiryDateBlockHidden = false;
                expiryDate = TextUtils.getDate(expiryDateField);
            } else { // prodotto confezionato oppure expiryDays
                isExpiryDateBlockHidden = true;
                expiryDate = DateUtils.getDateByAddingDays(packagingDate, TextUtils.getInt((EditText) context.findViewById(R.id.expiryDaysAfterOpeningField)));
            }

            System.out.println(isExpiryDateBlockHidden);

            if(dateField==consumingDateField)
                checkConsumingDate();
            else if(dateField==expiryDateField)
                checkExpiryDate();
            else if(dateField==packagingDateField)
                checkPackagingDate();
            else if(dateField==openingDateField)
                checkOpeningDate();
            else if(dateField==purchaseDateField)
                checkPurchaseDate();

            if(errorMessages.size()>0) {
                if (errorMessages.size() == 1){
                    msg.append(' ').append(errorMessages.get(0));
                    msg.append(", continuare comunque?");
                } else {
                    msg.append(':');
                    for(int i=0; i<errorMessages.size(); i++)
                        msg.append("\n- ").append(errorMessages.get(i));
                    msg.append("\n\nContinuare comunque?");
                }

                showDateWarning(previousDate, dateField, msg.toString(), context);
            }
        }
    }

    private void checkPurchaseDate(){
        msg = new StringBuilder("La data di acquisto selezionata è");               // purchaseDate >= expiryDate
        if(purchaseDate!=null){
            if(expiryDate!=null){
                if(purchaseDate.after(expiryDate))
                    errorMessages.add("successiva alla data di scadenza");
                else if(purchaseDate.equals(expiryDate))
                    errorMessages.add("uguale alla data di scadenza");
            }
        }
    }

    private void checkOpeningDate(){
        msg = new StringBuilder("La data di apertura selezionata è");               // openingDate >= expiryDate
        if(openingDate!=null){
            if(expiryDate!=null){
                if(openingDate.after(expiryDate))
                    errorMessages.add("successiva alla data di scadenza");
                else if(openingDate.equals(expiryDate))
                    errorMessages.add("uguale alla data di scadenza");
            }
        }
    }

    private void checkPackagingDate(){
        msg = new StringBuilder("La data di produzione/lotto selezionata è");
        if(packagingDate!=null){
            if(expiryDate!=null){
                if(packagingDate.equals(expiryDate))                                // packagingDate == expiryDate
                    errorMessages.add("uguale alla data di scadenza");
            }

            if(isExpiryDateBlockHidden)
                checkExpiryDate();
        }
    }

    private void checkConsumingDate(){
        msg = new StringBuilder("La data di consumazione selezionata è");
        if(consumingDate!=null){
            if(expiryDate!=null){                                                   // consumingDate >= expiryDate
                if(consumingDate.after(expiryDate))
                    errorMessages.add("successiva alla data di scadenza");
                else if(consumingDate.equals(expiryDate))
                    errorMessages.add("uguale alla data di scadenza");
            }
        }
    }

    private void checkExpiryDate(){
        msg = new StringBuilder("La data di scadenza selezionata è"); // TODO variare testo nel caso in cui sia modificata dalla data di produzione
        if(expiryDate!=null){
            if(expiryDate.equals(packagingDate))                                    // expiryDate == packagingDate
                errorMessages.add("uguale alla data di produzione/lotto");

            if(expiryDate.equals(DateUtils.getCurrentDateWithoutTime()))            // expiryDate <= now
                errorMessages.add("uguale alla data odierna");
            else if(expiryDate.before(DateUtils.getCurrentDateWithoutTime()))
                errorMessages.add("precedente alla data odierna");

            if(consumingDate!=null){
                if(expiryDate.before(consumingDate))                                // expiryDate <= consumingDate
                    errorMessages.add("precedente alla data di consumazione");
                else if(expiryDate.equals(consumingDate))
                    errorMessages.add("uguale alla data di consumazione");
            }

            if(openingDate!=null){
                if(expiryDate.before(openingDate))                                  // expiryDate <= openingDate
                    errorMessages.add("precedente alla data di apertura");
                else if(expiryDate.equals(openingDate))
                    errorMessages.add("uguale alla data di apertura");
            }

            if(purchaseDate!=null){
                if (expiryDate.before(purchaseDate))                                // expiryDate <= purchaseDate
                    errorMessages.add("precedente alla data di acquisto");
                else if (expiryDate.equals(purchaseDate))
                    errorMessages.add("uguale alla data di acquisto");
            }
        }
    }

    private void showDateWarning(String previousValue, EditText dateField, String message, Activity context) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if(context.findViewById(R.id.expiryDateField).getTag(R.id.expirySwitchControl)!=null){
                        if(isExpiryDateBlockHidden)
                            ((EditText)context.findViewById(R.id.expiryDaysAfterOpeningField)).setText("");
                        else
                            ((EditText)context.findViewById(R.id.expiryDateField)).setText("");
                    } else {
                        dateField.setTag(R.id.warningEdit, "warningEdit");
                        dateField.setText(previousValue);
                        dateField.setTag(R.id.warningEdit,null);
                    }
                    break;
            }
            expiryDateField.setTag(R.id.expirySwitchControl, null);
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
               .setCancelable(false)
               .setTitle("Attenzione")
               .setPositiveButton("Continua", dialogClickListener)
               .setNegativeButton("Annulla", dialogClickListener)
               .show();
    }
}