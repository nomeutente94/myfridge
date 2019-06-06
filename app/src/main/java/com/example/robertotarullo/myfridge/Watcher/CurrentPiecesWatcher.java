package com.example.robertotarullo.myfridge.Watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

public class CurrentPiecesWatcher implements TextWatcher {
    private TextView piecesField;
    private LinearLayout currentPiecesBlock;

    public CurrentPiecesWatcher(TextView piecesField, LinearLayout currentPiecesBlock){
       this.piecesField = piecesField;
       this.currentPiecesBlock = currentPiecesBlock;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if(TextUtils.getInt(s)<TextUtils.getInt(piecesField))
            currentPiecesBlock.setVisibility(View.VISIBLE);
        else
            currentPiecesBlock.setVisibility(View.INVISIBLE);
    }

}