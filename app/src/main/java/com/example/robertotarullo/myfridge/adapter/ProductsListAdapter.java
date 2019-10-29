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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.robertotarullo.myfridge.activity.ProductsList;
import com.example.robertotarullo.myfridge.bean.Pack;
import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

public class ProductsListAdapter extends ArrayAdapter<Product> {
    private LayoutInflater inflater;
    private static final String GREEN_BAR = "#8ac249", YELLOW_BAR = "#fec006", RED_BAR = "#f34236";
    private static final int HALF_CONSUMPTION = 50, LOW_CONSUMPTION = 25;
    private Product p;
    private TextView quantityTextView, nameTextView, dataTextView, typeTextView, brandTextView, descriptionTextView;
    private LinearLayout consumptionBar;
    private LinearLayout nonConsumptionBar;
    private Boolean showConsumed;
    private Context context;
    private View optionsButton;

    private ProductsList.Action action;

    public ProductsListAdapter(Context context, int resourceId, List<Product> products, Boolean showConsumed, ProductsList.Action action) {
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
        brandTextView = v.findViewById(R.id.elem_lista_brand);
        descriptionTextView = v.findViewById(R.id.elem_lista_descrizione);

        optionsButton = v.findViewById(R.id.imagePopup);

        setName();
        setBrand();



        if (action== ProductsList.Action.PICK || action== ProductsList.Action.MANAGE) {
            setDescription();

            // Codice per togliere il margine
            LinearLayout elementInfoBlock = v.findViewById(R.id.list_element_info_block);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) elementInfoBlock.getLayoutParams();
            params.setMarginEnd(params.getMarginStart());
            elementInfoBlock.setLayoutParams(params);

            dataTextView.setVisibility(View.GONE);
            if (action== ProductsList.Action.PICK)
                optionsButton.setVisibility(View.GONE);
            else {
                optionsButton.setTag(position);
                optionsButton.setVisibility(View.VISIBLE);
            }
            nonConsumptionBar.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            dataTextView.setVisibility(View.VISIBLE);
            nonConsumptionBar.setVisibility(View.VISIBLE);
            descriptionTextView.setVisibility(View.GONE);

            if(p instanceof Pack)
                optionsButton.setVisibility(View.GONE);
            else if(p instanceof SingleProduct) {
                optionsButton.setTag(position);
                optionsButton.setVisibility(View.VISIBLE);
            }

            setConsumption();
            setDate();
        }

