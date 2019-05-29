package com.example.robertotarullo.myfridge.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.ProductsListAdapter;
import com.example.robertotarullo.myfridge.Bean.Pack;
import com.example.robertotarullo.myfridge.Bean.Product;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.Database.ProductDatabase;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.DateUtils;

import static com.example.robertotarullo.myfridge.Database.DatabaseUtils.DATABASE_NAME;

public class MainActivity extends AppCompatActivity {

    // Dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    // Variabili per intent
    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int EDIT_PRODUCT_REQUEST = 2;

    // Variabili di stato
    private int currentFilter; // Determina la modalità di conservazione corrente
    private Pack currentPackage; // Riferimento alla confezione correntemente visualizzata, null se non si sta visualizzando una confezione
    private boolean showConsumedProducts; // Determina se mostrare i prodotti consumati

    // Dichiarazione delle liste di prodotti
    private List<Product> products; // Lista di tutti i prodotti della view (pack e singleProduct)
    private List<Product> filteredProducts; // Lista di prodotti della modalità di conservazione corrente (pack e singleProduct)
    private List<Product> packProducts; // Lista di prodotti della confezione corrente (solo singleProduct)

    // Riferimenti a elementi della view
    private ListView listView;
    private EditText searchBar;
    private Button filterButton0, filterButton1, filterButton2;

