package com.example.robertotarullo.myfridge.activity;

import java.util.ArrayList;
import java.util.Collections;
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
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robertotarullo.myfridge.adapter.ProductsListAdapter;
import com.example.robertotarullo.myfridge.bean.Pack;
import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.comparator.AscendingDateComparator;
import com.example.robertotarullo.myfridge.comparator.ConsumedDiscendingDateComparator;
import com.example.robertotarullo.myfridge.database.DatabaseUtils;
import com.example.robertotarullo.myfridge.database.ProductDatabase;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.utils.TextUtils;
import com.example.robertotarullo.myfridge.watcher.QuantityWatcher;

public class MainActivity extends AppCompatActivity {

    // Dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    // Variabili per intent
    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int EDIT_PRODUCT_REQUEST = 2;
    private static final int SHOPPING_REQUEST = 3;

    // Variabili di stato
    private int currentFilter; // Determina la modalità di conservazione corrente
    private Pack currentPackage; // Riferimento alla confezione correntemente visualizzata, null se non si sta visualizzando una confezione
    private boolean showConsumedProducts; // Determina se mostrare i prodotti consumati

    // Dichiarazione delle liste di prodotti
    private List<SingleProduct> singleProducts; // Lista di tutti i prodotti
    private List<Product> groupedProducts; // Lista di tutti i prodotti della view (pack e singleProduct)
    private List<Product> filteredProducts; // Lista di prodotti della modalità di conservazione corrente (pack e singleProduct)
    private List<Product> packProducts; // Lista di prodotti della confezione corrente (solo singleProduct)

    // Riferimenti a elementi della view
    private ListView listView;
    private EditText searchBar;
    private Button filterButton0, filterButton1, filterButton2;

    // Adapter lista
    private ProductsListAdapter productsListAdapter;
    private TextView noProductsWarning;

    // Position dell'elemento su cui si è aperto l'utlimo popupmenu
    private int currentPopupPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ottieni un riferimento al DB
        productDatabase = DatabaseUtils.getDatabase(getApplicationContext());

        // Ottieni riferimenti alle view
        listView = findViewById(R.id.mylistview);
        searchBar = findViewById(R.id.searchBar);
        filterButton0 = findViewById(R.id.StorageConditionFilterButton0);
        filterButton1 = findViewById(R.id.StorageConditionFilterButton1);
        filterButton2 = findViewById(R.id.StorageConditionFilterButton2);
        noProductsWarning = findViewById(R.id.noProductsWarning);

        // Inizializza la search bar
        searchBar.addTextChangedListener(new SearchBarWatcher());

        // Setta il filtro prodotti iniziale
        currentFilter = 1; // TODO leggere valore iniziale filtro da impostazioni
        pressOnFilter(filterButton1);

        // Inizializza la lista leggendo dal DB
        retrieveProductsFromDB(null);

