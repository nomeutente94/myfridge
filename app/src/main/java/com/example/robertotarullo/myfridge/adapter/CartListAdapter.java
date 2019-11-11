package com.example.robertotarullo.myfridge.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.activity.Cart;
import com.example.robertotarullo.myfridge.bean.ProductForm;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

import java.util.List;

public class CartListAdapter extends ArrayAdapter<ProductForm> {
    private LayoutInflater inflater;
    private ProductForm p;
    private TextView nameTextView, priceTextView;
    private Button deleteButton;

    public CartListAdapter(Context context, int resourceId, List<ProductForm> products) {
        super(context, resourceId, products);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = inflater.inflate(R.layout.list_cart_element, null);

        p = getItem(position);

        nameTextView = v.findViewById(R.id.elem_lista_nome);
        priceTextView = v.findViewById(R.id.elem_lista_prezzo);
        deleteButton = v.findViewById(R.id.deleteButton);

        if (p.getProduct().getBrand() != null)
            nameTextView.setText(p.getProduct().getName() + " " + p.getProduct().getBrand());
        else
            nameTextView.setText(p.getProduct().getName());

        nameTextView.setText(nameTextView.getText() + " (x" + p.getQuantity() + ")");

        if(p.getProduct().getPrice()>0) {
            priceTextView.setVisibility(View.VISIBLE);
            priceTextView.setText("€" + PriceUtils.getFormattedPrice(p.getProduct().getPrice()));
        } else{
            priceTextView.setVisibility(View.INVISIBLE);
        }


        deleteButton.setTag(position);

        return v;
    }
}

