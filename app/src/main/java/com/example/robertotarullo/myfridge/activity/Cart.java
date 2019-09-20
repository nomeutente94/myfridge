package com.example.robertotarullo.myfridge.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robertotarullo.myfridge.adapter.CartListAdapter;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

import java.util.ArrayList;
import java.util.Collections;

public class Cart extends AppCompatActivity {

    private ArrayList<SingleProduct> cartProducts;

    private ArrayList<Integer> quantities;
    private ArrayList<SingleProduct> listToDisplay;

    // Riferimenti a elementi della view
    private ListView listView;
    private TextView totalPriceText;

    // Adapter lista
    private CartListAdapter productsListAdapter;
    private TextView noProductsWarning;

    @Override
    public void onBackPressed() {
        if(cartProducts.size()>0){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        super.onBackPressed();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Sono presenti uno o più prodotti nel carrello, sei sicuro di volere uscire?")
                    .setTitle("Attenzione")
                    .setPositiveButton("Esci", dialogClickListener)
                    .setNegativeButton("Annulla", dialogClickListener)
                    .show();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_summary);
        setTitle("Carrello");

        // Ottieni riferimenti alle view
        listView = findViewById(R.id.mylistview);
        noProductsWarning = findViewById(R.id.noProductsWarning);
        totalPriceText = findViewById(R.id.totalPriceText);

        cartProducts = new ArrayList<>();
        updateList();

        // Setta il comportamento al click di un elemento
        listView.setOnItemClickListener((parent, view, position, id) -> editSingleProduct(position));
    }

    // Avvia l'activity EditProduct per la modifica
    public void editSingleProduct(int position){
        // TODO modifica prodotto con EditProduct con i campi di action "shopping" e compilando i campi col prodotto cliccato
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra("action", "shopping");
        intent.putExtra("position", position);
        intent.putExtra("quantity", quantities.get(position));
        intent.putExtra("productToEdit", (SingleProduct)listView.getItemAtPosition(position));
        intent.putExtra("suggestions", listToDisplay);
        startActivityForResult(intent, 2);
    }

    public void deleteProduct(View view){
        int position = Integer.parseInt(view.getTag().toString());
        SingleProduct p = productsListAdapter.getItem(position);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    cartProducts.removeAll(Collections.singleton(p));
                    updateList();
                    Toast.makeText(getApplicationContext(), "Prodotto rimosso", Toast.LENGTH_LONG).show();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        String msg = null;

        if(Collections.frequency(cartProducts, p)==1)
            msg = "Rimuovere il prodotto dal carrello?";
        else if(Collections.frequency(cartProducts, p)>1)
            msg = "Rimuovere " +Collections.frequency(cartProducts, p)+ " prodotti dal carrello?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Attenzione")
                .setPositiveButton("Rimuovi", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    public void onConfirmButtonClick(View view) {
        if(cartProducts.size()>0){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // TODO aggiungere qui i prodotti (o in una classe esterna) invece di delegare il compito ad EditProduct?
                        Intent intent = new Intent(this, EditProduct.class);
                        intent.putExtra("cartProducts", cartProducts);
                        intent.putExtra("action", "shopping");
                        startActivityForResult(intent, 3);

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            StringBuilder msg = new StringBuilder("Vuoi aggiungere tutti i prodotti presenti nel carrello?");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg.toString())
                    .setTitle("Attenzione")
                    .setPositiveButton("Aggiungi", dialogClickListener)
                    .setNegativeButton("Annulla", dialogClickListener)
                    .show();
        } else
            onBackPressed();
    }

    public void addProduct(View view){
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra("action", "shopping");
        intent.putExtra("pointOfPurchaseId", getIntent().getLongExtra("pointOfPurchaseId",0));
        intent.putExtra("suggestions", listToDisplay);
        startActivityForResult(intent, 1);
    }

    private void updateList(){
        for(int i=0; i<cartProducts.size(); i++)
            System.out.println("cartProducts[" + i + "] = " + cartProducts.get(i).getName());

        listToDisplay = new ArrayList<>(cartProducts);
        quantities = new ArrayList<>();
        for(int i=0; i<listToDisplay.size(); i++){
            int occurences = 1;
            for(int j=0; j<listToDisplay.size(); j++){
                if(i!=j && listToDisplay.get(i).equals(listToDisplay.get(j))){
                    occurences++;
                    listToDisplay.remove(j);
                    j--;
                }
            }
            quantities.add(occurences);
        }

        productsListAdapter = new CartListAdapter(this, R.layout.list_element, listToDisplay, quantities);
        listView.setAdapter(productsListAdapter);

        float total = 0;
        for(int i=0; i<cartProducts.size(); i++) {
            total += cartProducts.get(i).getPrice();
            System.out.println("TOTAL: " + total);
        }

        if(total==0)
            totalPriceText.setText("€0,00");
        else
            totalPriceText.setText("€" + PriceUtils.getFormattedPrice(total));

        if(listView.getAdapter().getCount()==0)
            noProductsWarning.setVisibility(View.VISIBLE);
        else
            noProductsWarning.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                for(int i=0; i<data.getIntExtra("quantity", 1); i++)
                    cartProducts.add((SingleProduct)data.getSerializableExtra("newProduct"));

                updateList();
                Toast.makeText(getApplicationContext(), "Prodotto aggiunto al carrello", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                int newQuantity = data.getIntExtra("quantity", 1);
                int position = data.getIntExtra("position", 0);

                if(newQuantity > quantities.get(position)){
                    for(int i=0; i<newQuantity-quantities.get(position); i++)
                        cartProducts.add(cartProducts.indexOf(listToDisplay.get(position)), listToDisplay.get(position));
                } else if(newQuantity < quantities.get(position)){
                    for(int i=0; i<quantities.get(position) - newQuantity; i++)
                        cartProducts.remove(listToDisplay.get(position));
                }

                for(int i=0; i<cartProducts.size(); i++){
                    if(cartProducts.get(i).equals(listToDisplay.get(position))){
                        cartProducts.remove(i);
                        cartProducts.add(i, (SingleProduct)data.getSerializableExtra("editedProduct"));
                    }
                }

                updateList();
            }
        } else if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}
