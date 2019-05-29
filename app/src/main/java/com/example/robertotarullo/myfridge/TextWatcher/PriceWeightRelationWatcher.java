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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if(s.length()==0){              // Se si è svuotato un campo
            Log.d("RelationWatcher", "Il campo " + type + " è stato svuotato");

            if(type.equals(WEIGHT_TAG)) // Campo peso
                unsetWeight();

            // Controlla se c'è un campo è calcolato/oscurato
            EditText disabledEditText = null;
            Button disabledClearButton = null;
            if(!editText1.isEnabled()) {
                disabledEditText = editText1;
                disabledClearButton = clearButton1;
            } else if(!editText2.isEnabled()) {
                disabledEditText = editText2;
                disabledClearButton = clearButton2;
            }

            if(disabledEditText!=null){ // se c'è un campo è calcolato/oscurato riattivalo e svuotalo
                disabledEditText.setEnabled(true);
                disabledClearButton.setEnabled(true);
                disabledEditText.setText("");
                if(disabledEditText.getTag().equals(WEIGHT_TAG))
                    unsetWeight();

                Log.d("RelationWatcher", "E' stato sbloccato il campo " +  disabledEditText.getTag());
            }

        } else { // Si è modificato il campo con un valore non vuoto
            Log.d("RelationWatcher", "Il campo " + type + " ha assunto il valore '" + s + "'");

            if(type.equals(WEIGHT_TAG)) // Se si tratta del campo peso modificalo
                setWeight(s);

            if( (!TextUtils.isEmpty(editText1) && TextUtils.isEmpty(editText2)) ||  // Se si hanno due campi non vuoti e il terzo vuoto
                (!TextUtils.isEmpty(editText2) && TextUtils.isEmpty(editText1)))
            {
                EditText nonEmptyField;
                EditText emptyField;
                Button emptyFieldClearButton;

                // Trova quale campo è da calcolare
                if(editText1.getText().length()>0 && editText2.getText().length()==0) {
                    nonEmptyField = editText1;
                    emptyField = editText2;
                    emptyFieldClearButton = clearButton2;
                } else {
                    nonEmptyField = editText2;
                    emptyField = editText1;
                    emptyFieldClearButton = clearButton1;
                }

                Log.d("RelationWatcher", "Calcolo e blocco " +  emptyField.getTag());
                reflectToField(s, nonEmptyField, emptyField); // calcola il terzo campo

                // oscura il campo appena calcolato
                emptyField.setEnabled(false);
                emptyFieldClearButton.setEnabled(false);
            }
        }
    }

    /**
     * @param s è il campo modificato
     * @param editText1 è il campo già compilato
     * @param editText2 è il campo di cui si calcola il risultato
     */
    private void reflectToField(Editable s, EditText editText1, EditText editText2){
        float value = 0;

        if(!type.equals(WEIGHT_TAG) && !editText1.getTag().equals(WEIGHT_TAG)){                 // Se si calcola il peso...
            if(type.equals(PRICE_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG))           // price e pricePerKilo
                value = (TextUtils.getFloat(s) * 1000) / TextUtils.getFloat(editText1);         // -> weight
            else if(type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(PRICE_TAG))      // pricePerKilo e price
                value = (TextUtils.getFloat(editText1) * 1000) / TextUtils.getFloat(s);         // -> weight

            if(!PriceUtils.getFormattedWeight(value).equals(PriceUtils.getFormattedWeight(TextUtils.getFloat(editText2)))) { // Cambia solo se il valore è diverso dal precedente, per evitare loop nel textwatcher
                editText2.setText(PriceUtils.getFormattedWeight(value));
                setWeight(s);
            }
        } else {                                                                                // Se si calcola prezzo o prezzo/kg
            if (type.equals(WEIGHT_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG))         // weight e pricePerKilo
                value = (TextUtils.getFloat(editText1) * TextUtils.getFloat(s)) / 1000;         // -> price
            else if (type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(WEIGHT_TAG))    // pricePerKilo e weight
                value = (TextUtils.getFloat(s) * TextUtils.getFloat(editText1)) / 1000;         // -> price
            else if (type.equals(PRICE_TAG) && editText1.getTag().equals(WEIGHT_TAG))           // price e weight
                value = (TextUtils.getFloat(s) * 1000) / TextUtils.getFloat(editText1);         // -> pricePerKilo
            else if (type.equals(WEIGHT_TAG) && editText1.getTag().equals(PRICE_TAG))           // weight e price
                value = (TextUtils.getFloat(editText1) * 1000) / TextUtils.getFloat(s);         // -> pricePerKilo

            if(!PriceUtils.getFormattedPrice(value).equals(PriceUtils.getFormattedPrice(TextUtils.getFloat(editText2)))) // Cambia solo se il valore è diverso dal precedente, per evitare loop nel textwatcher
                editText2.setText(PriceUtils.getFormattedPrice(value));
        }
    }

    // Gestisce le conseguenze dello svuotare il campo peso sul peso attuale e il relativo slider
    private void unsetWeight(){
        currentWeightField.setText("");
        if(currentWeightSlider.getTag().toString().equals("currentWeight")){
            // ripristina lo slide rispetto al valore percentuale
            currentWeightSlider.setTag("percentage");
            currentWeightSlider.setMax(100);
            currentWeightSlider.setProgress(Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()));
        }
    }

    // Gestisce le conseguenze del modificare con un valore il campo peso sul peso attuale e il relativo slider
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
}