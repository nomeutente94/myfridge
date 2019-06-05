package com.example.robertotarullo.myfridge.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.CartListAdapter;
import com.example.robertotarullo.myfridge.Bean.Product;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.PriceUtils;

import java.util.ArrayList;

public class Cart extends AppCompatActivity {

    private ArrayList<SingleProduct> cartProducts;

    // Riferimenti a elementi della view
    private ListView listView;
    private TextView totalPriceText;

    // Adapter lista
    private CartListAdapter productsListAdapter;
    private TextView noProductsWarning;

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("cartProducts", cartProducts);
        setResult(RESULT_OK, resultIntent);
        finish();
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

        cartProducts = (ArrayList<SingleProduct>) getIntent().getSerializableExtra("cartProducts");
        updateList();

        // Setta il comportamento al click di un elemento
        listView.setOnItemClickListener((parent, view, position, id) -> editSingleProduct(position));
    }

    // Avvia l'activity AddProduct per la modifica
    public void editSingleProduct(int position){
        // TODO modifica prodotto con AddProduct con i campi di action "shopping" e compilando i campi col prodotto cliccato
        Intent intent = new Intent(this, AddProduct.class);
        intent.putExtra("action", "shopping");
        intent.putExtra("position", position);
        intent.putExtra("productToEdit", (SingleProduct)listView.getItemAtPosition(position));
        startActivityForResult(intent, 1);
    }

    public void deleteProduct(View view){
        int position = Integer.parseInt(view.getTag().toString());
        Product p = productsListAdapter.getItem(position);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    cartProducts.remove(p);
                    updateList();
                    Toast.makeText(getApplicationContext(), "Prodotto rimosso", Toast.LENGTH_LONG).show();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Rimuovere il prodotto dal carrello?")
                .setTitle("Conferma eliminazione")
                .setPositiveButton("Rimuovi", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    public void onConfirmButtonClick(View view) {
        if(cartProducts.size()>0){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // TODO aggiungere qui i prodotti invece di delegare il compito ad AddProduct?
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("cartProducts", cartProducts);
                        resultIntent.putExtra("action", "shopping");
                        resultIntent.putExtra("cartEdit", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
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
        }
    }

    private void updateList(){
        productsListAdapter = new CartListAdapter(this, R.layout.list_element, cartProducts);
        listView.setAdapter(productsListAdapter);

        int total = 0;
        for(int i=0; i<cartProducts.size(); i++)
            total += cartProducts.get(i).getPrice();

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
                int index = data.getIntExtra("position", 0);
                cartProducts.remove(index);
                cartProducts.add(index, (SingleProduct)data.getSerializableExtra("editedProduct"));
                updateList();
            }
        }
    }
}
