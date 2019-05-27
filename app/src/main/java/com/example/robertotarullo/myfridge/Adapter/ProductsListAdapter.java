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
    private List<Product> products;
    private static final String GREEN_BAR = "#8ac249", YELLOW_BAR = "#fec006", RED_BAR = "#f34236";

    public ProductsListAdapter(Context context, int resourceId, List<Product> products) {
        super(context, resourceId, products);
        this.inflater = LayoutInflater.from(context);
        this.products = products;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            Log.d("DEBUG","Inflating view");
            v = inflater.inflate(R.layout.list_element, null);
        }

        Product p = getItem(position);

        TextView quantityTextView = v.findViewById(R.id.elem_lista_quantita);
        TextView nameTextView = v.findViewById(R.id.elem_lista_nome);
        LinearLayout consumptionBar = v.findViewById(R.id.elem_lista_consumption);
        LinearLayout nonConsumptionBar = v.findViewById(R.id.elem_lista_non_consumption);
        TextView dataTextView = v.findViewById(R.id.elem_lista_data);
        TextView typeTextView = v.findViewById(R.id.elem_lista_tipo);
        Button deleteButton = v.findViewById(R.id.deleteButton);

        nameTextView.setText(p.getName());

        if(p instanceof SingleProduct) {
            if(p.isPackaged())
                typeTextView.setText("Prodotto confezionato");
            else
                typeTextView.setText("Prodotto fresco");
        } else
            typeTextView.setText("Confezione");

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
        if(p.getPercentageQuantity()>50)
            nonConsumptionBar.setBackgroundColor(Color.parseColor(GREEN_BAR));
        else if(p.getPercentageQuantity()>30)
            nonConsumptionBar.setBackgroundColor(Color.parseColor(YELLOW_BAR));
        else
            nonConsumptionBar.setBackgroundColor(Color.parseColor(RED_BAR));

        // TODO implementare visualizzazione data per Pack
        /*if(p.getExpiryDate()!= null && p.getExpiryDate().equals(DateUtils.getDate("01", "01", "1970"))) // TODO Cambiare controllo data "mai"
            dataTextView.setText("Non scade mai");
        else if(p instanceof SingleProduct && ((SingleProduct) p).getActualExpiringDate()!=null)
            dataTextView.setText(DateUtils.getFormattedDate(((SingleProduct) p).getActualExpiringDate()));
        else if(p.getExpiryDate()!=null)
            dataTextView.setText(DateUtils.getFormattedDate(p.getExpiryDate()));
        else
            dataTextView.setText("Data di scadenza non specificata");*/

        deleteButton.setTag(position);

        return v;
    }
}

