package com.example.robertotarullo.myfridge.Adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Bean.Pack;
import com.example.robertotarullo.myfridge.Bean.Product;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.R;

public class ProductsListAdapter extends ArrayAdapter<Product> {
    private LayoutInflater inflater;
    private static final String GREEN_BAR = "#8ac249", YELLOW_BAR = "#fec006", RED_BAR = "#f34236";
    private static final int HALF_CONSUMPTION = 50, LOW_CONSUMPTION = 25;
    private Product p;
    private TextView quantityTextView, nameTextView, dataTextView, typeTextView;
    private LinearLayout consumptionBar, nonConsumptionBar;
    private Button deleteButton;

    public ProductsListAdapter(Context context, int resourceId, List<Product> products) {
        super(context, resourceId, products);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG","Inflating view");
            v = inflater.inflate(R.layout.list_element, null);
        }

        p = getItem(position);

        quantityTextView = v.findViewById(R.id.elem_lista_quantita);
        nameTextView = v.findViewById(R.id.elem_lista_nome);
        consumptionBar = v.findViewById(R.id.elem_lista_consumption);
        nonConsumptionBar = v.findViewById(R.id.elem_lista_non_consumption);
        dataTextView = v.findViewById(R.id.elem_lista_data);
        typeTextView = v.findViewById(R.id.elem_lista_tipo);
        deleteButton = v.findViewById(R.id.deleteButton);

        setName();
        setType();
        setConsumption();
        setDate();

        deleteButton.setTag(position);

        return v;
    }

    private void setName(){
        if(p.getBrand()!=null)
            nameTextView.setText(p.getName() + " " + p.getBrand());
        else
            nameTextView.setText(p.getName());
    }

    private void setType(){
        if(p instanceof SingleProduct) {
            deleteButton.setVisibility(View.VISIBLE);

            if(p.isPackaged())
                typeTextView.setText("Prodotto confezionato");
            else
                typeTextView.setText("Prodotto fresco");
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
            typeTextView.setText(((Pack) p).getProducts().size() + " prodotti");
        }
    }

    private void setConsumption(){
        int consumedQuantity;
        if(p.isConsumed()) {
            consumedQuantity = 100;
            deleteButton.setEnabled(false);
        } else {
            consumedQuantity = 100 - p.getPercentageQuantity();
            deleteButton.setEnabled(true);
        }

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                (consumedQuantity)
        );

        consumptionBar.setLayoutParams(param);
        if(p.getPercentageQuantity() > HALF_CONSUMPTION)
            nonConsumptionBar.setBackgroundColor(Color.parseColor(GREEN_BAR));
        else if(p.getPercentageQuantity() > LOW_CONSUMPTION)
            nonConsumptionBar.setBackgroundColor(Color.parseColor(YELLOW_BAR));
        else
            nonConsumptionBar.setBackgroundColor(Color.parseColor(RED_BAR));
    }

    private void setDate(){
        Date expiryDate = DateUtils.getActualExpiryDate(p);

        if(expiryDate!= null){
            if(expiryDate.equals(DateUtils.getNoExpiryDate())) // TODO Cambiare controllo data "mai"
                dataTextView.setText("Mai");
            else
                dataTextView.setText(DateUtils.getLanguageFormattedDate(expiryDate)); // TODO prevedere altre formattazioni data
        } else
            dataTextView.setText("Non specificata");
    }
}

