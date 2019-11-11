package com.example.robertotarullo.myfridge.watcher;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.activity.EditProduct;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.utils.TextUtils;

public class PiecesWatcher implements TextWatcher {
    private Button addButton, subtractButton;
    private SeekBar currentWeightSlider;
    private TextView currentPiecesField;
    private EditText weightField, currentWeightField, currentPercentageField;
    private View currentPiecesBlock, currentPercentageBlock;
    private String sliderTagCurrentWeight, sliderTagPieces, sliderTagPerentage;

    public PiecesWatcher(Activity activity){
        this.sliderTagCurrentWeight = activity.getString(R.string.currentweightslider_tag_weight);
        this.sliderTagPieces = activity.getString(R.string.currentweightslider_tag_pieces);
        this.sliderTagPerentage = activity.getString(R.string.currentweightslider_tag_percentage);
        this.addButton = activity.findViewById(R.id.piecesAddButton);
        this.subtractButton = activity.findViewById(R.id.piecesSubtractButton);
        this.currentWeightSlider = activity.findViewById(R.id.currentWeightSlider);
        this.currentPiecesField = activity.findViewById(R.id.currentPiecesField);
        this.weightField = activity.findViewById(R.id.weightField);
        this.currentWeightField = activity.findViewById(R.id.currentWeightField);
        this.currentPiecesBlock = activity.findViewById(R.id.currentPiecesBlock);
        this.currentPercentageField = activity.findViewById(R.id.currentPercentageField);
        this.currentPercentageBlock = activity.findViewById(R.id.currentPercentageBlock);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        TextUtils.updateQuantityButtonsView(addButton, subtractButton, s, SingleProduct.DEFAULT_PIECES, EditProduct.MAX_PIECES);
        int pieces = TextUtils.getInt(s);

        if(pieces>1){
            currentPiecesBlock.setVisibility(View.VISIBLE);
            currentPercentageBlock.setVisibility(View.GONE);

            currentWeightSlider.setTag(sliderTagPieces); // setta lo slider in base al numero di pezzi

            // calcola il numero di pezzi rimanenti rispetto al valore percentuale
            float currentPiecesAsFloat = TextUtils.getFloat(currentPercentageField) * pieces / (float)100;
            int currentPieces = (int) Math.ceil(currentPiecesAsFloat);
            currentWeightSlider.setMax(pieces);
            currentWeightSlider.setProgress(currentPieces);
            currentPiecesField.setText(String.valueOf(currentPieces));

            if(!TextUtils.isEmpty(weightField)){
                float currentWeightAsFloat = (TextUtils.getInt(weightField) * currentWeightSlider.getProgress()) / (float)pieces; // calcola il nuovo currentWeight rispetto ai pezzi
                //int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                currentWeightField.setText(String.valueOf(currentWeightAsFloat));
            }
        } else { // setta lo slider in base al peso, se non compilato in percentuale generica
            currentPiecesBlock.setVisibility(View.GONE);
            if(TextUtils.isEmpty(weightField)){
                currentPercentageBlock.setVisibility(View.VISIBLE);
            }
            currentPiecesField.setText(s.toString());

            if(!TextUtils.isEmpty(weightField)){
                currentWeightSlider.setTag(sliderTagCurrentWeight); // ripristina lo slide rispetto al peso attuale

                // calcola il nuovo currentWeight rispetto al valore percentuale
                float currentWeightAsFloat = (TextUtils.getFloat(currentPercentageField) * TextUtils.getInt(weightField)) / (float)100;
                int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                currentWeightSlider.setMax(TextUtils.getInt(weightField));
                currentWeightSlider.setProgress(currentWeight);
            } else {
                // ripristina lo slide rispetto al valore percentuale
                currentWeightSlider.setTag(sliderTagPerentage);
                currentWeightSlider.setMax((int) SingleProduct.DEFAULT_PERCENTAGEQUANTITY);
                currentWeightSlider.setProgress((int) Math.ceil(TextUtils.getFloat(currentPercentageField)));
            }
        }
    }
}