package com.example.robertotarullo.myfridge.Utils;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Activity.AddProduct;
import com.example.robertotarullo.myfridge.Bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class TextUtils {
    // Prova a ritornare il valore del testo nel campo come float
    public static float getFloat(EditText field){
        try{
            return Float.valueOf(field.getText().toString().replace(',', '.'));
        } catch (NumberFormatException e){
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getInt(EditText field){
        try{
            return Integer.valueOf(field.getText().toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getInt(TextView field){
        try{
            return Integer.valueOf(field.getText().toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getIntFromHint(EditText field){
        try{
            return Integer.valueOf(field.getHint().toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getInt(Editable s){
        try{
            return Integer.valueOf(s.toString());
        } catch (NumberFormatException e){
            return 0;
        }
    }

    // Sposta il cursore dell'EditText alla fine
    // Di solito chiamato dopo una modifica effettuata con setText, in quanto lascia di default il cursore a 0
    public static void setSelectionToEnd(EditText field) {
        field.setSelection(field.getText().length());
    }

    // Controlla se un campo di testo è vuoto
    public static boolean isEmpty(EditText etText) {
        if (etText.getText().toString().length() > 0)
            return false;
        return true;
    }

    public static boolean isEmpty(TextView tvText) {
        if (tvText.getText().toString().length() > 0)
            return false;
        return true;
    }

    public static Date getDate(EditText dateField){
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dateField.getText().toString());
        } catch (ParseException e) {
            Log.d("DEBUG", "Data non valida per: " + dateField.getTag());
            return null;
        }
    }

    // prova a ritornare il valore del testo nel campo come float
    public static float getFloat(Editable s){
        try{
            return Float.valueOf(s.toString().replace(',', '.'));
        } catch (NumberFormatException e){
            return 0;
        }
    }

    public static float getFloat(TextView s){
        try{
            return Float.valueOf(s.getText().toString().replace(',', '.'));
        } catch (NumberFormatException e){
            return 0;
        }
    }

    public static void editQuantityByButtons(Button button, TextView field, int min, int max){
        int value = TextUtils.getInt(field);
        int newValue = -1;

        if(button.getTag().toString().equals("add") && value<max)
            newValue = value + 1;
        else if(button.getTag().toString().equals("subtract") && value>min)
            newValue = value - 1;

        if(newValue!=-1)
            field.setText(String.valueOf(newValue));
    }

    public static void updateQuantityButtonsView(Button addButton, Button subtractButton, Editable s, int min, int max){
        if(getInt(s)==min)
            subtractButton.setEnabled(false);
        else
            subtractButton.setEnabled(true);

        if(getInt(s)==max)
            addButton.setEnabled(false);
        else
            addButton.setEnabled(true);
    }

    public static void clearField(View view) {
        boolean found = false;
        ViewGroup parent = (ViewGroup) view.getParent();

        for(int i=0; i<parent.getChildCount() && !found; i++) {
            if(parent.getChildAt(i) instanceof EditText){
                found = true;
                EditText child = (EditText) parent.getChildAt(i);
                child.setText("");
                child.requestFocus();
            }
        }
    }

    public static void setText(String text, EditText field){
        if(text!=null)
            field.setText(text);
    }

    public static void setPrice(float price, EditText field){
        if(price>0)
            field.setText(PriceUtils.getFormattedPrice(price));
    }

    public static void setWeight(float weight, EditText field){
        if(weight>0) {
            field.setText(PriceUtils.getFormattedWeight(weight));
        }
    }

    public static void setDate(Date date, EditText field){
        if(date!=null)
            editFieldNotFromUser(field, DateUtils.getFormattedDate(date));
    }

    public static void editFieldNotFromUser(EditText dateField, String text){
        dateField.setTag(R.id.warningEdit, "lock");
        dateField.setText(text);
        dateField.setTag(R.id.warningEdit, null);
    }

    public static void setPointOfPurchase(long id, Spinner spinner){
        if(id>0) {
            for(int i=0; i<spinner.getCount(); i++){
                if(((PointOfPurchase)spinner.getItemAtPosition(i)).getId()==id)
                    spinner.setSelection(i);
            }
        }
    }
}
