package com.example.robertotarullo.myfridge.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.ProductsListAdapter;
import com.example.robertotarullo.myfridge.Bean.Pack;
import com.example.robertotarullo.myfridge.Bean.Product;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.Database.ProductDatabase;
import com.example.robertotarullo.myfridge.R;

import static com.example.robertotarullo.myfridge.Database.DatabaseUtils.DATABASE_NAME;

public class MainActivity extends AppCompatActivity {

    // variabili per intent
    private static final int ADD_PRODUCT_REQUEST = 1;
    private static final int EDIT_PRODUCT_REQUEST = 2;

    private int currentFilter;
    private Pack currentPackage;
    private boolean showConsumedProducts;

    // dichiarazione delle variabili di database
    private ProductDatabase productDatabase;
    private List<Product> products;
    private List<Product> filteredProducts;

    // views
    private EditText searchBar;
    private ListView listView;

    private ProductsListAdapter productsListAdapter;

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
        setFilteredProducts(Integer.valueOf(v.getTag().toString()));
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(currentPackage!=null)
            setFilteredProducts(currentFilter);
        else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productDatabase = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class, DATABASE_NAME).build(); // Ottieni un riferimento al db

        listView = findViewById(R.id.mylistview);
        searchBar = findViewById(R.id.searchBar);

        searchBar.addTextChangedListener(new SearchBarWatcher());

        currentFilter = 1; // leggere valore da impostazioni

        retrieveProductsFromDB(0); // Inizializza la lista leggendo dal db

        // Specifica cosa fare quando l'utente tocca un item della lista
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product p = (Product)listView.getItemAtPosition(position);
            if(p instanceof Pack && currentPackage==null)
                setPackageView((Pack)p);
            else
                editSingleProduct((SingleProduct)p);
        });

    }

    public void eraseField(View view) {
        if(view.getTag().toString().equals("searchBar")) {
            searchBar.setText("");
            searchBar.requestFocus();
        }
    }

    // Mostra i risultati di ricerca
    // Confronta la ricerca col nome e la marca
    public class SearchBarWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            List<Product> searchResults = new ArrayList<>();
            if(s.length()>0){
                for(int i=0; i<filteredProducts.size(); i++){
                    if(filteredProducts.get(i).getName()!=null){
                        if(filteredProducts.get(i).getName().toLowerCase().contains(s.toString().toLowerCase()))
                            searchResults.add(filteredProducts.get(i));
                    } else if(filteredProducts.get(i).getBrand()!=null){
                        if(filteredProducts.get(i).getBrand().toLowerCase().contains(s.toString().toLowerCase()))
                            searchResults.add(filteredProducts.get(i));
                    }
                }
                productsListAdapter = new ProductsListAdapter(MainActivity.this, R.layout.list_element, searchResults);
                listView.setAdapter(productsListAdapter);
            } else {
                productsListAdapter = new ProductsListAdapter(MainActivity.this, R.layout.list_element, filteredProducts);
                listView.setAdapter(productsListAdapter);
            }
        }
    }


    // prende i prodotti dal db e li mostra (se si vuole mostrare il contenuto di una confezione inserire un id > 0)
    private void retrieveProductsFromDB(long packageId){
        products = new ArrayList<>();

        new Thread(() -> {
            List<SingleProduct> singleProducts = productDatabase.productDao().getAll(); // Prendi i prodotti singoli
            List<Pack> packs = productDatabase.packDao().getAll();                         // Prendi tutti i pack

            runOnUiThread(() -> {
                // Crea un hashmap di pack
                Map<Long, Pack> packMap = new HashMap<>();
                for(Pack pack : packs)
                    packMap.put(pack.getId(), pack);

                // Sposta i singleProducts nei relativi pack
                for(int i=0; i<singleProducts.size(); i++) {
                    if(singleProducts.get(i).getPackageId()>0) {
                        packMap.get(singleProducts.get(i).getPackageId()).addProduct(singleProducts.get(i));
                        singleProducts.remove(i);
                        i--;
                    }
                }

                // Aggiungi packs e singleProducts alla lista globale
                products.addAll(singleProducts);
                products.addAll(packs);

                if(packageId==0)
                    setFilteredProducts(currentFilter); // controlla prima quale filtro utilizzare !!!!
                else
                    setPackageView(packMap.get(packageId));
            });
        }).start();
    }

    private void setFilteredProducts(int storageCondition){
        findViewById(R.id.storageConditionsBlock).setVisibility(View.VISIBLE);
        currentPackage = null;
        currentFilter = storageCondition;

        if(storageCondition==0)
            setTitle("Dispensa");
        else if(storageCondition==1)
            setTitle("Frigorifero");
        else if(storageCondition==2)
            setTitle("Congelatore");

        filteredProducts = new ArrayList<>();
        for(int i=0; i<products.size(); i++){
            if(showConsumedProducts){
                addFilteredProductToList(filteredProducts, products.get(i));
            } else {
                if(!products.get(i).isConsumed())
                    addFilteredProductToList(filteredProducts, products.get(i));
            }
        }
        sortByAscendingDate(filteredProducts); // controlla prima quale ordinamento utilizzare !!!!
        productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, filteredProducts);
        listView.setAdapter(productsListAdapter);
    }

    private void addFilteredProductToList(List<Product> filteredProducts, Product p){
        if (p instanceof SingleProduct) {
            if (((SingleProduct) p).getActualStorageCondition() == currentFilter)
                filteredProducts.add(p);
        } else {
            if (((Pack) p).belongsToStorageCondition(currentFilter))
                filteredProducts.add(p);
        }
    }

    private void setPackageView(Pack pack){
        findViewById(R.id.storageConditionsBlock).setVisibility(View.GONE);
        if(!showConsumedProducts){
            if(pack.getPieces()>0)
                showPackageProducts(pack);
            else // se la confezione è stata consumata torna indietro
                setFilteredProducts(currentFilter);
        } else
            showPackageProducts(pack);
    }

    private void showPackageProducts(Pack pack){
        setTitle(pack.getName());
        currentPackage = pack;
        List<Product> packProducts = new ArrayList<>();
        for(int i=0; i<pack.getProducts().size(); i++){
            if(showConsumedProducts)
                packProducts.add(pack.getProducts().get(i));
            else {
                if (!pack.getProducts().get(i).isConsumed())
                    packProducts.add(pack.getProducts().get(i));
            }
        }
        productsListAdapter = new ProductsListAdapter(this, R.layout.list_element, packProducts);
        listView.setAdapter(productsListAdapter);
    }


    public void exportDB(View view) {
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
    }

    // lancia AddProduct
    public void addProduct(View view){
        Intent intent = new Intent(this, AddProduct.class);
        intent.putExtra("action", "add");
        intent.putExtra("filter", currentFilter);
        if(currentPackage!=null)
            intent.putExtra("package", currentPackage.getId());
        startActivityForResult(intent, ADD_PRODUCT_REQUEST);
    }

    // lancia AddPointOfPurchase
    public void addPointOfPurchase(View view){
        Intent intent = new Intent(this, AddPointOfPurchase.class);
        startActivity(intent);
    }

    public void editSingleProduct(SingleProduct p){
        Intent intent = new Intent(this, AddProduct.class);
        intent.putExtra("id", p.getId());
        intent.putExtra("action", "edit");
        startActivityForResult(intent, EDIT_PRODUCT_REQUEST);
    }

    // ELIMINAZIONE TRAMITE CONSUMAZIONE
    public void deleteProduct(View view){
        int position = Integer.parseInt(view.getTag().toString());
        Product p = productsListAdapter.getItem(position);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(() -> {
                        if(p instanceof Pack) {
                            if (productDatabase.productDao().updatePackConsumption(p.getPackageId(), true) > 0) {
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Confezione rimossa", Toast.LENGTH_LONG).show();
                                    retrieveProductsFromDB(0); // aggiorna lista
                                });
                            }
                        } else {
                            if (productDatabase.productDao().updateConsumption(((SingleProduct) p).getId(), true) > 0){
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Prodotto rimosso", Toast.LENGTH_LONG).show();
                                    if(currentPackage!=null)
                                        retrieveProductsFromDB(currentPackage.getId()); // aggiorna lista
                                    else
                                        retrieveProductsFromDB(0); // aggiorna lista
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
            msg = "Vuoi eliminare la confezione \""+ p.getName() + "\"?";
        else
            msg = "Vuoi eliminare il prodotto \""+ p.getName() + "\"?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Conferma eliminazione")
                .setPositiveButton("Elimina", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    // ordina dalla data più recente alla più lontana, con i valori null alla fine
    private void sortByAscendingDate(List<Product> products){
        Collections.sort(products, (p1, p2) -> {

            Date date1;
            Date date2;

            if(p1 instanceof SingleProduct)
                date1 = ((SingleProduct) p1).getActualExpiringDate();
            else
                date1 = p1.getExpiryDate();

            if(p2 instanceof SingleProduct)
                date2 = ((SingleProduct) p2).getActualExpiringDate();
            else
                date2 = p2.getExpiryDate();

            if(date1==null)
                return -1;
            else if(date2==null)
                return 1;
            else if(date1.equals(date2))
                return 0;
            else if(date1.after(date2))
                return 1;
            else //if(date1.before(date2))
                return -1;
        });
    }

    // aggiorna la lista aggiungendo il nuovo prodotto inserito
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                currentFilter = data.getIntExtra("filter", 1);
                retrieveProductsFromDB(0);
            }
        } else if (requestCode == EDIT_PRODUCT_REQUEST) {
            if (resultCode == RESULT_OK) {
                currentFilter = data.getIntExtra("filter", 1);
                retrieveProductsFromDB(data.getLongExtra("packId", 0));
            }
        }
    }
}
