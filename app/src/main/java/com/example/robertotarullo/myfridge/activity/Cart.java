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
import com.example.robertotarullo.myfridge.database.DatabaseUtils;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.utils.PriceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Cart extends AppCompatActivity {

    private static final int ADD_REQUEST = 1;
    private static final int EDIT_REQUEST = 2;

    // Variabili per intent
    public static final String POINT_OF_PURCHASE_ID = "pointOfPurchaseId";

    public static final String QUANTITY = "quantity";
    public static final String NEW_PRODUCT = "newProduct";
    public static final String EDITED_PRODUCT = "editedProduct";



    // Liste utilizzate
    private ArrayList<SingleProduct> cartProducts = new ArrayList<>(); // Lista dei prodotti inseriti nel carrello
    private ArrayList<Integer> quantities = new ArrayList<>(); // Quantità relativa a ogni prodotto nel carrello // TODO crea classe coppia prodotto quantità
    private ArrayList<SingleProduct> listToDisplay = new ArrayList<>(); // Lista mostrata all'utente

    // Riferimenti a elementi della view
    private ListView listView;
    private TextView totalPriceText;
    private TextView noProductsWarning;

    // Adapter lista
    private CartListAdapter productsListAdapter;

    // Attributi comuni a tutti i prodotti del carrello
    private long pointOfPurchaseId;
    private Date purchaseDate;

    private int lastEditPosition = -1; // Posizione dell'ultimo prodotto di cui si è aperta la modifica

    @Override
    // Gestisci il comportamento alla pressione del tasto indietro
    public void onBackPressed() {

        // Se il carrello non è vuoto mostra avviso prima di continuare
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
            builder.setMessage(getString(R.string.dialog_body_cart_back))
                   .setTitle(getString(R.string.dialog_title_warning))
                   .setPositiveButton(getString(R.string.dialog_button_exit), dialogClickListener)
                   .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                   .show();

        // Se il carrello è vuoto esci senza alcun avviso
        } else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_summary);
        setTitle(getString(R.string.activity_title_cart));

        // Ottieni riferimenti alle view
        listView = findViewById(R.id.mylistview);
        noProductsWarning = findViewById(R.id.noProductsWarning);
        totalPriceText = findViewById(R.id.totalPriceText);

        // Inizializzazione attributi comuni a tutti i prodotti del carrello
        pointOfPurchaseId = getIntent().getLongExtra(POINT_OF_PURCHASE_ID,0);
        purchaseDate = DateUtils.getCurrentDateWithoutTime();

        // Setta il comportamento al click di un elemento in lista
        listView.setOnItemClickListener((parent, view, position, id) -> editSingleProduct(position));
    }

    // Avvia l'activity EditProduct per la modifica
    public void editSingleProduct(int position){
        lastEditPosition = position;
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra(EditProduct.ACTION, EditProduct.Action.EDIT);
        intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.SHOPPING);
        intent.putExtra(EditProduct.QUANTITY, quantities.get(position));
        intent.putExtra(EditProduct.PRODUCT_TO_EDIT, (SingleProduct)listView.getItemAtPosition(position));
        intent.putExtra(EditProduct.SUGGESTIONS, listToDisplay);
        startActivityForResult(intent, EDIT_REQUEST);
    }

    public void deleteProduct(View view){
        SingleProduct p = productsListAdapter.getItem(Integer.parseInt(view.getTag().toString()));
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    cartProducts.removeAll(Collections.singleton(p)); // Rimuovi tutte le occorrenze del prodotto
                    updateList(); // Aggiorna la lista da visualizzare // TODO aggiornare ad ogni modifica di cartProducts? (listener)
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_delete_success), Toast.LENGTH_LONG).show();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        String msg = getString(R.string.dialog_body_cart_delete);
        if(Collections.frequency(cartProducts, p)>1)
            msg = String.format(getString(R.string.dialog_body_cart_multipledelete), Collections.frequency(cartProducts, p));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle(getString(R.string.dialog_title_delete))
                .setPositiveButton(getString(R.string.dialog_button_remove), dialogClickListener)
                .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                .show();
    }

    public void onConfirmButtonClick(View view) {

        // Se il carrello non è vuoto termina la spesa chiedendo conferma di aggiunta
        if(cartProducts.size()>0){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    // Inserisci purchaseDate e pointOfPurchaseId per tutti i prodotti
                    for (int i = 0; i < cartProducts.size(); i++) {
                        cartProducts.get(i).setPurchaseDate(purchaseDate);
                        cartProducts.get(i).setPointOfPurchaseId(pointOfPurchaseId);
                    }

                    // Inserimento dei prodotti dal carrello al database
                    new Thread(() -> {
                        if (Collections.frequency(DatabaseUtils.getDatabase(this).productDao().insertAll(cartProducts), -1) == 0)
                            finish();
                        else
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.toast_error_insert_cart), Toast.LENGTH_LONG).show());
                    }).start();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.dialog_body_cart_add))
                    .setTitle(getString(R.string.dialog_title_warning))
                    .setPositiveButton(getString(R.string.dialog_button_add), dialogClickListener)
                    .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                    .show();

        // Se il carrello è vuoto termina la spesa senza fare nulla
        } else
            onBackPressed();
    }

    // Mostra il form per inserire un nuovo prodotto nel carrello
    public void addProduct(View view){
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra(EditProduct.ACTION, EditProduct.Action.ADD);
        intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.SHOPPING);
        intent.putExtra(EditProduct.SUGGESTIONS, listToDisplay);
        startActivityForResult(intent, ADD_REQUEST);
    }

    // Aggiorna la lista da mostrare all'utente
    private void updateList(){
        listToDisplay = new ArrayList<>(cartProducts);
        quantities = new ArrayList<>();

        for(int i=0; i<listToDisplay.size(); i++){
            int occurences = 1;
            for(int j=i+1; j<listToDisplay.size(); j++){
                if(listToDisplay.get(i).equals(listToDisplay.get(j))){
                    occurences++;
                    listToDisplay.remove(j);
                    j--;
                }
            }
            quantities.add(occurences);
        }

        productsListAdapter = new CartListAdapter(this, R.layout.list_cart_element, listToDisplay, quantities);
        listView.setAdapter(productsListAdapter);

        float total = 0;
        for(int i=0; i<cartProducts.size(); i++) {
            total += cartProducts.get(i).getPrice();
        }

        // Aggiorna il campo prezzo
        if(total==0)
            totalPriceText.setText("€0,00");
        else
            totalPriceText.setText("€" + PriceUtils.getFormattedPrice(total));

        // Aggiorna la visibilità di noProductsWarning
        if(listView.getAdapter().getCount()==0)
            noProductsWarning.setVisibility(View.VISIBLE);
        else
            noProductsWarning.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_REQUEST) {
            if (resultCode == RESULT_OK) {
                for(int i=0; i<data.getIntExtra(QUANTITY, 1); i++)
                    cartProducts.add((SingleProduct)data.getSerializableExtra(NEW_PRODUCT));
                updateList();
            }
        } else if (requestCode == EDIT_REQUEST) {
            if (resultCode == RESULT_OK) {
                int newQuantity = data.getIntExtra(QUANTITY, 1);
                int oldQuantity = quantities.get(lastEditPosition);
                SingleProduct oldProduct = listToDisplay.get(lastEditPosition);

                if(newQuantity > oldQuantity){
                    for(int i=0; i < newQuantity-oldQuantity; i++)
                        cartProducts.add(cartProducts.indexOf(oldProduct), oldProduct);
                } else if(newQuantity < oldQuantity){
                    for(int i=0; i < oldQuantity-newQuantity; i++)
                        cartProducts.remove(oldProduct);
                }

                for(int i=0; i<cartProducts.size(); i++){
                    if(cartProducts.get(i).equals(oldProduct)){
                        cartProducts.set(i, (SingleProduct)data.getSerializableExtra(EDITED_PRODUCT));
                    }
                }
                updateList();
            }
        }
    }
}
