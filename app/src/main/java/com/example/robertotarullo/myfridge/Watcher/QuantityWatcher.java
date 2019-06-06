package com.example.robertotarullo.myfridge.Watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.example.robertotarullo.myfridge.Utils.TextUtils;

public class QuantityWatcher implements TextWatcher {
    private Button addButton, subtractButton;
    private int min, max;

    public QuantityWatcher(Button addButton, Button subtractButton, int min, int max){
       this.addButton = addButton;
       this.subtractButton = subtractButton;
       this.min = min;
       this.max = max;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        TextUtils.updateQuantityButtonsView(addButton, subtractButton, s, min, max);
    }

}