        return v;
    }

    private void setName(){
        nameTextView.setText(p.getName());
    }

    private void setBrand(){
        if(p.getBrand()==null) {
            brandTextView.setVisibility(View.GONE);
        } else {
            brandTextView.setVisibility(View.VISIBLE);
            brandTextView.setText(p.getBrand());
        }
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

            if(action== ProductsList.Action.PICK || action== ProductsList.Action.MANAGE){
                if(p.isPackaged())
                    typeTextView.setText("Confezionato");
                else
                    typeTextView.setText("Fresco");
            } else {
                typeTextView.setText(String.valueOf(((Pack)p).getProducts().size()));
                if(p.isPackaged())
                    typeTextView.setText(typeTextView.getText() + " confezionati");
                else
                    typeTextView.setText(typeTextView.getText() + " freschi");
            }
        }
    }

    private void setConsumption(){
        if(p instanceof SingleProduct){
            consumptionBar.setVisibility(View.VISIBLE); // TODO unire la coppia consumptionBar e nonConsumptionBar
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
            consumptionBar.setVisibility(View.INVISIBLE); // TODO unire la coppia consumptionBar e nonConsumptionBar
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
                        int daysLeft = DateUtils.getDaysByDateDiff(now, date);
                        if(daysLeft==1)
                            dataTextView.setText("Entro domani");
                        else if(daysLeft<=7)
                            dataTextView.setText("Entro " + daysLeft + " giorni");
                        else
                            dataTextView.setText("Entro il " + DateUtils.getLanguageFormattedDate(date)); // TODO prevedere altre formattazioni data
                    }
                }
            } else
                dataTextView.setText("Non specificata");
        }
    }

    // Mostra una descrizione su stringa di tutti gli attributi specificati in pickEquals
    private void setDescription(){
        SingleProduct currentProduct = null;
        if(p instanceof SingleProduct)
            currentProduct = (SingleProduct)p;
        else if(p instanceof Pack)
            currentProduct = ((Pack) p).getProducts().get(0);

        StringBuilder msg = new StringBuilder();
        if(currentProduct.isPackaged())
            msg.append("Confezionato");
        else
            msg.append("Non confezionato");

        msg.append(" - ");

        if(currentProduct.getPrice()>0) {
            msg.append("€").append(PriceUtils.getFormattedPrice(currentProduct.getPrice()));
            msg.append(" - ");
        } else if(currentProduct.getWeight()>0 && currentProduct.getPricePerKilo()>0){
            msg.append("€").append(PriceUtils.getFormattedPrice(PriceUtils.getPrice(currentProduct.getPricePerKilo(), currentProduct.getWeight())));
            msg.append(" - ");
        }

        if(currentProduct.getPricePerKilo()>0){
            msg.append("€").append(PriceUtils.getFormattedPrice(currentProduct.getPricePerKilo())).append("/kg");
            msg.append(" - ");
        } else if(currentProduct.getWeight()>0 && currentProduct.getPrice()>0){
            msg.append("€").append(PriceUtils.getFormattedPrice(PriceUtils.getPricePerKilo(currentProduct.getPrice(), currentProduct.getWeight()))).append("/kg");
            msg.append(" - ");
        }

        if(currentProduct.getWeight()>0) {
            msg.append(PriceUtils.getFormattedWeight(currentProduct.getWeight())).append("g");
            msg.append(" - ");
        } else if(currentProduct.getPrice()>0 && currentProduct.getPricePerKilo()>0){
            msg.append(PriceUtils.getFormattedWeight(PriceUtils.getWeight(currentProduct.getPrice(), currentProduct.getPricePerKilo()))).append("g");
            msg.append(" - ");
        }

        if(currentProduct.getPieces()>1)
            msg.append(currentProduct.getPieces()).append(" pezzi");
        else
            msg.append("Pezzo unico");
        msg.append(" - ");

        if(currentProduct.getExpiryDate()!=null && currentProduct.getExpiryDate().equals(DateUtils.getNoExpiryDate())){
            msg.append("Non scade mai");
            msg.append(" - ");
        }

        if(currentProduct.getExpiringDaysAfterOpening()>0 || currentProduct.getExpiringDaysAfterOpening()==-1) {
            if(currentProduct.getExpiringDaysAfterOpening()==1) {
                if (currentProduct.isPackaged())
                    msg.append(currentProduct.getExpiringDaysAfterOpening()).append(" giorno dall'apertura");
                else
                    msg.append(currentProduct.getExpiringDaysAfterOpening()).append(" giorno");
            } else if(currentProduct.getExpiringDaysAfterOpening()==-1){
                if(currentProduct.isPackaged())
                    msg.append("0 giorni dall'apertura");
                else
                    msg.append("0 giorni");
            } else {
                if(currentProduct.isPackaged())
                    msg.append(currentProduct.getExpiringDaysAfterOpening()).append(" giorni dall'apertura");
                else
                    msg.append(currentProduct.getExpiringDaysAfterOpening()).append(" giorni");
            }
            msg.append(" - ");
        }

        if(currentProduct.getStorageCondition()==0)
            msg.append("Dispensa");
        else if(currentProduct.getStorageCondition()==1)
            msg.append("Frigorifero");
        else if(currentProduct.getStorageCondition()==2)
            msg.append("Congelatore");

        if(currentProduct.isPackaged() && (currentProduct.getOpenedStorageCondition()!=currentProduct.getStorageCondition())){
            msg.append("/");
            if(currentProduct.getOpenedStorageCondition()==0)
                msg.append("Dispensa");
            else if(currentProduct.getOpenedStorageCondition()==1)
                msg.append("Frigorifero");
            else if(currentProduct.getOpenedStorageCondition()==2)
                msg.append("Congelatore");
        }

        descriptionTextView.setText(msg.toString());
    }
}