        // Setta il comportamento al click di un elemento
        initializeItemBehaviour();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (showConsumedProducts)
            menu.add(0, R.id.showConsumed, Menu.NONE, "Mostra non consumati");
        else
            menu.add(0, R.id.showConsumed, Menu.NONE, "Mostra consumati");
        return super.onPrepareOptionsMenu(menu);
    }

    // Usare questa chiamata per cambiare scheda e NON setFilterView direttamente
    public void setFilteredProducts(View v) {
        pressOnFilter((Button) v); // Cambia il colore del filtro attuale
        clearSerchBarFocus();
        setFilterView(Integer.valueOf(v.getTag().toString())); // Filtra la lista dei prodotti in base al filtro selezionato
    }

    private void pressOnFilter(Button b) {
        if (b == null) {
            if (currentFilter == 0)
                b = filterButton0;
            else if (currentFilter == 1)
                b = filterButton1;
            else if (currentFilter == 2)
                b = filterButton2;
        }

        // Resetta i colori dei filtri
        filterButton0.setBackgroundColor(Color.parseColor("#d6d8d7"));
        filterButton1.setBackgroundColor(Color.parseColor("#d6d8d7"));
        filterButton2.setBackgroundColor(Color.parseColor("#d6d8d7"));
        // Cambia il colore del filtro attuale
        b.setBackgroundColor(Color.parseColor("#bcbebd"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showConsumed:
                showConsumedProducts = !showConsumedProducts;
                groupProducts(null);
                filterBySearchBar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentPackage != null) {
            resetSearchBar();
            setFilterView(currentFilter); // TODO Usare setFilteredProducts per cambiare scheda e NON setFilterView direttamente ?
        } else
            super.onBackPressed();
    }

    private void resetSearchBar() {
        searchBar.setText(""); // Svuota la barra di ricerca
        clearSerchBarFocus();
    }

    private void clearSerchBarFocus() {
        searchBar.clearFocus(); // Togli il focus alla barra di ricerca
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchBar.getWindowToken(), 0); // Nascondi la tastiera
    }

    // Specifica cosa fare quando l'utente tocca un item della lista
    private void initializeItemBehaviour(){
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // TODO codice ripetuto con editProduct()
            Product p = (Product)listView.getItemAtPosition(position);

            if(p instanceof Pack && currentPackage==null) { // Se si è clickato un Pack
                resetSearchBar();
                setPackageView((Pack) p);
            } else if(!p.isConsumed())                      // Se si è clickato un SingleProduct
                updateProduct((SingleProduct)p);
            else
                Toast.makeText(getApplicationContext(), "Non puoi aggiornare lo stato di un prodotto consumato!", Toast.LENGTH_LONG).show();
        });
    }

    // Svuota il testo contenuto nella search bar
    public void clearField(View view) {
        TextUtils.clearField(view);
    }

    public void editProduct(MenuItem item){
        Product p = (Product) listView.getItemAtPosition(currentPopupPosition);

        if (p instanceof Pack && currentPackage == null) {  // Se si è clickato un pack
            resetSearchBar();
            setPackageView((Pack) p);
        } else                                              // Se si è clickato un singleProduct
            editSingleProduct((SingleProduct) p);
    }

    public void consumeProduct(MenuItem item) {
        int position = currentPopupPosition;
        Product p = productsListAdapter.getItem(position);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(() -> {
                        if (p instanceof Pack) {
                            /*
                            // rendere operazione atomica
                            boolean deleteOk = true;
                            for(int i=0; i<((Pack)p).getProducts().size(); i++){
                                if (productDatabase.productDao().updateConsumption(((Pack)p).getProducts().get(i).getId(), true) <= 0)
                                    deleteOk = false;
                            }

                            runOnUiThread(() -> {
                                if(deleteOk)
                                    Toast.makeText(getApplicationContext(), "Errore nella consumazione di uno o più prodotti", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Confezione settata come consumata", Toast.LENGTH_LONG).show();
                                retrieveProductsFromDB(null); // aggiorna lista
                            });
                            */
                        } else {
                            SingleProduct sp = ((SingleProduct) p);
                            sp.setConsumed(true);
                            sp.setConsumptionDate(DateUtils.getCurrentDateWithoutTime());

                            if (productDatabase.productDao().update(sp) > 0) {
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Prodotto settato come consumato", Toast.LENGTH_LONG).show();
                                    retrieveProductsFromDB(null); // aggiorna lista
                                });
                            }
                        }
                    }).start();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        String msg;
        if (p instanceof Pack)
            msg = "Vuoi segnare come consumati tutti i prodotti di tipo \"" + p.getName() + "\"?";
        else
            msg = "Vuoi segnare come consumato il prodotto \"" + p.getName() + "\"?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Conferma consumazione")
                .setPositiveButton("Conferma", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    // TODO parametrizzare e mettere a fattor comune con consumeProduct
    public void unconsumeProduct(MenuItem item) {
        int position = currentPopupPosition;
        Product p = productsListAdapter.getItem(position);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(() -> {
                        if (p instanceof Pack) {
                            /*
                            // rendere operazione atomica
                            boolean deleteOk = true;
                            for(int i=0; i<((Pack)p).getProducts().size(); i++){
                                if (productDatabase.productDao().updateConsumption(((Pack)p).getProducts().get(i).getId(), true) <= 0)
                                    deleteOk = false;
                            }

                            runOnUiThread(() -> {
                                if(deleteOk)
                                    Toast.makeText(getApplicationContext(), "Errore nella consumazione di uno o più prodotti", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Confezione settata come consumata", Toast.LENGTH_LONG).show();
                                retrieveProductsFromDB(null); // aggiorna lista
                            });
                            */
                        } else {
                            SingleProduct sp = ((SingleProduct) p);
                            sp.setConsumed(false);
                            sp.setConsumptionDate(null);

                            if (productDatabase.productDao().update(sp) > 0) {
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Prodotto settato come non consumato", Toast.LENGTH_LONG).show();
                                    retrieveProductsFromDB(null); // aggiorna lista
                                });
                            }
                        }
                    }).start();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        String msg;
        if (p instanceof Pack)
            msg = "Vuoi settare come non consumati tutti i prodotti di tipo \"" + p.getName() + "\"?";
        else
            msg = "Vuoi settare come non consumato il prodotto \"" + p.getName() + "\"?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Conferma ripristino consumazione")
                .setPositiveButton("Conferma", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    // TODO parametrizzare e mettere a fattor comune con onOptionsItemSelected case R.id.delete dell'activity EditProduct
    public void deleteProduct(MenuItem item) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(() -> {
                        if (productDatabase.productDao().delete((SingleProduct)productsListAdapter.getItem(currentPopupPosition)) > 0) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("delete", true);
                            setResult(RESULT_OK, resultIntent);
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Prodotto eliminato", Toast.LENGTH_LONG).show());
                            retrieveProductsFromDB(null); // aggiorna lista
                        }
                    }).start();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        String msg = "Vuoi eliminare definitivamente il prodotto?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Conferma eliminazione")
                .setPositiveButton("Elimina", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    // TODO codice uguale a onConfirmButtonClick case "add"
    public void cloneProduct(MenuItem item) {
        View cloneDialogView = getLayoutInflater().inflate(R.layout.clone_dialog, null);

        TextView clonesField = cloneDialogView.findViewById(R.id.quantityField);
        clonesField.addTextChangedListener(new QuantityWatcher(cloneDialogView.findViewById(R.id.quantityAddButton), cloneDialogView.findViewById(R.id.quantitySubtractButton)));

        //RadioButton radioButtonFull = cloneDialogView.findViewById(R.id.radio_clone_complete);
        RadioButton radioButtonPartial = cloneDialogView.findViewById(R.id.radio_clone_partial);
        RadioButton radioButtonMin = cloneDialogView.findViewById(R.id.radio_clone_min);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    SingleProduct clonedProduct = (SingleProduct)productsListAdapter.getItem(currentPopupPosition);
                    clonedProduct.setId(0);

                    if(radioButtonPartial.isChecked())
                        softReset(clonedProduct);
                    else if(radioButtonMin.isChecked())
                        hardReset(clonedProduct);

                    currentFilter = clonedProduct.getActualStorageCondition();

                    List<SingleProduct> productsToClone = new ArrayList<>();
                    for (int i = 0; i < TextUtils.getInt(clonesField); i++)
                        productsToClone.add(clonedProduct);

                    new Thread(() -> {
                        int nonAddedProductsCount = Collections.frequency(productDatabase.productDao().insertAll(productsToClone), -1);
                        int insertCount = productsToClone.size() - nonAddedProductsCount;
                        String msg = "Prodotti aggiunti: " + insertCount + "\nProdotti non aggiunti: " + nonAddedProductsCount;

                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show(); // STRINGS.XML
                            retrieveProductsFromDB(null); // aggiorna lista
                        });
                    }).start();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(cloneDialogView)
               .setTitle("Inserisci quantità di duplicati")
               .setPositiveButton("Conferma", dialogClickListener)
               .setNegativeButton("Annulla", dialogClickListener)
               .show();
    }

    // TODO spostare nel bean o in una classe di utils?
    private void hardReset(SingleProduct p){
        softReset(p);
        p.setPurchaseDate(null);
        p.setPointOfPurchaseId(0);
        p.setExpiryDate(null);
        p.setPackagingDate(null);
    }

    // TODO spostare nel bean o in una classe di utils?
    private void softReset(SingleProduct p){
        p.setCurrentWeight(0);
        p.setPercentageQuantity(100);
        p.setCurrentPieces(p.getPieces());
        p.setConsumptionDate(null);
        p.setConsumed(false);
        p.setOpened(false);
        p.setOpeningDate(null);
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

    // Ritorna una lista dei prodotti attualmente visualizzati a schermo
    private List<Product> getCurrentDisplayedProducts() {
        if (currentPackage == null)
            return filteredProducts;
        else
            return packProducts;
    }

    // Filtra e mostra i prodotti filtrati in base al testo contenuto nella search bar
    private void filterBySearchBar() {
        List<Product> searchResults = new ArrayList<>();

        if (searchBar.getText().length() > 0) { // Se la barra di ricerca contiene qualcosa
            // Cerca la stringa della barra di ricerca nel nome o la marca dei prodotti
            for (int i = 0; i < getCurrentDisplayedProducts().size(); i++) {
                if (getCurrentDisplayedProducts().get(i).getName().toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                    searchResults.add(getCurrentDisplayedProducts().get(i));
                else if (getCurrentDisplayedProducts().get(i).getBrand() != null) {
                    if (getCurrentDisplayedProducts().get(i).getBrand().toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                        searchResults.add(getCurrentDisplayedProducts().get(i));
                }

            }
            productsListAdapter = new ProductsListAdapter(MainActivity.this, R.layout.list_element, searchResults, showConsumedProducts);
        } else // Se la barra di ricerca è vuota resetta la view
            productsListAdapter = new ProductsListAdapter(MainActivity.this, R.layout.list_element, getCurrentDisplayedProducts(), showConsumedProducts);

        setAdapter(productsListAdapter);
    }

    // Aggiorna la lista dei prodotti dal DB e aggiorna la view
    // Se non si vuole visualizzare il contenuto di un gruppo passare null
    // .. altrimenti passare il riferimento all'oggetto pack
    private void retrieveProductsFromDB(Pack pack) {
        new Thread(() -> {
            singleProducts = productDatabase.productDao().getAll(); // Prendi tutti i prodotti
            runOnUiThread(() -> groupProducts(pack));
        }).start();
    }

    private void groupProducts(Pack pack) {
        List<SingleProduct> groupedSingleProducts = new ArrayList<>(singleProducts);
        groupedProducts = new ArrayList<>();
        if(!showConsumedProducts) // Non raggruppare se si visualizzano i prodotti consumati
            groupedProducts.addAll(getPacks(groupedSingleProducts));      // Passa gli eventuali raggruppamenti di prodotti
        groupedProducts.addAll(groupedSingleProducts);                // Passa i singleProduct di cui non è stato trovato alcun raggruppamento

        if (pack == null) {
            if(currentFilter==0)
                setFilteredProducts(filterButton0);
            else if(currentFilter==1)
                setFilteredProducts(filterButton1);
            else if(currentFilter==2)
                setFilteredProducts(filterButton2);
        } else
            setPackageView(pack);
    }

    // Raggruppa prodotti in base a caratteristiche comuni spostandoli dall'array ricevuto
    private List<Pack> getPacks(List<SingleProduct> singleProducts) {
        List<Pack> packs = new ArrayList<>();

        for (int i = 0; i < singleProducts.size(); i++) {                                           // Per ogni prodotto
            if ((showConsumedProducts && singleProducts.get(i).isConsumed()) || (!showConsumedProducts && !singleProducts.get(i).isConsumed())) {
                Pack p = new Pack();                                                                // Crea un nuovo pack
                for (int j = 0; j < singleProducts.size(); j++) {                                   // Cerca tra tutti i prodotti
                    if ((showConsumedProducts && singleProducts.get(j).isConsumed()) || (!showConsumedProducts && !singleProducts.get(j).isConsumed())) {
                        if (j != i && singleProducts.get(i).packEquals(singleProducts.get(j))) {    // .. se i due prodotti sono raggruppabili
                            p.addProduct(singleProducts.get(j));                                    // .. sposta il prodotto nel pack
                            singleProducts.remove(j);
                            j--;
                        }
                    }
                }
                if (!p.getProducts().isEmpty()) {                                                   // Se è stato raggruppato con almeno un altro prodotto
                    p.addProduct(singleProducts.get(i));                                            // .. sposta il prodotto nel pack
                    singleProducts.remove(i);
                    i--;
                    packs.add(p);                                                                   // .. aggiungi il pack alla lista
                }
            }
        }
        return packs;
    }

    // Mostra a schermo i prodotti filtrati secondo la modalità di conservazione attuale
    private void setFilterView(int storageCondition) {
        findViewById(R.id.storageConditionsBlock).setVisibility(View.VISIBLE); // Mostra pulsanti di filtro
        if(showConsumedProducts)
            setTitle("Consumati"); // Resetta il titolo al ritorno da una packageView
        else
            setTitle("MyFridge (test build)"); // Resetta il titolo al ritorno da una packageView
        currentPackage = null; // Comunica che non si sta visualizzando alcun gruppo
        currentFilter = storageCondition; // Comunica quale filtro si sta utilizzando

        filteredProducts = new ArrayList<>();
        for (int i = 0; i < groupedProducts.size(); i++) {
            if ((showConsumedProducts && groupedProducts.get(i).isConsumed()) || (!showConsumedProducts && !groupedProducts.get(i).isConsumed())) { // Controlla se il prodotto soddisfa il filtro corrente 'Mostra consumati'
                // Controlla se il prodotto soddisfa il filtro storageCondition ricevuto
                if (groupedProducts.get(i) instanceof SingleProduct) {
                    if (((SingleProduct) groupedProducts.get(i)).getActualStorageCondition() == currentFilter)
                        filteredProducts.add(groupedProducts.get(i));
                } else {
                    if ((groupedProducts.get(i)).getStorageCondition() == currentFilter) // TODO actualStorageCondition per gruppo ?
                        filteredProducts.add(groupedProducts.get(i));
                }
            }
        }
        sortByAscendingDate(filteredProducts); // TODO controlla prima quale ordinamento utilizzare
        productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, filteredProducts, showConsumedProducts);
        setAdapter(productsListAdapter);
        filterBySearchBar();
    }

    private void updateNoProductsWarning() {
        if (listView.getAdapter().getCount() == 0)
            noProductsWarning.setVisibility(View.VISIBLE);
        else
            noProductsWarning.setVisibility(View.GONE);
    }

    // Mostra nella view il contenuto di un raggruppamento (se non vuoto)
    private void setPackageView(Pack pack) {
        currentPackage = pack;
        findViewById(R.id.storageConditionsBlock).setVisibility(View.GONE); // Nascondi i pulsanti per filtrare la modalità di conservazione
        if (pack.getBrand() != null)
            setTitle(pack.getName() + " " + pack.getBrand());
        else
            setTitle(pack.getName());

        packProducts = new ArrayList<>();
        for (int i = 0; i < pack.getProducts().size(); i++) {
            if (showConsumedProducts){
                packProducts.add(pack.getProducts().get(i));
            } else {
                if (!pack.getProducts().get(i).isConsumed())
                    packProducts.add(pack.getProducts().get(i));
            }
        }

        productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, packProducts, showConsumedProducts);
        setAdapter(productsListAdapter);
        // TODO comunica tramite un textview il caso in cui non ci sia nessun prodotto da visualizzare, sia nel gruppo che non
    }

    // aggiunge adapter e aggiorna warning
    // TODO cambia lista all'adapter esistente senza inizializzarlo ogni volta e metti un observer per aggiornare il warning
    private void setAdapter(ProductsListAdapter adapter) {
        listView.setAdapter(adapter);
        updateNoProductsWarning();
    }

    // Avvia l'activity EditProduct per l'aggiunta
    public void addProduct(View view) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra("action", "add");
        intent.putExtra("filter", currentFilter);
        startActivityForResult(intent, ADD_PRODUCT_REQUEST);
    }

    // Avvia l'activity AddPointOfPurchase
    public void addPointOfPurchase(View view) {
        Intent intent = new Intent(this, AddPointOfPurchase.class);
        startActivity(intent);
    }

    // Avvia l'activity EditProduct per la modifica
    public void editSingleProduct(SingleProduct p) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra("id", p.getId());
        //intent.putExtra("filter", currentFilter);
        intent.putExtra("action", "edit");
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
    }

    public void updateProduct(SingleProduct p) {
        Intent intent = new Intent(this, EditProduct.class);
        intent.putExtra("id", p.getId());
        intent.putExtra("action", "update");
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
    }

    public void updateProduct(MenuItem item) {
        SingleProduct p = (SingleProduct) listView.getItemAtPosition(currentPopupPosition);
        updateProduct(p);
    }

    public void startShoppingMode(View view) {
        Intent intent = new Intent(this, ShoppingForm.class);
        startActivityForResult(intent, SHOPPING_REQUEST);
    }

    // ordina dalla data più recente alla più lontana
    private void sortByAscendingDate(List<Product> products) {
        if(showConsumedProducts)
            Collections.sort(products, new ConsumedDiscendingDateComparator());
        else
            Collections.sort(products, new AscendingDateComparator());
    }

    public void showPopup(View v) {
        currentPopupPosition = Integer.parseInt(v.getTag().toString());
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.element_options, popup.getMenu());

        // TODO nascondere / mostrare / cambiare voci
        if(productsListAdapter.getItem(currentPopupPosition).isConsumed()) {
            popup.getMenu().findItem(R.id.consumeItem).setVisible(false);
            popup.getMenu().findItem(R.id.unconsumeItem).setVisible(true);
            popup.getMenu().findItem(R.id.updateStateItem).setVisible(false);
        }

        popup.show();
    }

    // Aggiorna la lista rispecchiando le eventuali modifiche applicate dalle altre activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //resetSearchBar();

        showConsumedProducts = false; // TODO controllare per quali casi

        if (requestCode == ADD_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                currentFilter = data.getIntExtra("filter", currentFilter);
                retrieveProductsFromDB(null);
            }
        } else if (requestCode == EDIT_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (!data.getBooleanExtra("delete", false)) { // Se il prodotto è stato modificato
                    currentFilter = data.getIntExtra("filter", currentFilter);
                    pressOnFilter(null);
                }
                retrieveProductsFromDB(null);
            }
        } else if (requestCode == SHOPPING_REQUEST) {
            if (resultCode == RESULT_OK) {
                retrieveProductsFromDB(null);
            }
        }
    }
}
