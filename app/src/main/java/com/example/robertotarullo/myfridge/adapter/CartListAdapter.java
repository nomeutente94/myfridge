package com.example.robertotarullo.myfridge.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

import java.util.List;

public class CartListAdapter extends ArrayAdapter<SingleProduct> {
    private LayoutInflater inflater;
    private SingleProduct p;
    private TextView nameTextView, priceTextView;
    private Button deleteButton;
    private List<Integer> quantities;

    public CartListAdapter(Context context, int resourceId, List<SingleProduct> products, List<Integer> quantities) {
        super(context, resourceId, products);
        this.inflater = LayoutInflater.from(context);
        this.quantities = quantities;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null)
            v = inflater.inflate(R.layout.list_cart_element, null);

        p = getItem(position);

        nameTextView = v.findViewById(R.id.elem_lista_nome);
        priceTextView = v.findViewById(R.id.elem_lista_prezzo);
        deleteButton = v.findViewById(R.id.deleteButton);

        if (p.getBrand() != null)
            nameTextView.setText(p.getName() + " " + p.getBrand());
        else
            nameTextView.setText(p.getName());

        nameTextView.setText(nameTextView.getText() + " (x" + quantities.get(position) + ")");

        if(p.getPrice()>0) {
            priceTextView.setVisibility(View.VISIBLE);
            priceTextView.setText("€" + PriceUtils.getFormattedPrice(p.getPrice()));
        } else{
            priceTextView.setVisibility(View.INVISIBLE);
        }


        deleteButton.setTag(position);

        return v;
    }
}

