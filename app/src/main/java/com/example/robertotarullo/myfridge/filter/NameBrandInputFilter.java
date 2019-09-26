package com.example.robertotarullo.myfridge.filter;

import android.text.InputFilter;
import android.text.Spanned;

public class NameBrandInputFilter implements InputFilter {

    @Override
    // Buffer is going to replace the range dstart … dend of dest
    // with the new text from the range start … end of source.
    // Return the CharSequence that you would like to have placed there instead,
    // including an empty string if appropriate, or null to accept the original replacement
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        if (source.equals(" ")){

            // Impedisci spazi consecutivi
            if(dest.length()>0 && (dest.toString().charAt(dstart-1)==' '))
                return "";

            // Impedisci lo zero al primo posto
            if(dstart==0)
                return "";
        }

        return null;
    }

}