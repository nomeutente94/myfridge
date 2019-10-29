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
import com.example.robertotarullo.myfridge.database.ProductDatabase;
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

    // Variabili per intent (onActivityResult)
    public static final String QUANTITY = "quantity"; // Chiave della quantità del prodotto aggiunto/modificato
    public static final String NEW_PRODUCT = "newProduct"; // Chiave del nuovo prodotto
    public static final String EDITED_PRODUCT = "editedProduct"; // Chiave del prodotto modificato

    // Liste utilizzate
    private ArrayList<SingleProduct> cartProducts = new ArrayList<>(); // Lista dei prodotti inseriti nel carrello
    private ArrayList<CartProduct> cartProductsToDisplay = new ArrayList<>(); // Lista mostrata all'utente

    // Riferimenti a elementi della view
    private ListView listView; // View della lista di prodotti attualmente presenti nel carrello
    private TextView totalPriceText; // View del campo prezzo totale
    private TextView noProductsWarning;

    // Adapter lista
    private CartListAdapter productsListAdapter;

    // Attributi comuni a tutti i prodotti del carrello
    private long pointOfPurchaseId;
    private Date purchaseDate;

    private int lastEditPosition = -1; // Posizione dell'ultimo prodotto di cui si è aperta la schermata di modifica

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
        intent.putExtra(EditProduct.QUANTITY, cartProductsToDisplay.get(position).quantity);
        intent.putExtra(EditProduct.PRODUCT_TO_EDIT, cartProductsToDisplay.get(position).product);
        intent.putExtra(EditProduct.SUGGESTIONS, cartProducts);
        startActivityForResult(intent, EDIT_REQUEST);
    }

    // Elimina la voce relativa al pulsante premuto
    public void deleteProduct(View view){
        CartProduct cartProduct = productsListAdapter.getItem(Integer.parseInt(view.getTag().toString()));

        if(cartProduct!=null){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        cartProducts.removeAll(Collections.singleton(cartProduct.getProduct())); // Rimuovi tutte le occorrenze del prodotto
                        updateList(); // Aggiorna la lista da visualizzare
                        Toast.makeText(getApplicationContext(), getString(R.string.success_delete), Toast.LENGTH_LONG).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            String msg = getString(R.string.dialog_body_cart_delete);
            if (cartProduct.getQuantity() > 1)
                msg = String.format(getString(R.string.dialog_body_cart_multipleDelete), cartProduct.getQuantity());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg)
                    .setTitle(getString(R.string.dialog_title_delete))
                    .setPositiveButton(getString(R.string.dialog_button_remove), dialogClickListener)
                    .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                    .show();
        } else
            Toast.makeText(this, getString(R.string.error_productNotFoundInList), Toast.LENGTH_LONG).show();
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
                        if (Collections.frequency(ProductDatabase.getInstance(this).productDao().insertAll(cartProducts), -1) == 0)
                            finish();
                        else
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_insert_cart), Toast.LENGTH_LONG).show());
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
        intent.putExtra(EditProduct.SUGGESTIONS, cartProducts);
        startActivityForResult(intent, ADD_REQUEST);
    }

    // Aggiorna la lista da mostrare all'utente
    private void updateList(){
        float total = 0;
        cartProductsToDisplay = new ArrayList<>(); // Resetta la lista dei prodotti da mostrare

        // Controlla che il prodotto non sia stato già inserito nella lista dei prodotti da mostrare
        // In caso positivo si aggiunge con le relative occorreze, altrimenti lo si ignora
        for(int i=0; i<cartProducts.size(); i++){
            total += cartProducts.get(i).getPrice();
            boolean alreadyFound = false;
            for(int j=0; j<cartProductsToDisplay.size() && !alreadyFound; j++){
                if(cartProducts.get(i).equals(cartProductsToDisplay.get(j).product))
                    alreadyFound = true;
            }
            if(!alreadyFound)
                cartProductsToDisplay.add(new CartProduct(cartProducts.get(i), Collections.frequency(cartProducts, cartProducts.get(i))));
        }

        // Mostra la lista a schermo
        productsListAdapter = new CartListAdapter(this, R.layout.list_cart_element, cartProductsToDisplay);
        listView.setAdapter(productsListAdapter);

        // Aggiorna il campo prezzo
        if(total==0)
            totalPriceText.setText(getString(R.string.text_cart_totalPrice_empty));
        else
            totalPriceText.setText(getString(R.string.text_cart_totalPrice,PriceUtils.getFormattedPrice(total)));

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

                // Aggungi il nuovo prodotto per il numero di volte specificato
                for(int i=0; i<data.getIntExtra(QUANTITY, SingleProduct.DEFAULT_PIECES); i++)
                    cartProducts.add((SingleProduct)data.getSerializableExtra(NEW_PRODUCT));
                updateList();
            }
        } else if (requestCode == EDIT_REQUEST) {
            if (resultCode == RESULT_OK) {
                int newQuantity = data.getIntExtra(QUANTITY, SingleProduct.DEFAULT_PIECES);
                int oldQuantity = cartProductsToDisplay.get(lastEditPosition).getQuantity();
                SingleProduct oldProduct = cartProductsToDisplay.get(lastEditPosition).getProduct();

                // Aggiungi o rimuovi prodotti nella loro posizione
                if(newQuantity > oldQuantity){
                    for(int i=0; i < newQuantity-oldQuantity; i++)
                        cartProducts.add(cartProducts.indexOf(oldProduct), oldProduct);
                } else if(newQuantity < oldQuantity){
                    for(int i=0; i < oldQuantity-newQuantity; i++)
                        cartProducts.remove(oldProduct);
                }

                // Applica le eventuali modifiche a tutti i prodotti
                for(int i=0; i<cartProducts.size(); i++){
                    if(cartProducts.get(i).equals(oldProduct)){
                        cartProducts.set(i, (SingleProduct)data.getSerializableExtra(EDITED_PRODUCT));
                    }
                }

                updateList();
            }
        }
    }

    // Rappresenta l'oggetto 'prodotto' nel carrello, con la relativa quantità
    public class CartProduct {
        private SingleProduct product;
        private int quantity;

        public SingleProduct getProduct(){
            return product;
        }

        public int getQuantity(){
            return quantity;
        }

        private CartProduct(SingleProduct product, int quantity){
            this.product = product;
            this.quantity = quantity;
        }
    }
}
