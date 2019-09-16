package com.example.robertotarullo.myfridge.Watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.example.robertotarullo.myfridge.Activity.EditProduct;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

public class QuantityWatcher implements TextWatcher {
    private Button addButton, subtractButton;

    public QuantityWatcher(Button addButton, Button subtractButton){
       this.addButton = addButton;
       this.subtractButton = subtractButton;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        TextUtils.updateQuantityButtonsView(addButton, subtractButton, s, EditProduct.MIN_QUANTITY, EditProduct.MAX_QUANTITY);
    }

}