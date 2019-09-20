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
import com.example.robertotarullo.myfridge.utils.TextUtils;

public class PiecesWatcher implements TextWatcher {
    private Button addButton, subtractButton;
    private SeekBar currentWeightSlider;
    private TextView currentPiecesField;
    private EditText weightField, currentWeightField;
    private View currentPiecesFieldLabel, currentWeightSliderLabel;

    public PiecesWatcher(Activity activity){
       this.addButton = activity.findViewById(R.id.piecesAddButton);
       this.subtractButton = activity.findViewById(R.id.piecesSubtractButton);
       this.currentWeightSlider = activity.findViewById(R.id.currentWeightSlider);
       this.currentPiecesField = activity.findViewById(R.id.currentPiecesField);
       this.weightField = activity.findViewById(R.id.weightField);
       this.currentWeightField = activity.findViewById(R.id.currentWeightField);
       this.currentPiecesFieldLabel = activity.findViewById(R.id.currentPiecesFieldLabel);
       this.currentWeightSliderLabel = activity.findViewById(R.id.currentWeightSliderLabel);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        TextUtils.updateQuantityButtonsView(addButton, subtractButton, s, EditProduct.MIN_PIECES, EditProduct.MAX_PIECES);
        int pieces = TextUtils.getInt(s);

        if(pieces>1){
            currentPiecesFieldLabel.setVisibility(View.VISIBLE); // TODO controllare l'intero blocco contentente label + field
            currentPiecesField.setVisibility(View.VISIBLE);
            currentWeightSliderLabel.setVisibility(View.GONE);

            // setta lo slider in base al numero di pezzi
            currentWeightSlider.setTag("pieces");

            // calcola il numero di pezzi rimanenti rispetto al valore percentuale
            float currentPiecesAsFloat = (Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()) * pieces / (float)100);
            int currentPieces = (int) Math.ceil(currentPiecesAsFloat);
            currentWeightSlider.setMax(pieces);
            currentWeightSlider.setProgress(currentPieces);
            currentPiecesField.setText(String.valueOf(currentPieces));

            if(!TextUtils.isEmpty(weightField)){
                // calcola il nuovo currentWeight rispetto ai pezzi
                float currentWeightAsFloat = (TextUtils.getInt(weightField) * currentWeightSlider.getProgress()) / (float)pieces;
                int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                currentWeightField.setText(String.valueOf(currentWeightAsFloat));
            }
        } else { // setta lo slider in base al peso, se non compilato in percentuale generica
            currentPiecesFieldLabel.setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
            currentPiecesField.setVisibility(View.GONE);
            if(TextUtils.isEmpty(weightField))
                currentWeightSliderLabel.setVisibility(View.VISIBLE);

            currentPiecesField.setText(s.toString());
            if(!TextUtils.isEmpty(weightField)){
                // ripristina lo slide rispetto al peso attuale
                currentWeightSlider.setTag("currentWeight");
                // calcola il nuovo currentWeight rispetto al valore percentuale
                float currentWeightAsFloat = (Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()) * TextUtils.getInt(weightField)) / (float)100;
                int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                currentWeightSlider.setMax(TextUtils.getInt(weightField));
                currentWeightSlider.setProgress(currentWeight);
            } else {
                // ripristina lo slide rispetto al valore percentuale
                currentWeightSlider.setTag("percentage");
                currentWeightSlider.setMax(100);
                currentWeightSlider.setProgress(Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()));
            }
        }
    }
}