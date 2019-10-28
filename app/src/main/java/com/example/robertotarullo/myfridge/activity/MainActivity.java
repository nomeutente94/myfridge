package com.example.robertotarullo.myfridge.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robertotarullo.myfridge.adapter.ProductsListAdapter;
import com.example.robertotarullo.myfridge.bean.Filter;
import com.example.robertotarullo.myfridge.bean.Pack;
import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.comparator.AscendingDateComparator;
import com.example.robertotarullo.myfridge.comparator.ConsumedDiscendingDateComparator;
import com.example.robertotarullo.myfridge.comparator.NameComparator;
import com.example.robertotarullo.myfridge.database.ProductDatabase;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.utils.TextUtils;
import com.example.robertotarullo.myfridge.watcher.QuantityWatcher;

public class MainActivity extends AppCompatActivity {

    // Variabili intent
    public static final String ACTION = "action"; // Tipo di azione
    public static final String PACK = "currentPack"; // Chiave relativa al valore del currentPack

    // Valori restituiti da intent result
    public static final String FILTER = "filter";
    public static final String DELETE = "delete";

    private final int FILTER_INACTIVE_COLOR = Color.parseColor("#d6d8d7");
    private final int FILTER_ACTIVE_COLOR = Color.parseColor("#bcbebd");

    // Tipo di azione
    public enum Action{
        PICK,
        CONSUMED,
        MANAGE,
        PACK
    }

    // Dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    // Variabili per intent
    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int EDIT_PRODUCT_REQUEST = 2;
    private static final int SHOPPING_REQUEST = 3;
    private static final int CONSUMED_REQUEST = 4;
    private static final int MANAGE_REQUEST = 5;
    private static final int PACK_REQUEST = 6;

    // Variabili delle impostazioni
    private static final int DEFAULT_FILTER = 1;

    // Variabili di stato
    private int currentFilter; // Determina la modalità di conservazione corrente

    // Dichiarazione delle liste di prodotti
    private List<Product> displayedProducts = new ArrayList<>(); // Lista di prodotti attualmente su schermo

    // Riferimenti a elementi della view
    private ListView listView;
    private EditText searchBar;

    // Adapter lista
    private ProductsListAdapter productsListAdapter;

    // Elementi view
    private TextView noProductsWarning, resultsCount;

    // Position dell'elemento su cui si è aperto l'ultima volta il popupMenu
    private int currentPopupPosition = -1;

    // Definisce il tipo di lista mostrata, null se default
    private Action action;

    // Rappresenta il pack attualmente mostrato
    // null se action != Action.PACK
    private Pack currentPack;

    // Lista dei filtri
    private List<Filter> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inizializza variabili prese dall'activity chiamante
        action = (Action) getIntent().getSerializableExtra(ACTION);
        currentPack = (Pack) getIntent().getSerializableExtra(PACK);

        // Ottieni riferimenti alle view
        listView = findViewById(R.id.mylistview);
        searchBar = findViewById(R.id.searchBar);
        noProductsWarning = findViewById(R.id.noProductsWarning);
        resultsCount = findViewById(R.id.resultsCount);

        // Stabilisci il comportamento della search bar
        searchBar.addTextChangedListener(new SearchBarWatcher());

        // Setta il filtro prodotti iniziale
        currentFilter = DEFAULT_FILTER; // TODO leggere valore iniziale filtro da impostazioni

        // Stabilisce il titolo dell'activity
        setTitle();

        // Setta il comportamento al click di un elemento
        initializeItemBehaviour();

        // Ottieni un riferimento al DB
        productDatabase = ProductDatabase.getInstance(getApplicationContext());

