package com.example.robertotarullo.myfridge.Listener;

import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

public class CurrentWeightSliderListener implements SeekBar.OnSeekBarChangeListener{

    private EditText weightField, currentWeightField;
    private TextView piecesField, currentPiecesField;

    public CurrentWeightSliderListener(EditText weightField, EditText currentWeightField,  TextView piecesField, TextView currentPiecesField){
        this.weightField = weightField;
        this.currentWeightField = currentWeightField;
        this.piecesField = piecesField;
        this.currentPiecesField = currentPiecesField;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(progress!=0){
            int currentPercentage = 100;

            if(seekBar.getTag().toString().equals("percentage")){
                currentPercentage = progress;
            } else if(seekBar.getTag().toString().equals("currentWeight")){
                currentWeightField.setText(String.valueOf(progress));
                float percentageAsFloat = (progress * 100) / (float)TextUtils.getInt(weightField);
                currentPercentage = (int) Math.ceil(percentageAsFloat);
            } else if(seekBar.getTag().toString().equals("pieces")){
                currentPiecesField.setText(String.valueOf(progress));
                float percentageAsFloat = (progress * 100) / (float)TextUtils.getInt(piecesField);
                currentPercentage = (int) Math.ceil(percentageAsFloat);

                if(!TextUtils.isEmpty(weightField)){
                    float currentWeightAsFloat = (TextUtils.getInt(weightField) * progress) / (float)TextUtils.getInt(piecesField);
                    int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                    currentWeightField.setText(String.valueOf(currentWeightAsFloat));
                }
            }

            if(fromUser)
                seekBar.setTag(R.id.percentageValue, String.valueOf(currentPercentage));
        } else {
            // chiedi all'utente se vuole che il prodotto venga segnato come consumato
            seekBar.setProgress(1);
        }


        System.out.println("Tipo di slider: " + seekBar.getTag().toString());
        System.out.println("Percentuale interna dello slider: " + seekBar.getTag(R.id.percentageValue).toString());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
