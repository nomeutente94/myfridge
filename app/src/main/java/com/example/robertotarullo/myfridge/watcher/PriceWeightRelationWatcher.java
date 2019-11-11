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
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.utils.TextUtils;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

public class PriceWeightRelationWatcher implements TextWatcher {

    private String PRICE_TAG, WEIGHT_TAG, PRICEPERKILO_TAG;
    private String type;
    private Button clearButton1, clearButton2;
    private EditText editText1, editText2, currentPercentageField;
    private SeekBar currentWeightSlider;
    private EditText currentWeightField;
    private View currentWeightBlock, currentPercentageBlock;
    private TextView piecesField;
    private String sliderTagCurrentWeight, sliderTagPieces, sliderTagPerentage;

    public PriceWeightRelationWatcher(String type, EditText editText1, EditText editText2, Button clearButton1, Button clearButton2, Activity activity){
        this.PRICE_TAG = activity.getString(R.string.field_price_tag);
        this.WEIGHT_TAG = activity.getString(R.string.field_weight_tag);
        this.PRICEPERKILO_TAG = activity.getString(R.string.field_pricePerKilo_tag);
        this.sliderTagCurrentWeight = activity.getString(R.string.currentweightslider_tag_weight);
        this.sliderTagPieces = activity.getString(R.string.currentweightslider_tag_pieces);
        this.sliderTagPerentage = activity.getString(R.string.currentweightslider_tag_percentage);
        this.type = type;
        this.editText1 = editText1;
        this.editText2 = editText2;
        this.currentWeightField = activity.findViewById(R.id.currentWeightField);
        this.clearButton1 = clearButton1;
        this.clearButton2 = clearButton2;
        this.currentWeightSlider = activity.findViewById(R.id.currentWeightSlider);
        this.currentWeightBlock = activity.findViewById(R.id.currentWeightBlock);
        this.currentPercentageField = activity.findViewById(R.id.currentPercentageField);
        this.currentPercentageBlock = activity.findViewById(R.id.currentPercentageBlock);
        this.piecesField = activity.findViewById(R.id.piecesField);
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

                currentWeightBlock.setVisibility(View.GONE);
                if(TextUtils.getInt(piecesField)==1){
                    currentPercentageBlock.setVisibility(View.VISIBLE);
                }
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

                currentWeightBlock.setVisibility(View.VISIBLE);
                currentPercentageBlock.setVisibility(View.GONE);
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

    // s è il campo modificato, editText1 è il campo già compilato di cui si usa il valore, editText2 è il campo di cui si vuole calcolare il valore
    private void reflectToField(Editable s, EditText editText1, EditText editText2){
        float value = 0;

        if(!type.equals(WEIGHT_TAG) && !editText1.getTag().equals(WEIGHT_TAG)){  // Se si calcola weight...

            if(type.equals(PRICE_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG)) { // price e pricePerKilo
                value = PriceUtils.getWeight(TextUtils.getFloat(s), TextUtils.getFloat(editText1));
            } else if(type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(PRICE_TAG)) { // pricePerKilo e price
                value = PriceUtils.getWeight(TextUtils.getFloat(editText1), TextUtils.getFloat(s));
            }

            // Cambia valore solo se diverso dal precedente, per evitare loop nel textwatcher
            if(!PriceUtils.getFormattedWeight(value).equals(PriceUtils.getFormattedWeight(TextUtils.getFloat(editText2)))) {
                editText2.setText(PriceUtils.getFormattedWeight(value));
                setWeight(Math.round(value));
            }
        } else { // Se si calcola prezzo o prezzo/kg...

            // weight e pricePerKilo -> price
            if (type.equals(WEIGHT_TAG) && editText1.getTag().equals(PRICEPERKILO_TAG)) {
                value = PriceUtils.getPrice(TextUtils.getFloat(editText1), TextUtils.getFloat(s));
            }
            // pricePerKilo e weight -> price
            else if (type.equals(PRICEPERKILO_TAG) && editText1.getTag().equals(WEIGHT_TAG)) {
                value = PriceUtils.getPrice(TextUtils.getFloat(s), TextUtils.getFloat(editText1));
            }
            // price e weight -> pricePerKilo
            else if (type.equals(PRICE_TAG) && editText1.getTag().equals(WEIGHT_TAG)) {
                value = PriceUtils.getPricePerKilo(TextUtils.getFloat(s), TextUtils.getFloat(editText1));
            }
            // weight e price  -> pricePerKilo
            else if (type.equals(WEIGHT_TAG) && editText1.getTag().equals(PRICE_TAG)) {
                value = PriceUtils.getPricePerKilo(TextUtils.getFloat(editText1), TextUtils.getFloat(s));
            }
            // Cambia solo se il valore è diverso dal precedente, per evitare loop nel textwatcher
            if(!PriceUtils.getFormattedPrice(value).equals(PriceUtils.getFormattedPrice(TextUtils.getFloat(editText2)))) {
                editText2.setText(PriceUtils.getFormattedPrice(value));
            }
        }
    }

    // Gestisce le conseguenze del modificare con un valore il campo peso sul peso attuale e il relativo slider
    private void setWeight(int weight){
        if(weight==0){
            currentWeightField.setText("");
            if(currentWeightSlider.getTag().toString().equals(sliderTagCurrentWeight)){
                // ripristina lo slide rispetto al valore percentuale
                currentWeightSlider.setTag(sliderTagPerentage);
                currentWeightSlider.setMax((int) SingleProduct.DEFAULT_PERCENTAGEQUANTITY);
                currentWeightSlider.setProgress((int) Math.ceil(TextUtils.getFloat(currentPercentageField)));
            }
        } else {
            if(currentWeightSlider.getTag().toString().equals(sliderTagPerentage))
                currentWeightSlider.setTag(sliderTagCurrentWeight);

            // Calcola il nuovo currentWeight rispetto al valore percentuale
            float currentWeightAsFloat = (TextUtils.getFloat(currentPercentageField) * weight) / SingleProduct.DEFAULT_PERCENTAGEQUANTITY;
            int currentWeight = (int) Math.ceil(currentWeightAsFloat);

            if(currentWeightSlider.getTag().toString().equals(sliderTagCurrentWeight)) {
                currentWeightSlider.setMax(weight);
                currentWeightSlider.setProgress(currentWeight);
            }
            currentWeightField.setText(String.valueOf(currentWeight));
        }
    }
}