        if(action==null) {
            new Thread(() -> {
                triggerDbCallbacks();
                runOnUiThread(this::syncFiltersAndProducts); // Procedi solo dopo aver prepopolato i filtri
            }).start();
        } else {
            hideMainViews();
            syncProducts();
        }
    }

    // TODO dummy select per triggerare la callback di prepolazione
    private void triggerDbCallbacks(){
        productDatabase.query("select 1", null); // dummy select
    }

    private void syncFiltersAndProducts(){
        new Thread(() -> {
            filters = productDatabase.filterDao().getFilters();

            runOnUiThread(() -> {
                for(int i=0; i<filters.size(); i++){
                    LinearLayout storageConditionsBlock = findViewById(R.id.storageConditionsBlock);
                    storageConditionsBlock.setWeightSum(filters.size());

                    Button filterButton = new Button(this);
                    filterButton.setLayoutParams(findViewById(R.id.exampleFilterButton).getLayoutParams());
                    filterButton.setVisibility(View.VISIBLE);
                    filterButton.setTag(String.valueOf(i));
                    filterButton.setOnClickListener(v -> setFilter(v));
                    storageConditionsBlock.addView(filterButton);
                    filters.get(i).setButton(filterButton);
                }
                syncProducts();
            });
        }).start();
    }

    // Aggiorna la lista dei prodotti dal DB in base al tipo di action
    private void syncProducts() {
        if (action==null) {
            // Inizializza filtri
            for(int i=0; i<filters.size(); i++) {
                filters.get(i).setProducts(new ArrayList<>());
            }
            new Thread(() -> {
                List<SingleProduct> singleProducts = productDatabase.productDao().getAll(false);
                runOnUiThread(() -> {
                    setNotifications(singleProducts);
                    List<Product> groupedProducts = getGroupedProducts(singleProducts); // Raggruppa i prodotti in pack
                    for (int i = 0; i < groupedProducts.size(); i++) { // Aggiungi i prodotti raggruppati nel rispettivo filtro
                        filters.get(groupedProducts.get(i).getActualStorageCondition()).getProducts().add(groupedProducts.get(i));
                    }
                    for(int i=0; i<filters.size(); i++){
                        sortByAscendingDate(filters.get(i).getProducts()); // TODO controlla prima quale ordinamento utilizzare
                    }
                    setFilter(filters.get(currentFilter).getButton()); // Mostra i prodotti del filtro attuale
                });
            }).start();
        } else if (action==Action.PACK) {
            new Thread(() -> {
                List<SingleProduct> singleProducts = productDatabase.productDao().getAll(false);
                runOnUiThread(() -> {
                    displayedProducts = new ArrayList<>();

                    for(SingleProduct sp: singleProducts){
                        if(sp.packEquals(currentPack.getProducts().get(0)))
                            displayedProducts.add(sp);
                    }

                    if(displayedProducts.size()==0){
                        finish(); // TODO Gestire
                    } else {
                        filterBySearchBar(); // TODO ordina per data di inserimento
                    }
                });
            }).start();
        } else if (action==Action.CONSUMED){
            new Thread(() -> {
                displayedProducts = new ArrayList<>(productDatabase.productDao().getAll(true));
                runOnUiThread(() -> {
                    sortByAscendingDate(displayedProducts);
                    filterBySearchBar();
                });
            }).start();
        } else if (action==Action.PICK || action==Action.MANAGE) {
            new Thread(() -> {
                // List<SingleProduct> singleProducts = productDatabase.productDao().getAll(); TODO prendi catalogo prodotti
                runOnUiThread(() -> {
                    // TODO
                });
            }).start();
        }
    }

    // Cambia il titolo dell'activity in base al parametro action
    private void setTitle(){
        if(action==null)
            setTitle(getString(R.string.activity_title_main_default));
        else if(action==Action.PICK)
            setTitle(getString(R.string.activity_title_main_pick));
        else if(action==Action.CONSUMED)
            setTitle(getString(R.string.activity_title_main_consumed));
        else if(action==Action.MANAGE)
            setTitle(getString(R.string.activity_title_main_manage));
        else if(action == Action.PACK) {
            if(currentPack.getBrand()==null)
                setTitle(getString(R.string.activity_title_main_pack_nobrand, currentPack.getName()));
            else
                setTitle(getString(R.string.activity_title_main_pack, currentPack.getName(), currentPack.getBrand()));
        } else
            setTitle(getString(R.string.activity_title_main_default));
    }

    // Nasconde i pulsanti in basso, i filtri per la modalità di conservazione e il counter di prodotti visualizzati
    private void hideMainViews(){
        findViewById(R.id.buttonPanel).setVisibility(View.GONE); // Nascondi i pulsanti in basso
        findViewById(R.id.storageConditionsBlock).setVisibility(View.GONE); // Nascondi i pulsanti relativi alla modalità di conservazione

        // TODO Rimuovi il margine dalla view contenitore della lista
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.mylistviewBlock).getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        findViewById(R.id.mylistviewBlock).setLayoutParams(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(action==null)
            getMenuInflater().inflate(R.menu.mainactivity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showConsumed:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(ACTION, Action.CONSUMED);
                startActivityForResult(intent, CONSUMED_REQUEST);
                return true;
            case R.id.productsDb:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra(ACTION, Action.MANAGE);
                startActivityForResult(intent, MANAGE_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setFilter(View v) {
        for(int i=0; i<filters.size(); i++)
            filters.get(i).getButton().setBackgroundColor(FILTER_INACTIVE_COLOR); // Setta tutti i pulsanti come inattivi
        v.setBackgroundColor(FILTER_ACTIVE_COLOR); // Setta pulsante come attivo
        clearSearchBarFocus();

        currentFilter = Integer.valueOf(v.getTag().toString()); // Comunica quale filtro si sta utilizzando
        displayedProducts = filters.get(currentFilter).getProducts();
        filterBySearchBar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void resetSearchBar() {
        searchBar.setText(""); // Svuota la barra di ricerca
        clearSearchBarFocus();
    }

    private void clearSearchBarFocus() {
        searchBar.clearFocus(); // Togli il focus alla barra di ricerca
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchBar.getWindowToken(), 0); // Nascondi la tastiera
    }

    // Specifica cosa fare quando l'utente tocca un item della lista
    private void initializeItemBehaviour(){
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product p = (Product)listView.getItemAtPosition(position);

            if(action==null) {
                if (p instanceof Pack) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(ACTION, Action.PACK);
                    intent.putExtra(PACK, (Pack) p);
                    startActivityForResult(intent, PACK_REQUEST);
                } else if (p instanceof SingleProduct) {
                    updateProduct((SingleProduct) p);
                }
            } else if(action==Action.PACK){
                if (p instanceof SingleProduct) {
                    updateProduct((SingleProduct) p);
                } else if (p instanceof Pack) {
                    Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
                }
            } else if(action==Action.PICK){ // TODO
                /*DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent resultIntent = new Intent();

                            SingleProduct clickedProduct = null;
                            if(p instanceof SingleProduct)
                                clickedProduct = (SingleProduct)p;
                            else if(p instanceof Pack)
                                clickedProduct = ((Pack) p).getProducts().get(0);

                            resultIntent.putExtra("productId", clickedProduct.getId());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.dialog_body_main_pick))
                    .setTitle(getString(R.string.dialog_title_warning))
                    .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                    .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                    .show();*/
            } else if(action==Action.MANAGE){ // TODO
                /*if(p instanceof Pack)
                    editPack((Pack) p);
                else if(p instanceof SingleProduct)
                    editSingleProduct((SingleProduct) p);*/
            } else if(action==Action.CONSUMED){
                if (p instanceof SingleProduct) {
                    editProduct(p);
                } else if (p instanceof Pack) {
                    Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onMenuItemClick(MenuItem item){
        Product p = productsListAdapter.getItem(currentPopupPosition);
        if (p instanceof SingleProduct) {
            SingleProduct sp = ((SingleProduct) p);

            switch(item.getItemId()){
                case R.id.updateStateItem:
                    updateProduct(sp);  // Se si è clickato un singleProduct
                    break;
                case R.id.consumeItem:
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        if(which == DialogInterface.BUTTON_POSITIVE){
                            sp.setConsumed(true);
                            sp.setConsumptionDate(new Date()); // Si imposta come data di consumazione l'istante in cui viene confermata la consumazione

                            new Thread(() -> {
                                if (productDatabase.productDao().update(sp) > 0) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(getApplicationContext(), getString(R.string.success_consume), Toast.LENGTH_LONG).show();
                                        syncProducts(); // aggiorna lista
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_generic), Toast.LENGTH_LONG).show());
                                }
                            }).start();
                        }
                    };

                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.dialog_body_main_consume))
                            .setTitle(getString(R.string.dialog_title_consume))
                            .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                            .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                            .show();
                    break;
                case R.id.unconsumeItem:
                    dialogClickListener = (dialog, which) -> {
                        if(which == DialogInterface.BUTTON_POSITIVE){
                            sp.setConsumed(false);
                            sp.setConsumptionDate(null);

                            new Thread(() -> {
                                if (productDatabase.productDao().update(sp) > 0) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(getApplicationContext(), getString(R.string.success_unconsume), Toast.LENGTH_LONG).show();
                                        syncProducts(); // aggiorna lista
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_generic), Toast.LENGTH_LONG).show());
                                }
                            }).start();
                        }
                    };

                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.dialog_body_main_unconsume))
                            .setTitle(getString(R.string.dialog_title_unconsume))
                            .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                            .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                            .show();
                    break;
                case R.id.editItem:
                    editProduct(sp);
                    break;
                case R.id.cloneItem:
                    View cloneDialogView = getLayoutInflater().inflate(R.layout.clone_dialog, null);
                    TextView clonesField = cloneDialogView.findViewById(R.id.quantityField);
                    clonesField.addTextChangedListener(new QuantityWatcher(cloneDialogView.findViewById(R.id.quantityAddButton), cloneDialogView.findViewById(R.id.quantitySubtractButton)));

                    //RadioButton radioButtonFull = cloneDialogView.findViewById(R.id.radio_clone_complete);
                    RadioButton radioButtonPartial = cloneDialogView.findViewById(R.id.radio_clone_partial);
                    RadioButton radioButtonMin = cloneDialogView.findViewById(R.id.radio_clone_min);

                    dialogClickListener = (dialog, which) -> {
                        if(which == DialogInterface.BUTTON_POSITIVE){
                            SingleProduct clonedProduct = new SingleProduct(sp);
                            clonedProduct.setId(0);

                            if(radioButtonPartial.isChecked())
                                clonedProduct.loseConsumptionState();
                            else if(radioButtonMin.isChecked())
                                clonedProduct.loseState();

                            currentFilter = clonedProduct.getActualStorageCondition();

                            List<SingleProduct> productsToClone = new ArrayList<>();
                            for (int i = 0; i < TextUtils.getInt(clonesField); i++)
                                productsToClone.add(clonedProduct);

                            new Thread(() -> {
                                int nonAddedProducts = Collections.frequency(productDatabase.productDao().insertAll(productsToClone), -1);
                                runOnUiThread(() -> {
                                    if(nonAddedProducts==0){
                                        Toast.makeText(getApplicationContext(), getString(R.string.success_clone, productsToClone.size() - nonAddedProducts), Toast.LENGTH_LONG).show();
                                        syncProducts(); // aggiorna lista
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_generic), Toast.LENGTH_LONG).show());
                                    }
                                });
                            }).start();
                        }
                    };

                    new AlertDialog.Builder(this)
                            .setView(cloneDialogView)
                            .setTitle(getString(R.string.dialog_title_clone))
                            .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                            .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                            .show();
                    break;
                case R.id.deleteItem:
                    dialogClickListener = (dialog, which) -> {
                        if(which == DialogInterface.BUTTON_POSITIVE){
                            new Thread(() -> {
                                if (productDatabase.productDao().delete(sp) > 0) {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success_delete), Toast.LENGTH_LONG).show());
                                    syncProducts(); // aggiorna lista
                                } else {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_generic), Toast.LENGTH_LONG).show());
                                }
                            }).start();
                        }
                    };

                    new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.dialog_body_main_delete))
                            .setTitle(getString(R.string.dialog_title_delete))
                            .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                            .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                            .show();
                    break;
            }
        } else if (p instanceof Pack){
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
        }
    }

    // Modifica la quantità dei duplicati tramite i relativi pulsanti
    public void editQuantity(View view) {
        boolean found = false;
        for(int i=0; i<((ViewGroup)view.getParent()).getChildCount() && !found; ++i) {
            View child = ((ViewGroup)view.getParent()).getChildAt(i);
            if(child.getId()==R.id.quantityField && child instanceof TextView){
                found = true;
                TextUtils.editQuantityByButtons((Button)view, (TextView)child, EditProduct.MIN_QUANTITY, EditProduct.MAX_QUANTITY);
            }
        }
    }

    // Setta il comportamento al variare del testo contenuto nella search bar
    public class SearchBarWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            filterBySearchBar();
        }
    }

    // Svuota il contenuto del primo campo di tipo edittext corrispondente al pulsante premuto
    public void clearField(View view) {
        TextUtils.clearField(view);
    }

    // Filtra e mostra i prodotti filtrati in base al testo contenuto nella search bar
    private void filterBySearchBar() {
        List<Product> searchResults = new ArrayList<>();

        if (searchBar.getText().length() > 0) { // Se la barra di ricerca contiene qualcosa
            // Cerca la stringa della barra di ricerca nel nome o la marca dei prodotti
            for (int i = 0; i < displayedProducts.size(); i++) {
                if (displayedProducts.get(i).getName().toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                    searchResults.add(displayedProducts.get(i));
                else if (displayedProducts.get(i).getBrand() != null) {
                    if (displayedProducts.get(i).getBrand().toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                        searchResults.add(displayedProducts.get(i));
                }
            }
            showProducts(searchResults);
        } else // Se la barra di ricerca è vuota resetta la view
            showProducts(displayedProducts);
    }

    // Ritorna una lista di prodotti raggruppati a partire da una di singoli non raggruppati
    // Il criterio di raggruppamento dipende dal tipo di action
    private List<Product> getGroupedProducts(List<SingleProduct> nonGroupedProducts) {
        List<SingleProduct> nonGroupableProducts = new ArrayList<>(nonGroupedProducts);
        List<Pack> groups = new ArrayList<>();

        for (int i = 0; i < nonGroupableProducts.size(); i++) { // Per ogni prodotto
            Pack p = new Pack(); // Crea un nuovo currentPack

            // Cerca un prodotto raggruppabile
            for (int j = i+1; j < nonGroupableProducts.size(); j++) {
                boolean groupable = false;
                if (action == Action.PICK || action == Action.MANAGE)
                    groupable = nonGroupableProducts.get(i).pickEquals(nonGroupableProducts.get(j));
                else if(action == null || action == Action.PACK)
                    groupable = nonGroupableProducts.get(i).packEquals(nonGroupableProducts.get(j));

                // Se raggruppabile sposta il prodotto nel currentPack
                if (groupable) {
                    p.getProducts().add(nonGroupableProducts.get(j));
                    nonGroupableProducts.remove(j);
                    j--;
                }
            }

            // Se è stato trovato qualche raggruppamento si sposta il prodotto iniziale nel currentPack
            if(!p.getProducts().isEmpty()){
                p.getProducts().add(0, nonGroupableProducts.get(i)); // Aggiungi nella prima posizione del currentPack
                nonGroupableProducts.remove(i); // Rimuovi dalla lista
                i--; // Aggiorna indice lista
                groups.add(p); // Aggiungi il currentPack alla lista
            }
        }

        List<Product> groupedProducts = new ArrayList<>(groups);
        groupedProducts.addAll(nonGroupableProducts);

        return groupedProducts;
    }

    private void setNotifications(List<SingleProduct> nonGroupedProducts){

        // Inizializza notifiche per ogni filtro
        for(int i=0; i<filters.size(); i++){
            filters.get(i).setNotificationCount(0);
        }

        // Aggiungi eventuali notifiche sui filtri per prodotti in scadenza/scaduti
        for (int i = 0; i < nonGroupedProducts.size(); i++) { // Per ogni prodotto
            Date expiryDate = DateUtils.getActualExpiryDate(nonGroupedProducts.get(i));
            if (expiryDate != null && !expiryDate.equals(DateUtils.getNoExpiryDate())) {
                Date now = DateUtils.getCurrentDateWithoutTime();
                if (expiryDate.before(now) || expiryDate.equals(now)) {
                    Filter filter = filters.get(nonGroupedProducts.get(i).getActualStorageCondition());
                    filter.setNotificationCount(filter.getNotificationCount() + 1);
                }
            }
        }

        // Aggiorna notifica sui pulsanti dei filtri
        for(int i=0; i<filters.size(); i++){
            Button filterButton = filters.get(i).getButton();
            if(filters.get(i).getNotificationCount()>0)
                filterButton.setText(getString(R.string.filter_name_notify, filters.get(i).getName(), filters.get(i).getNotificationCount()));
            else
                filterButton.setText(getString(R.string.filter_name, filters.get(i).getName()));
        }
    }

    // Aggiunge adapter con i prodotti ricevuti e aggiorna warning
    private void showProducts(List<Product> productsToShow) {
        productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, productsToShow, action==Action.CONSUMED, action); // Crea adapter
        listView.setAdapter(productsListAdapter); // Mostra la lista

        if(action != null) {
            resultsCount.setVisibility(View.GONE);
        }

        if (listView.getAdapter().getCount() == 0) {
            noProductsWarning.setVisibility(View.VISIBLE);
            if(action==null) {
                resultsCount.setVisibility(View.GONE);
            }
        } else {
            noProductsWarning.setVisibility(View.GONE);
            if(action==null)
                resultsCount.setVisibility(View.VISIBLE);

            resultsCount.setText(getString(R.string.status, productsListAdapter.getCount())); // Aggiorna count
        }
    }

    // Avvia l'activity EditProduct per l'aggiunta (legacy)
    public void addProduct(View view) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra(EditProduct.ACTION, EditProduct.Action.ADD);
        intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.DEFAULT);
        intent.putExtra(EditProduct.FILTER, currentFilter);
        startActivityForResult(intent, ADD_PRODUCT_REQUEST);
    }

    // TODO Avvia l'activity EditProduct per l'aggiunta (new)
    public void addProductWithoutConsumption(View view) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra(EditProduct.ACTION, EditProduct.Action.ADD);
        intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.NO_CONSUMPTION);
        intent.putExtra(EditProduct.FILTER, currentFilter);
        startActivityForResult(intent, ADD_PRODUCT_REQUEST);
    }

    // Avvia l'activity AddPointOfPurchase
    public void addPointOfPurchase(View view) {
        Intent intent = new Intent(this, AddPointOfPurchase.class);
        startActivity(intent);
    }

    /*TODO // Avvia l'activity EditProduct per la modifica
    public void editPack(Pack p) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra("action", EditProduct.Action.EDIT_PACK);
        if(action==Action.MANAGE)
            intent.putExtra("actionType", EditProduct.ActionType.MANAGE);
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
    }*/

    // Avvia l'activity EditProduct per la modifica
    public void editProduct(Product p) {
        if(p instanceof SingleProduct){
            Intent intent = new Intent(this, EditProduct.class);
            intent.putExtra(EditProduct.ID, ((SingleProduct)p).getId()); // TODO Passare l'intero prodotto
            //intent.putExtra("filter", currentFilter);
            intent.putExtra(EditProduct.ACTION, EditProduct.Action.EDIT);
            if(action==Action.CONSUMED)
                intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.CONSUMED);
            else if(action==Action.MANAGE)
                intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.MANAGE);
            else if(action==null || action==Action.PACK)
                intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.DEFAULT);
            startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
        } else {
           Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show(); // TODO sostituire tutti gli errori generici
        }
    }

    public void updateProduct(SingleProduct p) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra(EditProduct.ID, p.getId());
        intent.putExtra(EditProduct.ACTION, EditProduct.Action.EDIT);
        intent.putExtra(EditProduct.ACTION_TYPE, EditProduct.ActionType.UPDATE);
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
    }

    public void startShoppingMode(View view) {
        Intent intent = new Intent(this, ShoppingForm.class);
        startActivityForResult(intent, SHOPPING_REQUEST);
    }

    // Ordina dalla data più recente alla più lontana
    private void sortByAscendingDate(List<Product> productsToSort) {
        if(action==Action.CONSUMED)
            Collections.sort(productsToSort, new ConsumedDiscendingDateComparator());
        else
            Collections.sort(productsToSort, new AscendingDateComparator());
    }

    private void sortByName(List<Product> productsToSort) {
        Collections.sort(productsToSort, new NameComparator());
    }

    public void showPopup(View v) {
        currentPopupPosition = Integer.parseInt(v.getTag().toString());
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.element_options, popup.getMenu());

        if(action==null || action==Action.PACK){
            popup.getMenu().findItem(R.id.unconsumeItem).setVisible(false);
            //popup.getMenu().findItem(R.id.deleteItem).setVisible(false); // TODO nascondere elimina nella schermata principale?
        } else if(action==Action.MANAGE){
            popup.getMenu().findItem(R.id.consumeItem).setVisible(false);
            popup.getMenu().findItem(R.id.updateStateItem).setVisible(false);
            popup.getMenu().findItem(R.id.cloneItem).setVisible(false);
            popup.getMenu().findItem(R.id.unconsumeItem).setVisible(false);
        } else if(action==Action.CONSUMED){
            popup.getMenu().findItem(R.id.consumeItem).setVisible(false);
            popup.getMenu().findItem(R.id.updateStateItem).setVisible(false);
        }
        popup.show();
    }

    // Aggiorna la lista rispecchiando le eventuali modifiche applicate dalle altre activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                currentFilter = data.getIntExtra(FILTER, currentFilter);
                syncProducts();
            }
        } else if (requestCode == EDIT_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (!data.getBooleanExtra(DELETE, false)) { // Se il prodotto è stato modificato e non eliminato
                    currentFilter = data.getIntExtra(FILTER, currentFilter);
                }
                syncProducts();
            }
        } else if (requestCode == SHOPPING_REQUEST) {
            if (resultCode == RESULT_OK) {
                syncProducts();
            }
        } else if (requestCode == CONSUMED_REQUEST) {
            syncProducts(); // TODO chiamare solo in caso di modifica

            if (resultCode == RESULT_OK) {

            }
        } else if (requestCode == MANAGE_REQUEST) {
            syncProducts(); // TODO chiamare solo in caso di modifica

            if (resultCode == RESULT_OK) {

            }
        } else if(requestCode == PACK_REQUEST){
            syncProducts(); // TODO chiamare solo in caso di modifica

            if (resultCode == RESULT_OK) {

            }
        }
    }
}
