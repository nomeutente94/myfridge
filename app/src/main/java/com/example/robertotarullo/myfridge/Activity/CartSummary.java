package com.example.robertotarullo.myfridge.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Adapter.CartListAdapter;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.R;

import java.util.ArrayList;
import java.util.List;

public class CartSummary extends AppCompatActivity {

    private List<SingleProduct> cartProducts;

    // Riferimenti a elementi della view
    private ListView listView;

    // Adapter lista
    private CartListAdapter productsListAdapter;
    private TextView noProductsWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_summary);
        setTitle("Riepilogo carrello");

        // Ottieni riferimenti alle view
        listView = findViewById(R.id.mylistview);
        noProductsWarning = findViewById(R.id.noProductsWarning);

        cartProducts = new ArrayList<>(); // TODO prendi la lista dei prodotti nel carrello
        productsListAdapter = new CartListAdapter(this, R.layout.list_element, cartProducts);
        setAdapter(productsListAdapter);

        // Setta il comportamento al click di un elemento
        listView.setOnItemClickListener((parent, view, position, id) -> editSingleProduct((SingleProduct)listView.getItemAtPosition(position)));
    }

    private void setAdapter(CartListAdapter adapter){
        listView.setAdapter(adapter);
        updateNoProductsWarning();
    }

    private void updateNoProductsWarning(){
        if(listView.getAdapter().getCount()==0)
            noProductsWarning.setVisibility(View.VISIBLE);
        else
            noProductsWarning.setVisibility(View.GONE);
    }

    // Avvia l'activity AddProduct per la modifica
    public void editSingleProduct(SingleProduct p){
        /* TODO modifica prodotto con AddProduct con i campi di action "shopping" e compilando i campi col prodotto cliccato
        Intent intent = new Intent(this, AddProduct.class);
        intent.putExtra("action", "edit");
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
        */
    }
}
