package com.example.robertotarullo.myfridge.adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.robertotarullo.myfridge.activity.MainActivity;
import com.example.robertotarullo.myfridge.bean.Pack;
import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.R;

public class ProductsListAdapter extends ArrayAdapter<Product> {
    private LayoutInflater inflater;
    private static final String GREEN_BAR = "#8ac249", YELLOW_BAR = "#fec006", RED_BAR = "#f34236";
    private static final int HALF_CONSUMPTION = 50, LOW_CONSUMPTION = 25;
    private Product p;
    private TextView quantityTextView, nameTextView, dataTextView, typeTextView;
    private LinearLayout consumptionBar, nonConsumptionBar;
    private Boolean showConsumed;
    private Context context;
    private View optionsButton;

    private MainActivity.Action action;

    public ProductsListAdapter(Context context, int resourceId, List<Product> products, Boolean showConsumed, MainActivity.Action action) {
        super(context, resourceId, products);
        this.inflater = LayoutInflater.from(context);
        this.showConsumed = showConsumed;
        this.context = context;
        this.action = action;
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {
        if (v == null)
            v = inflater.inflate(R.layout.list_element, null);

        p = getItem(position);

        quantityTextView = v.findViewById(R.id.elem_lista_quantita);
        nameTextView = v.findViewById(R.id.elem_lista_nome);
        consumptionBar = v.findViewById(R.id.elem_lista_consumption);
        nonConsumptionBar = v.findViewById(R.id.elem_lista_non_consumption);
        dataTextView = v.findViewById(R.id.elem_lista_data);
        typeTextView = v.findViewById(R.id.elem_lista_tipo);

        optionsButton = v.findViewById(R.id.imagePopup);
        //consumeItem = ((Activity) context).findViewById(R.id.consumeItem);

        setName();
        setType();
        setConsumption();
        setDate();

        optionsButton.setTag(position);

        if(action==MainActivity.Action.PICK){
            dataTextView.setVisibility(View.GONE);
            optionsButton.setVisibility(View.GONE);
            nonConsumptionBar.setVisibility(View.GONE);
        }

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
            optionsButton.setVisibility(View.VISIBLE);

            if(p.isPackaged())
                typeTextView.setText("Confezionato");
            else
                typeTextView.setText("Fresco");
        } else {
            optionsButton.setVisibility(View.INVISIBLE);

            if(action==MainActivity.Action.PICK){
                if(p.isPackaged())
                    typeTextView.setText("Confezionato");
                else
                    typeTextView.setText("Fresco");
            } else {
                typeTextView.setText(String.valueOf(((Pack)p).getSize()));
                if(p.isPackaged())
                    typeTextView.setText(typeTextView.getText() + " confezionati");
                else
                    typeTextView.setText(typeTextView.getText() + " freschi");
            }
        }
    }

    private void setConsumption(){
        if(p instanceof SingleProduct){
            consumptionBar.setVisibility(View.VISIBLE);
            nonConsumptionBar.setVisibility(View.VISIBLE);

            int consumedQuantity = 100;
            if(!p.isConsumed())
                consumedQuantity -= ((SingleProduct)p).getPercentageQuantity();

            consumptionBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0, consumedQuantity));

            if(((SingleProduct)p).getPercentageQuantity() > HALF_CONSUMPTION)
                nonConsumptionBar.setBackgroundColor(Color.parseColor(GREEN_BAR));
            else if(((SingleProduct)p).getPercentageQuantity() > LOW_CONSUMPTION)
                nonConsumptionBar.setBackgroundColor(Color.parseColor(YELLOW_BAR));
            else
                nonConsumptionBar.setBackgroundColor(Color.parseColor(RED_BAR));
        } else {
            consumptionBar.setVisibility(View.INVISIBLE);
            nonConsumptionBar.setVisibility(View.INVISIBLE);
        }
    }

    private void setDate(){
        dataTextView.setTextColor(Color.parseColor("#828282"));

        Date date;
        if(showConsumed) {
            date = ((SingleProduct) p).getConsumptionDate();
            if(date != null)
                dataTextView.setText("Consumato il " + DateUtils.getLanguageFormattedDate(date)); // TODO prevedere altre formattazioni data
            else
                dataTextView.setText("Non specificata");
        } else {
            date = DateUtils.getActualExpiryDate(p);
            if(date != null){
                if(date.equals(DateUtils.getNoExpiryDate())) // TODO Cambiare controllo data "mai"
                    dataTextView.setText("Non scade mai");
                else {
                    Date now = DateUtils.getCurrentDateWithoutTime();
                    if(now.equals(date)) {
                        dataTextView.setTextColor(Color.parseColor("#ea8c00"));
                        dataTextView.setText("Entro oggi");
                    } else if(now.after(date)) {
                        dataTextView.setTextColor(Color.RED);
                        int expiryDays = DateUtils.getDaysByDateDiff(date, now);
                        if(expiryDays==1)
                            dataTextView.setText("Scaduto da 1 giorno");
                        else
                            dataTextView.setText("Scaduto da " + expiryDays + " giorni");
                        //dataTextView.setText("Scaduto il " + DateUtils.getLanguageFormattedDate(date)); // TODO prevedere altre formattazioni data
                    } else {
                        dataTextView.setText("Entro il " + DateUtils.getLanguageFormattedDate(date)); // TODO prevedere altre formattazioni data
                    }
                }
            } else
                dataTextView.setText("Non specificata");
        }
    }
}

