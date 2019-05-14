package com.example.robertotarullo.myfridge.Utils;

import android.text.Editable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class TextUtils {
    // Prova a ritornare il valore del testo nel campo come float
    public static float getFloat(EditText field){
        try{
            return Float.valueOf(field.getText().toString().replace(',', '.'));
        } catch (NumberFormatException e){
            System.out.println("Ops! Impossibile operare con il valore +" + field.getText().toString());
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getInt(EditText field){
        try{
            return Integer.valueOf(field.getText().toString());
        } catch (NumberFormatException e){
            System.out.println("Ops! Impossibile operare con il valore +");
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getInt(TextView field){
        try{
            return Integer.valueOf(field.getText().toString());
        } catch (NumberFormatException e){
            System.out.println("Ops! Impossibile operare con il valore +");
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getIntFromHint(EditText field){
        try{
            return Integer.valueOf(field.getHint().toString());
        } catch (NumberFormatException e){
            System.out.println("Ops! Impossibile operare con il valore +");
            return 0;
        }
    }

    // Prova a ritornare il valore del testo nel campo come int
    public static int getInt(Editable s){
        try{
            return Integer.valueOf(s.toString());
        } catch (NumberFormatException e){
            System.out.println("Ops! Impossibile operare con il valore +");
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
            Log.d("error", "Ops! Impossibile operare con il valore +" + s.toString());
            return 0;
        }
    }

    public static float getFloat(TextView s){
        try{
            return Float.valueOf(s.getText().toString().replace(',', '.'));
        } catch (NumberFormatException e){
            Log.d("error", "Ops! Impossibile operare con il valore +" + s.toString());
            return 0;
        }
    }

    public static void editQuantityByButtons(Button button, Button otherButton, TextView field, int min, int max){
        int value = TextUtils.getInt(field);
        int newValue = -1;

        if(button.getTag().toString().equals("add") && value<max)
            newValue = value + 1;
        else if(button.getTag().toString().equals("subtract") && value>min)
            newValue = value - 1;

        if(newValue!=-1){
            field.setText(String.valueOf(newValue));
            if(TextUtils.getInt(field)==min)
                button.setEnabled(false);
            else
                button.setEnabled(true);
            otherButton.setEnabled(true);
        }
    }


}
