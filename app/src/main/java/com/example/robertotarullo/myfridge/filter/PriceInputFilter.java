package com.example.robertotarullo.myfridge.filter;

import android.text.InputFilter;
import android.text.Spanned;

public class PriceInputFilter implements InputFilter {
    private static final int MAX_DECIMALS_DIGITS = 2;
    private static final int MAX_INT_DIGITS = 4;

    @Override
    // Buffer is going to replace the range dstart … dend of dest
    // with the new text from the range start … end of source.
    // Return the CharSequence that you would like to have placed there instead,
    // including an empty string if appropriate, or null to accept the original replacement
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int commaPos = dest.toString().indexOf(',');

        // Impedisci l’assenza di uno zero prima di una virgola e viceversa
        if ((source.equals(",") || source.equals("0")) && dest.toString().length() == 0) {
            return "0,";
        }

        // Impedisci la presenza di più di una virgola
        if((source.equals(",") && commaPos>-1)){
            return "";
        }

        // Limita a due cifre decimali
        if (dest.toString().contains(",")) {
            int digitsAfterComma = dest.toString().substring(commaPos).length();
            if (digitsAfterComma == MAX_DECIMALS_DIGITS+1 && dstart > commaPos) {
                return "";
            }
        }

        // Impedisci altre cifre dopo il primo 0 intero
        if (dest.length()>0 && dest.charAt(0)=='0' && !source.equals(",")) {
            if(commaPos>-1){
                if(dstart == commaPos) { // permesso in prima posizione
                    return "";
                }
            } else {
                return "";
            }
        }

        // Impedisci l'inserimento di uno zero se 0,0
        if(dest.toString().equals("0,0") && source.toString().equals("0")){
            return "";
        }

        // Impedisci più di 4 cifre intere
        if(commaPos>-1){
            String integers = dest.toString().substring(0, commaPos);
            if(integers.length() >= MAX_INT_DIGITS && dstart <= commaPos) {
                return "";
            }
        } else {
            if(dest.length() >= MAX_INT_DIGITS && !source.toString().equals(",")) {
                return "";
            }
        }

        return null;
    }

}