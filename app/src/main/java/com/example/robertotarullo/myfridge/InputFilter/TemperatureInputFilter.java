package com.example.robertotarullo.myfridge.InputFilter;

import android.text.InputFilter;
import android.text.Spanned;

public class TemperatureInputFilter implements InputFilter {
    private static final int MAX_INT_DIGITS = 2;

    @Override
    // Buffer is going to replace the range dstart … dend of dest
    // with the new text from the range start … end of source.
    // Return the CharSequence that you would like to have placed there instead,
    // including an empty string if appropriate, or null to accept the original replacement
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        // Impedisci di inserire un trattino se c'è già
        if(dest.length()>0 && dest.charAt(0)=='-' && source.toString().equals("-")) {
            System.out.println(1);
            return "";
        }

        // Impedisci di inserire un trattino in un posto diverso dal primo
        if(source.toString().equals("-") && dstart!=0) {
            System.out.println(2);
            return "";
        }

        // Impedisci altre cifre dopo il primo 0 intero
        if(dest.toString().length()>0 && dest.charAt(0)=='0'){
            System.out.println(3);
            return "";
        }

        // impedisci uno zero dopo un trattino
        if(dest.toString().indexOf("-")>-1 && source.toString().equals("0") && dstart==1){
            return "";
        }

        // impedisci un numero dopo uno zero
        if(dest.toString().indexOf("0")>-1 && !source.equals("-")){
            System.out.println(4);
            return "";
        }

        // Impedisci più di 2 cifre intere
        if(dest.toString().indexOf("-")>-1){
            if(dest.length() >= MAX_INT_DIGITS+1) {
                System.out.println(5);
                return "";
            }
        } else if(dest.length() >= MAX_INT_DIGITS && !source.equals("-")) {
            System.out.println(6);
            return "";
        }

        return null;
    }

}