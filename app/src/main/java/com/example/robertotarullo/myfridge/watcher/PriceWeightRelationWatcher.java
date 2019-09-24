package com.example.robertotarullo.myfridge.watcher;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.TextUtils;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

import org.w3c.dom.Text;

public class PriceWeightRelationWatcher implements TextWatcher {
    private String type;
    private Button clearButton1, clearButton2;
    private EditText editText1, editText2, currentPercentageField;
    private final String PRICE_TAG = "priceField", WEIGHT_TAG = "weightField", PRICEPERKILO_TAG = "pricePerKiloField";
    private SeekBar currentWeightSlider;
    private EditText currentWeightField;
    private View currentWeightFieldLabel;
    //private TextView piecesField;

    public PriceWeightRelationWatcher(String type, EditText editText1, EditText editText2, Button clearButton1, Button clearButton2, Activity activity){
        this.type = type;
        this.editText1 = editText1;
        this.editText2 = editText2;
        this.currentWeightField = activity.findViewById(R.id.currentWeightField);
        this.clearButton1 = clearButton1;
        this.clearButton2 = clearButton2;
        this.currentWeightSlider = activity.findViewById(R.id.currentWeightSlider);
        this.currentWeightFieldLabel = activity.findViewById(R.id.currentWeightFieldLabel);
        this.currentPercentageField = activity.findViewById(R.id.currentPercentageField);
        //this.currentWeightSliderLabel = activity.findViewById(R.id.currentWeightSliderLabel);
        //this.piecesField = activity.findViewById(R.id.piecesField);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if(s.length()==0){ // Se si è svuotato un campo
            if(type.equals(WEIGHT_TAG)) { // Campo peso
                setWeight(0);

                currentWeightFieldLabel.setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
                currentWeightField.setVisibility(View.GONE);
                //if(TextUtils.getInt(piecesField)==1)
                    //currentWeightSliderLabel.setVisibility(View.VISIBLE);
            }

            // Controlla e trova se c'è un campo calcolato/oscurato
            EditText disabledEditText = null;
            Button disabledClearButton = null;
            if(!editText1.isEnabled()) {
                disabledEditText = editText1;
                disabledClearButton = clearButton1;
            } else if(!editText2.isEnabled()) {
                disabledEditText = editText2;
                disabledClearButton = clearButton2;
            }

            // se c'è un campo calcolato/oscurato riattivalo e svuotalo
            if(disabledEditText!=null){
                disabledEditText.setEnabled(true);
                disabledClearButton.setEnabled(true);
                disabledEditText.setText("");
                if(disabledEditText.getTag().equals(WEIGHT_TAG))
                    setWeight(0);
            }

        } else { // Si è modificato il campo con un valore non vuoto
            if(type.equals(WEIGHT_TAG)) { // Se si tratta del campo peso modificalo
                setWeight(TextUtils.getInt(s));

                currentWeightFieldLabel.setVisibility(View.VISIBLE); // TODO controllare l'intero blocco contentente label + field
                currentWeightField.setVisibility(View.VISIBLE);
                //currentWeightSliderLabel.setVisibility(View.GONE);
            }

            // Se si ha un campo non vuoto e abilitato e uno o non vuoto o disabilitato
            if(((!TextUtils.isEmpty(editText1) && editText1.isEnabled()) && (TextUtils.isEmpty(editText2) || !editText2.isEnabled()))){
                reflectToField(s, editText1, editText2); // calcola il terzo campo
                clearButton2.setEnabled(false);
                editText2.setEnabled(false);
            } else if(((!TextUtils.isEmpty(editText2) && editText2.isEnabled()) && (TextUtils.isEmpty(editText1) || !editText1.isEnabled()))){
                reflectToField(s, editText2, editText1); // calcola il terzo campo
                clearButton1.setEnabled(false);
                editText1.setEnabled(false);
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

        if(!type.equals(WEIGHT_TAG) && !editText1.getTag().equals(WEIGHT_TAG)){                 // Se si calcola weight...
            if(type.equals(PRICE_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG))           // price e pricePerKilo
                value = (TextUtils.getFloat(s) * 1000) / TextUtils.getFloat(editText1);         // -> weight
            else if(type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(PRICE_TAG))      // pricePerKilo e price
                value = (TextUtils.getFloat(editText1) * 1000) / TextUtils.getFloat(s);         // -> weight

            if(!PriceUtils.getFormattedWeight(value).equals(PriceUtils.getFormattedWeight(TextUtils.getFloat(editText2)))) { // Cambia solo se il valore è diverso dal precedente, per evitare loop nel textwatcher
                editText2.setText(PriceUtils.getFormattedWeight(value));
                setWeight(Math.round(value));
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

    // Gestisce le conseguenze del modificare con un valore il campo peso sul peso attuale e il relativo slider
    private void setWeight(int weight){
        if(weight==0){
            currentWeightField.setText("");
            if(currentWeightSlider.getTag().toString().equals("currentWeight")){
                // ripristina lo slide rispetto al valore percentuale
                currentWeightSlider.setTag("percentage");
                currentWeightSlider.setMax(100);
                currentWeightSlider.setProgress(TextUtils.getInt(currentPercentageField));
            }
        } else {
            if(currentWeightSlider.getTag().toString().equals("percentage"))
                currentWeightSlider.setTag("currentWeight");

            // calcola il nuovo currentWeight rispetto al valore percentuale
            float currentWeightAsFloat = (TextUtils.getInt(currentPercentageField) * weight) / (float)100;
            int currentWeight = (int) Math.ceil(currentWeightAsFloat);

            if(currentWeightSlider.getTag().toString().equals("currentWeight")) {
                currentWeightSlider.setMax(weight);
                currentWeightSlider.setProgress(currentWeight);
            }
            currentWeightField.setText(String.valueOf(currentWeight));
        }
    }
}