    // Adapter lista
    private ProductsListAdapter productsListAdapter;
    private TextView noProductsWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ottieni un riferimento al DB
        productDatabase = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class, DATABASE_NAME).build();

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
        highlightButton(filterButton1);

        // Inizializza la lista leggendo dal DB
        retrieveProductsFromDB(null);

        // Setta il comportamento al click di un elemento
        initializeItemBehaviour();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(showConsumedProducts)
            menu.add(0, R.id.showConsumed, Menu.NONE, "Nascondi consumati");
        else
            menu.add(0, R.id.showConsumed, Menu.NONE, "Mostra consumati");
        return super.onPrepareOptionsMenu(menu);
    }

    public void setFilteredProducts(View v){
        highlightButton((Button)v); // Cambia il colore del filtro attuale
        clearSerchBarFocus();
        setFilteredProducts(Integer.valueOf(v.getTag().toString())); // Filtra la lista dei prodotti in base al filtro selezionato
    }

    private void highlightButton(Button b){
        if(b==null){
            if(currentFilter==0)
                b = filterButton0;
            else if(currentFilter==1)
                b = filterButton1;
            else if(currentFilter==2)
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
                if(showConsumedProducts)
                    showConsumedProducts = false;
                else
                    showConsumedProducts = true;
                if(currentPackage!=null)
                    setPackageView(currentPackage);
                else
                    setFilteredProducts(currentFilter);
                filterBySearchBar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(currentPackage!=null) {
            resetSearchBar();
            setFilteredProducts(currentFilter);
        } else
            super.onBackPressed();
    }

    private void resetSearchBar(){
        searchBar.setText(""); // Svuota la barra di ricerca
        clearSerchBarFocus();
    }

    private void clearSerchBarFocus(){
        searchBar.clearFocus(); // Togli il focus alla barra di ricerca
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchBar.getWindowToken(), 0); // Nascondi la tastiera
    }

    // Specifica cosa fare quando l'utente tocca un item della lista
    private void initializeItemBehaviour(){
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product p = (Product)listView.getItemAtPosition(position);

            if(p instanceof Pack && currentPackage==null) { // Se si è clickato un pack
                resetSearchBar();
                setPackageView((Pack) p);
            } else                                          // Se si è clickato un singleProduct
                editSingleProduct((SingleProduct)p);
        });
    }

    // Svuota il testo contenuto nella search bar
    public void eraseField(View view) {
        if(view.getTag().toString().equals("searchBar")) {
            searchBar.setText("");
            searchBar.requestFocus();
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
    private List<Product> getCurrentDisplayedProducts(){
        if(currentPackage==null)
            return filteredProducts;
        else
            return packProducts;
    }

    // Filtra e mostra i prodotti filtrati in base al testo contenuto nella search bar
    private void filterBySearchBar(){
        List<Product> searchResults = new ArrayList<>();

        if(searchBar.getText().length()>0){ // Se la barra di ricerca contiene qualcosa
            // Cerca la stringa della barra di ricerca nel nome o la marca dei prodotti
            for(int i=0; i<getCurrentDisplayedProducts().size(); i++){
                if(getCurrentDisplayedProducts().get(i).getName()!=null){
                    if(getCurrentDisplayedProducts().get(i).getName().toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                        searchResults.add(getCurrentDisplayedProducts().get(i));
                } else if(getCurrentDisplayedProducts().get(i).getBrand()!=null){
                    if(getCurrentDisplayedProducts().get(i).getBrand().toLowerCase().contains(searchBar.getText().toString().toLowerCase()))
                        searchResults.add(getCurrentDisplayedProducts().get(i));
                }
            }
            productsListAdapter = new ProductsListAdapter(MainActivity.this, R.layout.list_element, searchResults);
        } else // Se la barra di ricerca è vuota resetta la view
            productsListAdapter = new ProductsListAdapter(MainActivity.this, R.layout.list_element, getCurrentDisplayedProducts());

        setAdapter(productsListAdapter);
    }

    // Aggiorna la lista dei prodotti dal DB e aggiorna la view
    // Se non si vuole visualizzare il contenuto di un gruppo passare null
    // .. altrimenti passare il riferimento all'oggetto pack
    private void retrieveProductsFromDB(Pack pack){
        System.out.println("Aggiorno la lista dei prodotti...");
        products = new ArrayList<>(); // Svuoto la lista precedente

        new Thread(() -> {
            List<SingleProduct> singleProducts = productDatabase.productDao().getAll(); // Prendi tutti i prodotti

            runOnUiThread(() -> {
                // Aggiungi packs e singleProducts alla lista globale
                products.addAll(groupProducts(singleProducts)); // Passa gli eventuali raggruppamenti di prodotti
                products.addAll(singleProducts);                // Passa i singleProduct di cui non è stato trovato alcun raggruppamento

                if(pack==null)
                    setFilteredProducts(currentFilter);
                else
                    setPackageView(pack);
            });
        }).start();
    }

    // Raggruppa prodotti in base a caratteristiche comuni spostandoli dall'array di SingleProduct ricevuto
    private List<Pack> groupProducts(List<SingleProduct> singleProducts){
        List<Pack> packs = new ArrayList<>();

        for(int i=0; i<singleProducts.size(); i++){                                     // Per ogni prodotto
            Pack p = new Pack();                                                        // Crea un nuovo pack
            for(int j=0; j<singleProducts.size(); j++){                                 // Cerca tra tutti i prodotti
                if(j!=i && singleProducts.get(i).packEquals(singleProducts.get(j))){    // .. se i due prodotti sono raggruppabili
                    p.addProduct(singleProducts.get(j));                                // .. sposta il prodotto nel pack
                    singleProducts.remove(j);
                    j--;
                }
            }
            if(!p.getProducts().isEmpty()){                                             // Se è stato raggruppato con almeno un altro prodotto
                p.addProduct(singleProducts.get(i));                                    // .. sposta il prodotto nel pack
                singleProducts.remove(i);
                i--;
                packs.add(p);                                                           // .. aggiungi il pack alla lista
            }
        }

        return packs;
    }

    // Mostra a schermo i prodotti filtrati secondo la modalità di conservazione attuale
    private void setFilteredProducts(int storageCondition){
        if(storageCondition!=currentFilter){
            findViewById(R.id.storageConditionsBlock).setVisibility(View.VISIBLE); // Mostra pulsanti di filtro
            currentPackage = null; // Comunica che non si sta visualizzando alcun gruppo
            setTitle("MyFridge");
            currentFilter = storageCondition; // Comunica quale filtro si sta utilizzando

            filteredProducts = new ArrayList<>();
            for(int i=0; i<products.size(); i++){
                if(showConsumedProducts || !products.get(i).isConsumed()){ // Controlla se il prodotto soddisfa il filtro corrente 'Mostra consumati'

                    // Controlla se il prodotto soddisfa il filtro storageCondition ricevuto
                    if (products.get(i) instanceof SingleProduct) {
                        if (((SingleProduct) products.get(i)).getActualStorageCondition() == currentFilter)
                            filteredProducts.add(products.get(i));
                    } else {
                        if ((products.get(i)).getStorageCondition() == currentFilter) // TODO actualStorageCondition per gruppo ?
                            filteredProducts.add(products.get(i));
                    }
                }
            }
            sortByAscendingDate(filteredProducts); // TODO controlla prima quale ordinamento utilizzare
            productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, filteredProducts);
            setAdapter(productsListAdapter);
            filterBySearchBar();
        }
    }

    private void updateNoProductsWarning(){
        if(listView.getAdapter().getCount()==0)
            noProductsWarning.setVisibility(View.VISIBLE);
        else
            noProductsWarning.setVisibility(View.GONE);
    }

    // Mostra nella view il contenuto di un raggruppamento (se non vuoto)
    private void setPackageView(Pack pack){
        currentPackage = pack;
        findViewById(R.id.storageConditionsBlock).setVisibility(View.GONE); // Nascondi i pulsanti per filtrare la modalità di conservazione
        if(pack.getBrand()!=null)
            setTitle(pack.getName() + " " + pack.getBrand());
        else
            setTitle(pack.getName());

        packProducts = new ArrayList<>();
        for(int i=0; i<pack.getProducts().size(); i++){
            if(showConsumedProducts)
                packProducts.add(pack.getProducts().get(i));
            else {
                if (!pack.getProducts().get(i).isConsumed())
                    packProducts.add(pack.getProducts().get(i));
            }
        }

        productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, packProducts);
        setAdapter(productsListAdapter);
        // TODO comunica tramite un textview il caso in cui non ci sia nessun prodotto da visualizzare, sia nel gruppo che non
    }

    // aggiunge adapter e aggiorna warning
    // TODO cambia lista all'adapter esistente senza inizializzarlo ogni volta e metti un observer per aggiornare il warning
    private void setAdapter(ProductsListAdapter adapter){
        listView.setAdapter(adapter);
        updateNoProductsWarning();
    }

    /*public void exportDB(View view) {
        new AlertDialog.Builder(this)
            .setTitle("Esporta Database")
            .setMessage("Ogni database precedentemente esportato verrà sostituito, continuare comunque? Il database verrà esportato nella directory principale del dispositivo")
            .setPositiveButton("Esporta", (dialog, whichButton) -> {
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                File dataDirectory = Environment.getDataDirectory();

                FileChannel source = null;
                FileChannel destination = null;


                String currentDBPath = "/user/0/com.example.robertotarullo.myfridge/databases/products_db";
                String backupDBPath = "myFridgeDB.sqlite";
                File currentDB = new File(dataDirectory, currentDBPath);
                File backupDB = new File(externalStorageDirectory, backupDBPath);

                try {
                    source = new FileInputStream(currentDB).getChannel();
                    destination = new FileOutputStream(backupDB).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    Toast.makeText(getApplicationContext(), "Database esportato", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Errore durante l'esportazione", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } finally {
                    try {
                        if (source != null) source.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (destination != null) destination.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            })
            .setNegativeButton("Annulla", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int whichButton) {
                    // non fare niente
                }
            }).show();
    }*/

    // Avvia l'activity AddProduct per l'aggiunta
    public void addProduct(View view){
        Intent intent = new Intent(this, AddProduct.class);
        intent.putExtra("action", "add");
        intent.putExtra("filter", currentFilter);
        startActivityForResult(intent, ADD_PRODUCT_REQUEST);
    }

    // Avvia l'activity AddPointOfPurchase
    public void addPointOfPurchase(View view){
        Intent intent = new Intent(this, AddPointOfPurchase.class);
        startActivity(intent);
    }

    // Avvia l'activity AddProduct per la modifica
    public void editSingleProduct(SingleProduct p){
        Intent intent = new Intent(this, AddProduct.class);
        intent.putExtra("id", p.getId());
        intent.putExtra("action", "edit");
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
    }

    // Mostra dialog per la consumazione di un singleProduct
    public void consumeProduct(View view){
        int position = Integer.parseInt(view.getTag().toString());
        Product p = productsListAdapter.getItem(position);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(() -> {
                        if(p instanceof Pack) {
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
                            if (productDatabase.productDao().updateConsumption(((SingleProduct) p).getId(), true) > 0){
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
        if(p instanceof Pack)
            msg = "Vuoi consumare tutti i prodotti di tipo \""+ p.getName() + "\"?";
        else
            msg = "Vuoi consumare il prodotto \""+ p.getName() + "\"?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Conferma consumazione")
                .setPositiveButton("Consuma", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    // ordina dalla data più recente alla più lontana, con i valori null alla fine
    private void sortByAscendingDate(List<Product> products){

        // Ordine: non specificata > data crescente > mai > consumati

        Collections.sort(products, (p1, p2) -> {

            Date date1 = DateUtils.getActualExpiryDate(p1);
            Date date2 = DateUtils.getActualExpiryDate(p2);

            // -1 mette in alto p1
            // 1 mette in alto p2
            // 0 mantiene l'ordine di default

            if(p1.isConsumed() && !p2.isConsumed())             // dai precedenza a non consumato
                return 1;
            else if(!p1.isConsumed() && p2.isConsumed())
                return -1;
            else if(p1.isConsumed() && p2.isConsumed())         // entrambi consumati
                return 0; // TODO ordina per data di consumazione crescente
            else if(date1==null && date2==null) {               // entrambe non specificate
                if(p1.isPackaged() == p2.isPackaged())          // dai precedenza ai prodotti freschi
                    return 0;
                else if(p1.isPackaged())
                    return 1;
                else if(p2.isPackaged())
                    return -1;
            } else if(date1==null)                              // dai precedenza a non specificata
                return -1;
            else if(date2==null)                                // dai precedenza a non specificata
                return 1;
            else if(date1.equals(date2))                        // date uguali
                return 0;
            else if(date1.after(date2)){
                if(date2.equals(DateUtils.getNoExpiryDate()))   // dai precedenze a data non 'mai'
                    return -1;
                else
                    return 1;                                   // dai precedenza alla data più vecchia
            } else if(date1.before(date2)) {
                if(date1.equals(DateUtils.getNoExpiryDate()))
                    return 1;                                   // dai precedenze a data non 'mai'
                else
                    return -1;                                  // dai precedenza alla data più vecchia
            }

            return 0;
        });
    }

    // Aggiorna la lista rispecchiando le eventuali modifiche applicate dalle altre activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        resetSearchBar();

        if (requestCode == ADD_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                currentFilter = data.getIntExtra("filter", currentFilter);
                retrieveProductsFromDB(null);
            }
        } else if (requestCode == EDIT_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                if(!data.getBooleanExtra("delete", false)) { // Se il prodotto è stato modificato
                    currentFilter = data.getIntExtra("filter", currentFilter);
                    highlightButton(null);
                }
                retrieveProductsFromDB(null);
            }
        }
    }
}
