package com.example.robertotarullo.myfridge.Activity;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.Bean.ProductForm;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.Adapter.PointsOfPurchaseSpinnerAdapter;
import com.example.robertotarullo.myfridge.Bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.Database.DatabaseUtils;
import com.example.robertotarullo.myfridge.Database.ProductDatabase;
import com.example.robertotarullo.myfridge.Fragment.SpinnerDatePickerFragment;
import com.example.robertotarullo.myfridge.Listener.CurrentWeightSliderListener;
import com.example.robertotarullo.myfridge.TextWatcher.PiecesWatcher;
import com.example.robertotarullo.myfridge.TextWatcher.QuantityWatcher;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;
import com.example.robertotarullo.myfridge.Fragment.DatePickerFragment;
import com.example.robertotarullo.myfridge.InputFilter.DaysInputFilter;
import com.example.robertotarullo.myfridge.InputFilter.PriceInputFilter;
import com.example.robertotarullo.myfridge.InputFilter.WeightInputFilter;
import com.example.robertotarullo.myfridge.Utils.PriceUtils;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.TextWatcher.PriceWeightRelationWatcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class AddProduct extends AppCompatActivity {

    // variabili statiche
    public static final int FREEZER_MAX_CELSIUS = 0, FRIDGE_MIN_CELSIUS = 3, FRIDGE_MAX_CELSIUS = 6;
    public static final int FRIDGE_DEFAULT_CELSIUS = 5, FREEZER_DEFAULT_CELSIUS = -18, ROOM_DEFAULT_CELSIUS = 20;  // ideale (4-6) per frigo
    public static final int ROOM_SELECTION = 0, FRIDGE_SELECTION = 1, FREEZER_SELECTION = 2;
    public static final int MAX_QUANTITY = 99, MIN_QUANTITY = 1, MAX_PIECES = 99, MIN_PIECES = 1;

    // intent
    private String action;
    private ProductForm startingForm;
    private long productToModifyId;
    private ArrayList<SingleProduct> shoppingCart;

    // Variabili per i suggerimenti dei campi
    private ArrayList<String> nameSuggestionsList, brandSuggestionsList;

    // views
    private ScrollView listScrollView;
    private EditText nameField, brandField, pricePerKiloField, priceField, weightField, purchaseDateField, expiryDateField, openingDateField, expiryDaysAfterOpeningField, currentWeightField;
    private Spinner storageConditionSpinner, openedStorageConditionSpinner, pointOfPurchaseSpinner;
    private CheckBox openedCheckBox, packagedCheckBox, noExpiryCheckbox;
    private Button confirmButton, priceClearButton, pricePerKiloClearButton, weightClearButton, changeToExpiryDaysButton, changeToExpiryDateButton, addQuantityButton, subtractQuantityButton, addPieceButton, subtractPieceButton;
    private SeekBar currentWeightSlider;
    private TextView storageConditionSpinnerLabel, quantityField, piecesField, currentPiecesField, expiryDaysAfterOpeningLabel;

    // variabili di controllo del form
    private boolean expiryDateMode;
    private List<SingleProduct> products;

    // dichiarazione dei blocchi che hanno regole per la visibilità
    private LinearLayout openingDateBlock, expiryDateBlock, openedCheckBoxBlock, openedStorageConditionBlock, currentWeightBlock, quantityBlock, currentPiecesBlock, expiryDaysAfterOpeningBlock, pointOfPurchaseBlock, purchaseDateBlock;

    // dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    // resetta lo stato dell'activity come al lancio
    // TODO Testare il buon funzionamento del risultato finale
    private void resetActivityStatus(){
        packagedCheckBox.setChecked(false); // TODO leggere da un valore delle impostazioni
        nameField.setText("");
        brandField.setText("");
        pricePerKiloField.setText("");
        currentWeightField.setText("");
        priceField.setText("");
        weightField.setText("");
        storageConditionSpinner.setSelection(FRIDGE_SELECTION); // TODO leggere da un valore delle impostazioni
        openedStorageConditionSpinner.setSelection(storageConditionSpinner.getSelectedItemPosition());
        pointOfPurchaseSpinner.setSelection(0);
        purchaseDateField.setText("");
        openingDateField.setText("");
        expiryDateField.setText("");
        expiryDaysAfterOpeningField.setText("");
        openedCheckBox.setChecked(false);
        quantityField.setText(String.valueOf(MIN_QUANTITY));
        piecesField.setText(String.valueOf(MIN_PIECES));
        currentPiecesField.setText(String.valueOf(MIN_PIECES));
        noExpiryCheckbox.setChecked(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Ottieni un riferimento al db
        productDatabase = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class, DatabaseUtils.DATABASE_NAME).build();

        // Riferimenti ai campi
        packagedCheckBox = findViewById(R.id.packagedCheckBox);
        nameField = findViewById(R.id.nameField);
        brandField = findViewById(R.id.brandField);
        pricePerKiloField = findViewById(R.id.pricePerKiloField);
        currentWeightField = findViewById(R.id.currentWeightField);
        priceField = findViewById(R.id.priceField);
        weightField = findViewById(R.id.weightField);
        storageConditionSpinner = findViewById(R.id.storageConditionSpinner);
        openedStorageConditionSpinner = findViewById(R.id.openedStorageConditionSpinner);
        pointOfPurchaseSpinner = findViewById(R.id.pointOfPurchaseSpinner);
        purchaseDateField = findViewById(R.id.purchaseDateField);
        openingDateField = findViewById(R.id.openingDateField);
        expiryDateField = findViewById(R.id.expiryDateField);
        expiryDaysAfterOpeningField = findViewById(R.id.expiryDaysAfterOpeningField);
        openedCheckBox = findViewById(R.id.openedCheckBox);
        currentWeightSlider = findViewById(R.id.currentWeightSlider);
        quantityField = findViewById(R.id.quantityField);
        piecesField = findViewById(R.id.piecesField);
        currentPiecesField = findViewById(R.id.currentPiecesField);
        noExpiryCheckbox = findViewById(R.id.noExpiryCheckbox);

        // View di controllo del form
        changeToExpiryDateButton = findViewById(R.id.changeToExpiryDate);
        changeToExpiryDaysButton = findViewById(R.id.changeToExpiryDays);
        addQuantityButton = findViewById(R.id.quantityAddButton);
        subtractQuantityButton = findViewById(R.id.quantitySubtractButton);
        addPieceButton = findViewById(R.id.piecesAddButton);
        subtractPieceButton = findViewById(R.id.piecesSubtractButton);
        listScrollView = findViewById(R.id.listScrollView);
        storageConditionSpinnerLabel = findViewById(R.id.storageConditionSpinnerLabel);
        expiryDaysAfterOpeningLabel = findViewById(R.id.expiryDaysAfterOpeningFieldLabel);
        confirmButton = findViewById(R.id.addButton);

        // riferimenti ai blocchi che hanno regole per la visibilità
        currentPiecesBlock = findViewById(R.id.currentPiecesBlock);
        openingDateBlock = findViewById(R.id.openingDateBlock);
        expiryDateBlock = findViewById(R.id.expiryDateBlock);
        openedCheckBoxBlock = findViewById(R.id.openedCheckBoxBlock);
        openedStorageConditionBlock = findViewById(R.id.openedStorageConditionBlock);
        currentWeightBlock = findViewById(R.id.currentWeightBlock);
        quantityBlock = findViewById(R.id.quantityBlock);
        expiryDaysAfterOpeningBlock = findViewById(R.id.expiryDaysAfterOpeningBlock);
        pointOfPurchaseBlock = findViewById(R.id.pointOfPurchaseBlock);
        purchaseDateBlock = findViewById(R.id.purchaseDateBlock);

        // riferimenti ai pulsanti clear di campi coinvolti in relazioni
        priceClearButton = findViewById(R.id.priceClearButton);
        pricePerKiloClearButton = findViewById(R.id.pricePerKiloClearButton);
        weightClearButton = findViewById(R.id.weightClearButton);

        action = getIntent().getStringExtra("action");

        // inizializza gli array per i suggerimenti
        initializeSuggestions();

        // Popola spinners
        initializeStorageSpinners();
        initializePointsOfPurchaseSpinner();

        // variabili di controllo del form
        expiryDateMode = false; // TODO configurabile: valore iniziale a preferenza dell'utente

        // Comportamenti delle checkbox
        initializeOpenedCheckBox(true);
        initializePackagedCheckBox(true);
        initializeNoExpiryCheckBox(true);

        // Validazione e comportamento
        currentWeightSlider.setTag(R.id.percentageValue, "100");
        priceField.addTextChangedListener(new PriceWeightRelationWatcher(priceField.getTag().toString(), pricePerKiloField, weightField, pricePerKiloClearButton, weightClearButton, currentWeightField, currentWeightSlider));
        pricePerKiloField.addTextChangedListener(new PriceWeightRelationWatcher(pricePerKiloField.getTag().toString(), priceField, weightField, priceClearButton, weightClearButton, currentWeightField, currentWeightSlider));
        weightField.addTextChangedListener(new PriceWeightRelationWatcher(weightField.getTag().toString(), priceField, pricePerKiloField, priceClearButton, pricePerKiloClearButton, currentWeightField, currentWeightSlider));
        purchaseDateField.addTextChangedListener(new DateWatcher(purchaseDateField));
        openingDateField.addTextChangedListener(new DateWatcher(openingDateField));
        expiryDateField.addTextChangedListener(new DateWatcher(expiryDateField));
        currentWeightSlider.setOnSeekBarChangeListener(new CurrentWeightSliderListener(weightField, currentWeightField, piecesField, currentPiecesField));
        quantityField.addTextChangedListener(new QuantityWatcher(addQuantityButton, subtractQuantityButton, MIN_QUANTITY, MAX_QUANTITY));
        piecesField.addTextChangedListener(new PiecesWatcher(addPieceButton, subtractPieceButton, MIN_PIECES, MAX_PIECES, currentWeightSlider, currentPiecesField, weightField, currentWeightField, currentPiecesBlock));

        // InputFilters
        expiryDaysAfterOpeningField.setFilters(new InputFilter[]{new DaysInputFilter()});
        priceField.setFilters(new InputFilter[]{new PriceInputFilter()});
        pricePerKiloField.setFilters(new InputFilter[]{new PriceInputFilter()});
        weightField.setFilters(new InputFilter[]{new WeightInputFilter()});
        currentWeightField.setFilters(new InputFilter[]{new WeightInputFilter()});

        // Comportamento alla perdita del focus
        weightField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onWeightFocusLost(); });
        priceField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onPriceFocusLost(priceField); });
        pricePerKiloField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onPriceFocusLost(pricePerKiloField); });

        if(action.equals("add") || action.equals("shopping")) {
            setTitle("Aggiungi prodotto");
            confirmButton.setText("Aggiungi");

            if(action.equals("shopping")) {
                shoppingCart = new ArrayList<>(); // Inizializza il carrello

                // Nascondi i campi precompilati
                findViewById(R.id.addButton).setVisibility(View.GONE);
                findViewById(R.id.shoppingModeButtonsBlock).setVisibility(View.VISIBLE);

                pointOfPurchaseBlock.setVisibility(View.GONE);
                purchaseDateBlock.setVisibility(View.GONE);
                openedCheckBoxBlock.setVisibility(View.GONE);
                currentWeightBlock.setVisibility(View.GONE);
            } else
                startingForm = getCurrentForm();

        } else if(action.equals("edit")) {
            setTitle("Modifica prodotto");
            productToModifyId = getIntent().getLongExtra("id", 0);
            confirmButton.setText("Salva");
            quantityBlock.setVisibility(View.GONE);
            fillForm();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(action.equals("edit"))
            menu.add(0, R.id.delete, Menu.NONE, "Elimina");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            new Thread(() -> {
                                if (productDatabase.productDao().deleteById(productToModifyId) > 0) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("delete", true);
                                    setResult(RESULT_OK, resultIntent);
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Prodotto eliminato", Toast.LENGTH_LONG).show());
                                    finish();
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(action.equals("shopping")) {
            if(shoppingCart.size()>0){
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("E' in corso la modalità spesa, sei sicuro di voler uscire senza salvare? Tutti gli eventuali prodotti aggiunti andranno persi.")
                        .setTitle("Attenzione")
                        .setPositiveButton("Esci", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();
            } else {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        } else {
            // chiedere conferma all'utente se tornare all'attività chiamante nel caso qualche campo sia stato modificato
            if(!startingForm.equals(getCurrentForm())){
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
                builder.setMessage("Sono stati modificati alcuni campi, sei sicuro di voler uscire senza salvare?")
                        .setTitle("Attenzione")
                        .setPositiveButton("Esci", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();
            } else
                super.onBackPressed();
        }
    }

    private ProductForm getCurrentForm(){
        return new ProductForm(createProductFromFields(), TextUtils.getInt(quantityField), TextUtils.getDate(expiryDateField), TextUtils.getInt(expiryDaysAfterOpeningField));
    }

    public void editFieldNotFromUser(EditText dateField, String text){
        dateField.setTag(R.id.warningEdit, "lock");
        dateField.setText(text);
        dateField.setTag(R.id.warningEdit, null);
    }

    private void initializeSuggestions(){
        nameSuggestionsList = new ArrayList<>();
        brandSuggestionsList = new ArrayList<>();

        new Thread(() -> {
            products = productDatabase.productDao().getAll(); // TODO Prendi tutti i prodotti non uguali
            runOnUiThread(() -> addSuggestions(products));
        }).start();
    }

    private void addSuggestions(SingleProduct suggestion){
        List<SingleProduct> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        addSuggestions(suggestions);
    }

    private void addSuggestions(List<SingleProduct> suggestions){
        List<String> lowerCaseNameSuggestionsList = new ArrayList<>(nameSuggestionsList);
        lowerCaseNameSuggestionsList.replaceAll(String::toLowerCase);

        List<String> lowerCaseBrandSuggestionsList = new ArrayList<>(nameSuggestionsList);
        lowerCaseBrandSuggestionsList.replaceAll(String::toLowerCase);

        // Aggiungi solo valori non duplicati e non null
        for(int i=0; i<suggestions.size(); i++){
            if(suggestions.get(i).getName()!=null && !lowerCaseNameSuggestionsList.contains(suggestions.get(i).getName().toLowerCase()))
                nameSuggestionsList.add(suggestions.get(i).getName());

            if(suggestions.get(i).getBrand()!=null && !lowerCaseBrandSuggestionsList.contains(suggestions.get(i).getBrand().toLowerCase()))
                brandSuggestionsList.add(suggestions.get(i).getBrand());
        }

        ((AutoCompleteTextView)nameField).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nameSuggestionsList));
        ((AutoCompleteTextView)brandField).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, brandSuggestionsList));
    }

    // Compila tutti i campi con i dati del prodotto da modificare
    private void fillForm() {
        new Thread(() -> {
            SingleProduct p = productDatabase.productDao().get(productToModifyId);

            // printProductOnConsole(p);

            runOnUiThread(() -> {
                nameField.setText(p.getName());
                if(p.getBrand()!=null)
                    brandField.setText(p.getBrand());
                if(p.getPrice()>0)
                    priceField.setText(PriceUtils.getFormattedPrice(p.getPrice()));
                if(p.getPricePerKilo()>0)
                    pricePerKiloField.setText(String.valueOf(p.getPricePerKilo()));
                if(p.getWeight()>0) {
                    weightField.setText(PriceUtils.getFormattedWeight(p.getWeight()));
                    currentWeightField.setText(PriceUtils.getFormattedWeight(p.getCurrentWeight()));
                }
                currentWeightSlider.setTag(R.id.percentageValue, String.valueOf(p.getPercentageQuantity()));
                if(p.getExpiringDaysAfterOpening()>0)
                    editFieldNotFromUser(expiryDaysAfterOpeningField, String.valueOf(p.getExpiringDaysAfterOpening()));
                if(p.getPurchaseDate()!=null)
                    editFieldNotFromUser(purchaseDateField, DateUtils.getFormattedDate(p.getPurchaseDate()));
                storageConditionSpinner.setSelection(p.getStorageCondition());

                if(p.getPointOfPurchaseId()>0) {
                    for(int i=0; i<pointOfPurchaseSpinner.getCount(); i++){
                        if(((PointOfPurchase)pointOfPurchaseSpinner.getItemAtPosition(i)).getId()==p.getPointOfPurchaseId())
                            pointOfPurchaseSpinner.setSelection(i);
                    }
                }

                piecesField.setText(String.valueOf(p.getPieces()));
                currentPiecesField.setText(String.valueOf(p.getCurrentPieces()));

                if(p.getExpiryDate()!=null) {
                    if(p.getExpiryDate().equals(DateUtils.getNoExpiryDate())) {
                        noExpiryCheckbox.setChecked(true);
                        editFieldNotFromUser(expiryDaysAfterOpeningField, "");
                    } else {
                        editFieldNotFromUser(expiryDateField, DateUtils.getFormattedDate(p.getExpiryDate()));
                        if(!p.isPackaged())
                            changeToExpiringDateMode(true); // Mostra 'data di scadenza' e nascondi 'giorni entro cui consumare'
                    }
                }

                // Se si tratta di un prodotto confezionato
                if(p.isPackaged()){
                    packagedCheckBox.setChecked(true);

                    // Se si tratta di un prodotto chiuso confezionato
                    if(p.isOpened()) {
                        openedCheckBox.setChecked(true);
                        if(p.getOpeningDate()!=null)
                            editFieldNotFromUser(openingDateField, DateUtils.getFormattedDate(p.getOpeningDate()));
                    }

                    openedStorageConditionSpinner.setSelection(p.getOpenedStorageCondition());
                }

                // Se si tratta di un prodotto fresco o confezione aperta
                if(p.isOpened()){
                    if(p.getWeight()==0 && p.getPieces()==1) {
                        currentWeightSlider.setProgress(p.getPercentageQuantity());
                    } else if(p.getWeight()>0 && p.getPieces()==1) {
                        currentWeightSlider.setTag("currentWeight");
                        currentWeightSlider.setMax(TextUtils.getInt(weightField));
                        currentWeightSlider.setProgress(TextUtils.getInt(currentWeightField));
                    } else {
                        currentWeightSlider.setTag("pieces");
                        currentWeightSlider.setMax(p.getPieces());
                        currentWeightSlider.setProgress(p.getCurrentPieces());
                    }
                }

                startingForm = getCurrentForm();
            });
        }).start();
    }

    // Metodo chiamato alla pressione del tasto di conferma, che può essere l'aggiunta o la modifica del prodotto
    public void onConfirmButtonClick(View view) {
        // esegui tutte le funzioni della perdita del focus per avere il valore corretto effettivo
        onWeightFocusLost();
        onPriceFocusLost(priceField);
        onPriceFocusLost(pricePerKiloField);

        // Il campo nome è obbligatorio
        if (TextUtils.isEmpty(nameField)) {
            Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
            setFocusAndScrollToView(findViewById(R.id.nameBlock));
        } else {
            SingleProduct newProduct = createProductFromFields();

            new Thread(() -> {
                int insertCount = 0; // counter inserimenti
                Intent resultIntent = new Intent();

                if(action.equals("edit")) { // Se si tratta di una modifica
                    newProduct.setId(productToModifyId);

                    if(productDatabase.productDao().update(newProduct)>0) {
                        insertCount = 1;
                        String msg = "Prodotti modificati: " + insertCount + "\nProdotti non modificati: " + (TextUtils.getInt(quantityField) - insertCount);
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                    }
                } else if(action.equals("add")){ // Se si tratta di un'aggiunta
                    List<SingleProduct> productsToAdd = new ArrayList<>();
                    for(int i=0; i<TextUtils.getInt(quantityField); i++)
                        productsToAdd.add(newProduct);
                    insertCount = addProducts(productsToAdd);
                } else if(action.equals("shopping")) { // Se si tratta di un'aggiunta
                    for(int i=0; i<TextUtils.getInt(quantityField); i++)
                        shoppingCart.add(newProduct);

                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Prodotto aggiunto al carrello", Toast.LENGTH_LONG).show();

                        // Aggiorna i suggerimenti con l'ultimo prodotto inserito
                        addSuggestions(newProduct);

                        resetActivityStatus();
                    });
                }

                if(insertCount>0){
                    resultIntent.putExtra("filter", newProduct.getActualStorageCondition());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }).start();
        }
    }

    private int addProducts(List<SingleProduct> productsToAdd){
        int nonAddedProductsCount = Collections.frequency(productDatabase.productDao().insertAll(productsToAdd), -1);
        int insertCount = productsToAdd.size() - nonAddedProductsCount;

        String msg = "Prodotti aggiunti: " + insertCount + "\nProdotti non aggiunti: " + nonAddedProductsCount;
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML

        return insertCount;
    }

    // metodo usato per il debug
    private void printProductOnConsole(SingleProduct p){
        System.out.println("id: " + p.getId());
        System.out.println("packaged: " + p.isPackaged());
        System.out.println("name: " + p.getName());
        System.out.println("brand: " + p.getBrand());
        System.out.println("price: " + p.getPrice());
        System.out.println("priceperkilo: " + p.getPricePerKilo());
        System.out.println("weight: " + p.getWeight());
        System.out.println("currentweight: " + p.getCurrentWeight());
        System.out.println("percentagequantity: " + p.getPercentageQuantity());
        System.out.println("pieces: " + p.getPieces());
        System.out.println("currentpieces: " + p.getCurrentPieces());
        System.out.println("expiringdaysafteropening: " + p.getExpiringDaysAfterOpening());
        System.out.println("purchasedate: " + p.getPurchaseDate());
        System.out.println("storagecondition: " + p.getStorageCondition());
        System.out.println("pointofpurchaseid: " + p.getPointOfPurchaseId());
        System.out.println("consumed: " + p.isConsumed());
        System.out.println("-------------------------");
        System.out.println("opened: " + p.isOpened());
        System.out.println("openingdate: " + p.getOpeningDate());
        System.out.println("expirydate: " + p.getExpiryDate());
        System.out.println("openedstoragecondition: " + p.getOpenedStorageCondition());
    }

    // Costruisce l'oggetto prodotto dai valori presenti nei campi
    private SingleProduct createProductFromFields(){
        SingleProduct p = new SingleProduct();

        p.setName(nameField.getText().toString());

        if(!TextUtils.isEmpty(brandField))
            p.setBrand(brandField.getText().toString());

        if(!TextUtils.isEmpty(priceField) && priceField.isEnabled())
            p.setPrice(TextUtils.getFloat(priceField));

        if(!TextUtils.isEmpty(weightField) && weightField.isEnabled())
            p.setWeight(TextUtils.getFloat(weightField));

        if(!TextUtils.isEmpty(currentWeightField))
            p.setCurrentWeight(TextUtils.getFloat(currentWeightField));

        if (!TextUtils.isEmpty(pricePerKiloField) && pricePerKiloField.isEnabled())
            p.setPricePerKilo(TextUtils.getFloat(pricePerKiloField));

        p.setPieces(TextUtils.getInt(piecesField));

        if(action.equals("shopping"))
            p.setPurchaseDate(DateUtils.getCurrentDateWithoutTime()); // TODO settare anche l'ora se implementata
        else if(TextUtils.getDate(purchaseDateField)!=null)
            p.setPurchaseDate(TextUtils.getDate(purchaseDateField));

        p.setStorageCondition(storageConditionSpinner.getSelectedItemPosition());

        if(action.equals("shopping"))
            p.setPointOfPurchaseId(getIntent().getLongExtra("pointOfPurchaseId", 0)); // settare pointofpurchase dall'intent
        else if(pointOfPurchaseSpinner.getSelectedItemPosition()>0)
            p.setPointOfPurchaseId(((PointOfPurchase)pointOfPurchaseSpinner.getSelectedItem()).getId());

        if(!TextUtils.isEmpty(expiryDaysAfterOpeningField) && expiryDaysAfterOpeningBlock.getVisibility()==View.VISIBLE && expiryDaysAfterOpeningBlock.isEnabled())
            p.setExpiringDaysAfterOpening(TextUtils.getInt(expiryDaysAfterOpeningField));

        p.setCurrentPieces(TextUtils.getInt(currentPiecesField));

        // campi che dipendono dal tipo e dall'apertura del prodotto confezionato
        if(packagedCheckBox.isChecked()){
            p.setPackaged(true);

            if(noExpiryCheckbox.isChecked())
                p.setExpiryDate(DateUtils.getNoExpiryDate());
            else
                p.setExpiryDate(TextUtils.getDate(expiryDateField));

            if(openedCheckBox.isChecked()) {
                p.setOpened(true);

                if(TextUtils.getDate(openingDateField)!=null)
                    p.setOpeningDate(TextUtils.getDate(openingDateField));
            } else
                p.setOpened(false); // non rimuovere

            p.setOpenedStorageCondition(openedStorageConditionSpinner.getSelectedItemPosition());

        } else { // compilazione dei campi di prodotti confezionati se prodotto non confezionato
            p.setOpened(true);

            p.setOpeningDate(p.getPurchaseDate());

            if(expiryDateBlock.getVisibility()==View.VISIBLE)
                p.setExpiryDate(TextUtils.getDate(expiryDateField));
            p.setOpenedStorageCondition(p.getStorageCondition());
        }

        if(p.isOpened()){ // si tratta di un prodotto confezionato aperto OPPURE di un prodotto fresco
            // Memorizza la quantità percentuale
            if(currentWeightSlider.getTag().toString().equals("currentWeight"))
                p.setPercentageQuantity((int) Math.ceil((currentWeightSlider.getProgress() * 100) / (float)TextUtils.getInt(weightField)));
            else if(currentWeightSlider.getTag().toString().equals("pieces"))
                p.setPercentageQuantity((int) Math.ceil((currentWeightSlider.getProgress() * 100) / (float)TextUtils.getInt(piecesField)));
            else
                p.setPercentageQuantity(currentWeightSlider.getProgress());
        } else { // prodotto confezionato chiuso
            p.setPercentageQuantity(100);
            p.setCurrentPieces(p.getPieces());
            if(!TextUtils.isEmpty(weightField))
                p.setCurrentWeight(p.getWeight());
        }

        printProductOnConsole(p);
        return p;
    }

    private void initializeOpenedCheckBox(boolean addListener) {
        if(openedCheckBox.isChecked()) {
            currentWeightBlock.setVisibility(View.VISIBLE);
            openingDateBlock.setVisibility(View.VISIBLE);
            currentWeightSlider.setVisibility(View.VISIBLE);
        } else {
            currentWeightBlock.setVisibility(View.GONE);
            openingDateBlock.setVisibility(View.GONE);
            currentWeightSlider.setVisibility(View.GONE);
        }

        if(addListener)
            openedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeOpenedCheckBox(false));
    }

    private void initializePackagedCheckBox(boolean addListener) {
        if(packagedCheckBox.isChecked()){
            enableNoExpiryCheckBoxBehaviour(noExpiryCheckbox.isChecked());
            changeToExpiryDateButton.setVisibility(View.GONE);
            changeToExpiryDaysButton.setVisibility(View.GONE);
            expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
            noExpiryCheckbox.setVisibility(View.VISIBLE);
            storageConditionSpinnerLabel.setText("Modalità di conservazione prima dell'apertura");
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare dopo l'apertura");
            expiryDateBlock.setVisibility(View.VISIBLE);

            if(!action.equals("shopping"))
                openedCheckBoxBlock.setVisibility(View.VISIBLE);

            openedStorageConditionBlock.setVisibility(View.VISIBLE);
            if(!openedCheckBox.isChecked() && !action.equals("shopping"))
                currentWeightBlock.setVisibility(View.GONE);
            else if(!action.equals("shopping"))
                openingDateBlock.setVisibility(View.VISIBLE);

            if(currentWeightSlider.getProgress()<currentWeightSlider.getMax())
                openedCheckBox.setChecked(true);
            else
                openedCheckBox.setChecked(false);
        } else {
            expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
            enableNoExpiryCheckBoxBehaviour(false);
            changeToExpiryDateButton.setVisibility(View.VISIBLE);
            changeToExpiryDaysButton.setVisibility(View.VISIBLE);
            noExpiryCheckbox.setVisibility(View.GONE);
            storageConditionSpinnerLabel.setText("Modalità di conservazione");
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare");
            expiryDateBlock.setVisibility(View.GONE);

            if(!action.equals("shopping"))
                openedCheckBoxBlock.setVisibility(View.GONE);
            if(!action.equals("shopping"))
                openingDateBlock.setVisibility(View.GONE);
            openedStorageConditionBlock.setVisibility(View.GONE);
            currentWeightSlider.setEnabled(true); // TODO ?

            if(!action.equals("shopping"))
                currentWeightBlock.setVisibility(View.VISIBLE);
            currentWeightSlider.setVisibility(View.VISIBLE); // TODO ?

            if(currentWeightSlider.getProgress()<currentWeightSlider.getMax() && !openedCheckBox.isChecked()) {
                currentWeightSlider.setProgress(currentWeightSlider.getMax());
                currentWeightSlider.setTag(R.id.percentageValue, 100);
            }

            // Mostra l'ultimo visualizzato
            if(expiryDateMode)
                changeExpiryMode(null);
        }

        if(addListener)
            packagedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializePackagedCheckBox(false));
    }

    private void initializeNoExpiryCheckBox(boolean addListener){
        enableNoExpiryCheckBoxBehaviour(noExpiryCheckbox.isChecked());

        if(addListener)
            noExpiryCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeNoExpiryCheckBox(false));
    }

    private void enableNoExpiryCheckBoxBehaviour(boolean enable){
        expiryDateField.setEnabled(!enable);
        findViewById(R.id.expiryDateClearButton).setEnabled(!enable);
        expiryDaysAfterOpeningField.setEnabled(!enable);
        findViewById(R.id.expiryDaysAfterOpeningClearButton).setEnabled(!enable);
    }

    private void initializeStorageSpinners() {
        ArrayList<String> storageList = new ArrayList<>();
        storageList.add("Temperatura ambiente");
        storageList.add("Frigorifero");
        storageList.add("Congelatore");

        storageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, storageList));
        openedStorageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, storageList));

        if(!action.equals("shopping"))
            storageConditionSpinner.setSelection(getIntent().getIntExtra("filter", 0));
        else
            storageConditionSpinner.setSelection(FRIDGE_SELECTION); // TODO permettere di selezionare il valore di default
        openedStorageConditionSpinner.setSelection(storageConditionSpinner.getSelectedItemPosition());
    }

    private void initializePointsOfPurchaseSpinner() {
        new Thread(() -> {
            List<PointOfPurchase> pointsOfPurchase = productDatabase.pointOfPurchaseDao().getPointsOfPurchase();

            // Aggiungi un prodotto fake che rappresenti la selezione nulla
            PointOfPurchase noSelection = new PointOfPurchase();
            if(pointsOfPurchase.size()>0)
                noSelection.setName("Scegli...");
            else {
                noSelection.setName("Nessun punto di acquisto");
                pointOfPurchaseSpinner.setEnabled(false);
            }
            pointsOfPurchase.add(0, noSelection);
            pointOfPurchaseSpinner.setAdapter(new PointsOfPurchaseSpinnerAdapter(this, R.layout.storage_condition_spinner_item, pointsOfPurchase));
        }).start();
    }

    private void showDateWarning(String previousValue, EditText dateField, String message) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dateField.setTag(R.id.warningEdit, "warningEdit");
                    dateField.setText(previousValue);
                    dateField.setTag(R.id.warningEdit,null);
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
            .setTitle("Attenzione")
            .setPositiveButton("Ok", dialogClickListener)
            .setNegativeButton("Annulla", dialogClickListener)
            .show();
    }

    private void onWeightFocusLost() {
        if(currentWeightField.getText().length() > 0 && weightField.getText().length() > 0) {
            if(TextUtils.getFloat(currentWeightField) > TextUtils.getFloat(weightField)) { // se currentWeight > weight, currentWeight = weight
                currentWeightField.setText("");
                currentWeightField.setHint(weightField.getText().toString());
                Toast.makeText(getApplicationContext(), "Il peso attuale non può superare il peso massimo", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Da chiamare alla perdita del focus di un campo prezzo
    // Formattazione dei campi prezzo alla perdita del focus
    private void onPriceFocusLost(EditText priceField){
        String priceAsString = priceField.getText().toString();
        if(priceAsString.equals("0") || priceAsString.equals("0,") || priceAsString.equals("0,0"))  // se il valore immesso è 0...
            priceField.setText("");                                                                 // rendi il campo vuoto
        else if(priceAsString.indexOf(',')>-1){                                                     // se vi è una virgola e il valore non è 0, aggiungi eventuali zero mancanti ai decimali
            int digitsAfterComma = priceAsString.substring(priceAsString.indexOf(',')).length();
            if (digitsAfterComma == 1) {
                priceField.setText(priceAsString + "00"); // FARE I CONTROLLI E GLI INSERIMENTI IN BASE A MAX_INT_DIGITS
                TextUtils.setSelectionToEnd(priceField);
            } else if (digitsAfterComma == 2) {
                priceField.setText(priceAsString + "0");
                TextUtils.setSelectionToEnd(priceField);
            }
        } else if(priceField.getText().length()>0) {                                                // se non vi è una virgola aggiunge ",00" alle cifre intere
            priceField.setText(priceAsString + ",00");
            TextUtils.setSelectionToEnd(priceField);
        }
    }

    // Modifica il corrispondente campo data
    public void showDatePickerDialog(View v) {
        if(v == expiryDateField){                                   // immissione data con spinner date picker
            DialogFragment f = new SpinnerDatePickerFragment();
            Bundle args = new Bundle();
            //args.putString("tag", v.getTag().toString());
            f.setArguments(args);
            f.show(getSupportFragmentManager(), "spinnerDatePicker");

        } else {                                                    // immissione data con datepicker android
            DialogFragment f = new DatePickerFragment();
            Bundle args = new Bundle();
            args.putString("tag", v.getTag().toString());
            f.setArguments(args);
            f.show(getSupportFragmentManager(), "datePicker");
        }
    }

    // Modifica i pezzi tramite i relativi pulsanti
    public void editPieces(View view) {
        TextUtils.editQuantityByButtons((Button)view, piecesField, MIN_PIECES, MAX_PIECES);
    }

    // Modifica la quantità tramite i relativi pulsanti
    public void editQuantity(View view) {
        TextUtils.editQuantityByButtons((Button)view, quantityField, MIN_QUANTITY, MAX_QUANTITY);
    }

    // Svuota il contenuto del campo corrispondente al pulsante premuto
    public void eraseField(View view) {
        if(view.getTag().toString().equals("name")) {
            nameField.setText("");
            nameField.requestFocus();
        } else if(view.getTag().toString().equals("brand")) {
            brandField.setText("");
            brandField.requestFocus();
        } else if(view.getTag().toString().equals("price")) {
            priceField.setText("");
            priceField.requestFocus();
        } else if(view.getTag().toString().equals("pricePerKilo")) {
            pricePerKiloField.setText("");
            pricePerKiloField.requestFocus();
        } else if(view.getTag().toString().equals("weight")) {
            weightField.setText("");
            weightField.requestFocus();
        } else if(view.getTag().toString().equals("currentWeight")) {
            currentWeightField.setText("");
            currentWeightField.requestFocus();
        } else if(view.getTag().toString().equals("expiryDate")) {
            expiryDateField.setText("");
            expiryDateField.requestFocus();
        } else if(view.getTag().toString().equals("purchaseDate")) {
            purchaseDateField.setText("");
            purchaseDateField.requestFocus();
        } else if(view.getTag().toString().equals("openingDate")) {
            openingDateField.setText("");
            openingDateField.requestFocus();
        } else if(view.getTag().toString().equals("expiryDaysAfterOpening")){
            expiryDaysAfterOpeningField.setText("");
            expiryDaysAfterOpeningField.requestFocus();
        }
    }

    // Sposta il focus su una determinata view
    private final void setFocusAndScrollToView(final View view){
        listScrollView.post(() -> {
            listScrollView.scrollTo(0, view.getTop());
            view.requestFocus();
        });
    }

    public void changeExpiryMode(View view) {
        if(view==changeToExpiryDateButton)
            expiryDateMode = true;
        else if(view==changeToExpiryDaysButton)
            expiryDateMode = false;

        changeToExpiringDateMode(expiryDateBlock.getVisibility()==View.GONE && expiryDaysAfterOpeningBlock.getVisibility()==View.VISIBLE);
    }

    private void changeToExpiringDateMode(boolean expiringDateMode){
        if(expiringDateMode) {
            expiryDateBlock.setVisibility(View.VISIBLE);
            findViewById(R.id.expiryDaysAfterOpeningBlock).setVisibility(View.GONE);
        } else {
            expiryDateBlock.setVisibility(View.GONE);
            findViewById(R.id.expiryDaysAfterOpeningBlock).setVisibility(View.VISIBLE);
        }
    }

    public void endShopping(View view) {
        if(shoppingCart.size()>0){
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        new Thread(() -> {
                            addProducts(shoppingCart);

                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }).start();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            StringBuilder msg = new StringBuilder("Vuoi aggiungere i seguenti prodotti?\n\n");

            for(int i=0; i<shoppingCart.size(); i++){
                msg.append("- ").append(shoppingCart.get(i).getName());
                if(shoppingCart.get(i).getBrand()!=null)
                    msg.append(" ").append(shoppingCart.get(i).getBrand());
                msg.append("\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(msg.toString())
                    .setTitle("Carrello")
                    .setPositiveButton("Aggiungi", dialogClickListener)
                    .setNegativeButton("Annulla", dialogClickListener)
                    .show();
        } else {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    // Non spostare in una classe esterna poichè impossibile chiamare showDateWarning da un contesto statico
    public class DateWatcher implements TextWatcher {
        private EditText dateField;
        String previousDate;

        public DateWatcher(EditText dateField){
            this.dateField = dateField;
        }

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {previousDate = s.toString();}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(dateField.getTag().toString().equals("purchaseDate") && !DateUtils.isDateEmpty(purchaseDateField) && purchaseDateField.getTag(R.id.warningEdit)==null){ // purchaseDate
                if(!DateUtils.isDateEmpty(expiryDateField) && TextUtils.getDate(purchaseDateField).after(TextUtils.getDate(expiryDateField)))
                    showDateWarning(previousDate, purchaseDateField,"La data di acquisto selezionata è uguale o successiva alla data di scadenza, continuare comunque aggiungendo un prodotto già scaduto?");
            } else if(dateField.getTag().toString().equals("openingDate") && !DateUtils.isDateEmpty(openingDateField) && openingDateField.getTag(R.id.warningEdit)==null){ // openingDate
                if(!DateUtils.isDateEmpty(expiryDateField) && TextUtils.getDate(openingDateField).after(TextUtils.getDate(expiryDateField)))
                    showDateWarning(previousDate, openingDateField,"La data di apertura selezionata è uguale o successiva alla data di scadenza, continuare comunque aggiungendo un prodotto già scaduto?");
            } else if(dateField.getTag().toString().equals("expiryDate") && !DateUtils.isDateEmpty(expiryDateField) && expiryDateField.getTag(R.id.warningEdit)==null){ // expiryDate
                if(TextUtils.getDate(expiryDateField).before(Calendar.getInstance().getTime()))
                    showDateWarning(previousDate, expiryDateField,"La data di scadenza selezionata è uguale o precedente alla data odierna, continuare comunque aggiungendo un prodotto già scaduto?");
                else if(!DateUtils.isDateEmpty(openingDateField) && TextUtils.getDate(expiryDateField).before(TextUtils.getDate(openingDateField)))
                    showDateWarning(previousDate, expiryDateField,"La data di scadenza selezionata è uguale o precedente alla data di apertura, continuare comunque aggiungendo un prodotto già scaduto?");
                else if(!DateUtils.isDateEmpty(purchaseDateField) && TextUtils.getDate(expiryDateField).before(TextUtils.getDate(purchaseDateField)))
                    showDateWarning(previousDate, expiryDateField,"La data di scadenza selezionata è uguale o precedente alla data di acquisto, continuare comunque aggiungendo un prodotto già scaduto?");
            }
        }
    }
}
