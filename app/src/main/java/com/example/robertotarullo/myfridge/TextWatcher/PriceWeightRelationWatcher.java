package com.example.robertotarullo.myfridge.TextWatcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.TextUtils;
import com.example.robertotarullo.myfridge.Utils.PriceUtils;

import org.w3c.dom.Text;

public class PriceWeightRelationWatcher implements TextWatcher {
    private String type;
    private Button clearButton1, clearButton2;
    private EditText editText1, editText2;
    private final String PRICE_TAG = "priceField", WEIGHT_TAG = "weightField", PRICEPERKILO_TAG = "pricePerKiloField";
    private SeekBar currentWeightSlider;
    private EditText currentWeightField;

    public PriceWeightRelationWatcher(String type, EditText editText1, EditText editText2, Button clearButton1, Button clearButton2, EditText currentWeightField, SeekBar currentWeightSlider){
        this.type = type;
        this.editText1 = editText1;
        this.editText2 = editText2;
        this.currentWeightField = currentWeightField;
        this.clearButton1 = clearButton1;
        this.clearButton2 = clearButton2;
        this.currentWeightSlider = currentWeightSlider;
    }

    private void unsetWeight(){
        currentWeightField.setText("");
        if(currentWeightSlider.getTag().toString().equals("currentWeight")){
            // ripristina lo slide rispetto al valore percentuale
            currentWeightSlider.setTag("percentage");
            currentWeightSlider.setMax(100);
            currentWeightSlider.setProgress(Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()));
        }
    }

    private void setWeight(Editable weight){
        if(currentWeightSlider.getTag().toString().equals("percentage"))
            currentWeightSlider.setTag("currentWeight");

        // calcola il nuovo currentWeight rispetto al valore percentuale
        float currentWeightAsFloat = (Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()) * TextUtils.getInt(weight)) / (float)100;
        int currentWeight = (int) Math.ceil(currentWeightAsFloat);

        if(currentWeightSlider.getTag().toString().equals("currentWeight")) {
            currentWeightSlider.setMax(TextUtils.getInt(weight));
            currentWeightSlider.setProgress(currentWeight);
        }

        currentWeightField.setText(String.valueOf(currentWeightAsFloat));
    }

    private void reflectToField(Editable s, EditText editText1, EditText editText2, Button clearButton){
        float value = 0;
        boolean weightValue = false;

        if (type.equals(WEIGHT_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG))         // weight e pricePerKilo
            value = (TextUtils.getFloat(editText1) * TextUtils.getFloat(s)) / 1000;                                 // -> price
        else if (type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(WEIGHT_TAG))    // pricePerKilo e weight
            value = (TextUtils.getFloat(s) * TextUtils.getFloat(editText1)) / 1000;                                 // -> price
        else if (type.equals(PRICE_TAG) && editText1.getTag().equals(WEIGHT_TAG))           // price e weight
            value = (TextUtils.getFloat(s) * 1000) / TextUtils.getFloat(editText1);                                 // -> pricePerKilo
        else if (type.equals(WEIGHT_TAG) && editText1.getTag().equals(PRICE_TAG))           // weight e price
            value = (TextUtils.getFloat(editText1) * 1000) / TextUtils.getFloat(s);                                 // -> pricePerKilo
        else if (type.equals(PRICE_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG)) {   // price e pricePerKilo
            value = (TextUtils.getFloat(s) * 1000) / TextUtils.getFloat(editText1);                                 // -> weight
            weightValue = true;
        } else if (type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(PRICE_TAG)){  // pricePerKilo e price
            value = (TextUtils.getFloat(editText1) * 1000) / TextUtils.getFloat(s);                                 // -> weight
            weightValue = true;
        }

        if(weightValue) {
            if(!PriceUtils.getFormattedWeight(value).equals(PriceUtils.getFormattedWeight(TextUtils.getFloat(editText2)))) {
                editText2.setText(PriceUtils.getFormattedWeight(value));
                setWeight(s);
            }
        } else {
            if(!PriceUtils.getFormattedPrice(value).equals(PriceUtils.getFormattedPrice(TextUtils.getFloat(editText2))))
                editText2.setText(PriceUtils.getFormattedPrice(value));
        }
        editText2.setEnabled(false);
        clearButton.setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {

        if(s.length()==0){
            if(type.equals(WEIGHT_TAG)) // viene modificato direttamente weight con un valore vuoto
                unsetWeight();

            if(!editText1.isEnabled()) { // ?
                editText1.setEnabled(true);
                clearButton1.setEnabled(true);
                editText1.setText("");
                if(editText1.getTag().equals(WEIGHT_TAG))
                    unsetWeight();
            } else if(!editText2.isEnabled()) { // ?
                editText2.setEnabled(true);
                clearButton2.setEnabled(true);
                editText2.setText("");
                if(editText2.getTag().equals(WEIGHT_TAG))
                    unsetWeight();
            }
        } else if(type.equals(WEIGHT_TAG))  // viene modificato direttamente weight con un valore non vuoto
            setWeight(s);

        if(s.length()>0 && !TextUtils.isEmpty(editText1) || !TextUtils.isEmpty(editText2)){
            if(editText1.getText().length() > 0 && (editText2.getText().length()==0 || !editText2.isEnabled()))
                reflectToField(s, editText1, editText2, clearButton2);
            else if(editText2.getText().length() > 0 && (editText1.getText().length()==0 || !editText1.isEnabled()))
                reflectToField(s, editText2, editText1, clearButton1);
        }
    }
}