package com.example.robertotarullo.myfridge.InputFilter;

import android.text.InputFilter;
import android.text.Spanned;

public class DaysInputFilter implements InputFilter {
    private static final int MAX_INT_DIGITS = 2;

    @Override
    // Buffer is going to replace the range dstart … dend of dest
    // with the new text from the range start … end of source.
    // Return the CharSequence that you would like to have placed there instead,
    // including an empty string if appropriate, or null to accept the original replacement
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        // Impedisci più di 4 cifre intere
        if(dest.length() >= MAX_INT_DIGITS)
            return "";

        // Impedisci lo zero al primo posto
        if (source.equals("0") && dstart==0) {
            return "";
        }

        return null;
    }

}