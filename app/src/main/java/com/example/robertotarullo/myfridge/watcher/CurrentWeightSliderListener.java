package com.example.robertotarullo.myfridge.watcher;

import android.app.Activity;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.TextUtils;

public class CurrentWeightSliderListener implements SeekBar.OnSeekBarChangeListener{

    private EditText weightField, currentWeightField, currentPercentageField;
    private TextView piecesField, currentPiecesField;

    public CurrentWeightSliderListener(Activity activity){
        this.weightField = activity.findViewById(R.id.weightField);
        this.currentWeightField = activity.findViewById(R.id.currentWeightField);
        this.piecesField = activity.findViewById(R.id.piecesField);
        this.currentPiecesField = activity.findViewById(R.id.currentPiecesField);
        this.currentPercentageField = activity.findViewById(R.id.currentPercentageField);
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
                currentPercentageField.setText(String.valueOf(currentPercentage));
        } else {
            seekBar.setProgress(1);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
