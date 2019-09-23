package com.example.robertotarullo.myfridge.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.robertotarullo.myfridge.adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.bean.ProductForm;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.adapter.PointsOfPurchaseSpinnerAdapter;
import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.database.DatabaseUtils;
import com.example.robertotarullo.myfridge.database.ProductDatabase;
import com.example.robertotarullo.myfridge.fragment.SpinnerDatePickerFragment;
import com.example.robertotarullo.myfridge.watcher.CurrentWeightSliderListener;
import com.example.robertotarullo.myfridge.watcher.DateWatcher;
import com.example.robertotarullo.myfridge.watcher.PiecesWatcher;
import com.example.robertotarullo.myfridge.watcher.QuantityWatcher;
import com.example.robertotarullo.myfridge.utils.DateUtils;
import com.example.robertotarullo.myfridge.utils.TextUtils;
import com.example.robertotarullo.myfridge.fragment.DatePickerFragment;
import com.example.robertotarullo.myfridge.filter.DaysInputFilter;
import com.example.robertotarullo.myfridge.filter.PriceInputFilter;
import com.example.robertotarullo.myfridge.filter.WeightInputFilter;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.watcher.PriceWeightRelationWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EditProduct extends AppCompatActivity {

    // Variabili statiche
    public static final int FREEZER_MAX_CELSIUS = 0, FRIDGE_MIN_CELSIUS = 3, FRIDGE_MAX_CELSIUS = 6;
    public static final int FRIDGE_DEFAULT_CELSIUS = 5, FREEZER_DEFAULT_CELSIUS = -18, ROOM_DEFAULT_CELSIUS = 20;  // ideale (4-6) per frigo
    public static final int ROOM_SELECTION = 0, FRIDGE_SELECTION = 1, FREEZER_SELECTION = 2;
    public static final int MAX_QUANTITY = 99, MIN_QUANTITY = 1, MAX_PIECES = 99, MIN_PIECES = 1;

    // Intent
    private String action;
    private ProductForm startingForm;
    private long productToModifyId;

    // Variabili per i suggerimenti dei campi
    private Set<String> nameSuggestionsList, brandSuggestionsList;

    // views
    private ScrollView listScrollView;
    private EditText nameField, brandField, pricePerKiloField, priceField, weightField, purchaseDateField, expiryDateField, openingDateField, expiryDaysAfterOpeningField, currentWeightField, packagingDateField, consumptionDateField;
    private Spinner storageConditionSpinner, openedStorageConditionSpinner, pointOfPurchaseSpinner;
    private CheckBox openedCheckBox, packagedCheckBox, noExpiryCheckbox, consumedCheckBox;
    private Button confirmButton, priceClearButton, pricePerKiloClearButton, weightClearButton, changeToExpiryDaysButton, changeToExpiryDateButton, addQuantityButton, subtractQuantityButton, addPieceButton, subtractPieceButton;
    private SeekBar currentWeightSlider;
    private TextView storageConditionSpinnerLabel, quantityField, piecesField, currentPiecesField, expiryDaysAfterOpeningLabel;

    // variabili di controllo del form
    private boolean expiryDateMode;

    // dichiarazione dei blocchi che hanno regole per la visibilità
    private LinearLayout openingDateBlock, expiryDateBlock, openedCheckBoxBlock, openedStorageConditionBlock, currentWeightBlock, quantityBlock, expiryDaysAfterOpeningBlock, pointOfPurchaseBlock, purchaseDateBlock, consumedCheckboxBlock, consumptionDateBlock, openedBlock;

    // dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Ottieni un riferimento al db
        productDatabase = DatabaseUtils.getDatabase(getApplicationContext());

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
        packagingDateField = findViewById(R.id.packagingDateField);
        expiryDaysAfterOpeningField = findViewById(R.id.expiryDaysAfterOpeningField);
        openedCheckBox = findViewById(R.id.openedCheckBox);
        currentWeightSlider = findViewById(R.id.currentWeightSlider);
        quantityField = findViewById(R.id.quantityField);
        piecesField = findViewById(R.id.piecesField);
        currentPiecesField = findViewById(R.id.currentPiecesField);
        noExpiryCheckbox = findViewById(R.id.noExpiryCheckbox);
        consumedCheckBox = findViewById(R.id.consumedCheckBox);
        consumptionDateField = findViewById(R.id.consumptionDateField);

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
        openingDateBlock = findViewById(R.id.openingDateBlock);
        expiryDateBlock = findViewById(R.id.expiryDateBlock);
        openedCheckBoxBlock = findViewById(R.id.openedCheckBoxBlock);
        openedStorageConditionBlock = findViewById(R.id.openedStorageConditionBlock);
        currentWeightBlock = findViewById(R.id.currentWeightBlock);
        quantityBlock = findViewById(R.id.quantityBlock);
        expiryDaysAfterOpeningBlock = findViewById(R.id.expiryDaysAfterOpeningBlock);
        pointOfPurchaseBlock = findViewById(R.id.pointOfPurchaseBlock);
        purchaseDateBlock = findViewById(R.id.purchaseDateBlock);
        consumedCheckboxBlock = findViewById(R.id.consumedCheckBoxBlock);
        consumptionDateBlock = findViewById(R.id.consumptionDateBlock);
        openedBlock = findViewById(R.id.openedBlock);

        // riferimenti ai pulsanti clear di campi coinvolti in relazioni
        priceClearButton = findViewById(R.id.priceClearButton);
        pricePerKiloClearButton = findViewById(R.id.pricePerKiloClearButton);
        weightClearButton = findViewById(R.id.weightClearButton);

        action = getIntent().getStringExtra("action");

        // inizializza gli array per i suggerimenti
        initializeSuggestions();

        // Popola spinners
        initializeStorageSpinners();

        // variabili di controllo del form
        expiryDateMode = false; // TODO configurabile: valore iniziale a preferenza dell'utente

        // Comportamenti delle checkbox
        initializeOpenedCheckBox(true);
        initializePackagedCheckBox(true);
        initializeNoExpiryCheckBox(true);
        initializeConsumedCheckBox(true);

        // Validazione e comportamento
        currentWeightSlider.setTag(R.id.percentageValue, "100");
        // TODO passare context al posto delle view fisse
        priceField.addTextChangedListener(new PriceWeightRelationWatcher(priceField.getTag().toString(), pricePerKiloField, weightField, pricePerKiloClearButton, weightClearButton, this));
        pricePerKiloField.addTextChangedListener(new PriceWeightRelationWatcher(pricePerKiloField.getTag().toString(), priceField, weightField, priceClearButton, weightClearButton, this));
        weightField.addTextChangedListener(new PriceWeightRelationWatcher(weightField.getTag().toString(), priceField, pricePerKiloField, priceClearButton, pricePerKiloClearButton, this));
        purchaseDateField.addTextChangedListener(new DateWatcher(purchaseDateField, this));
        openingDateField.addTextChangedListener(new DateWatcher(openingDateField, this));
        expiryDateField.addTextChangedListener(new DateWatcher(expiryDateField, this));
        packagingDateField.addTextChangedListener(new DateWatcher(packagingDateField, this));
        currentWeightSlider.setOnSeekBarChangeListener(new CurrentWeightSliderListener(weightField, currentWeightField, piecesField, currentPiecesField));
        quantityField.addTextChangedListener(new QuantityWatcher(addQuantityButton, subtractQuantityButton));
        piecesField.addTextChangedListener(new PiecesWatcher(this));
        // currentPiecesField.addTextChangedListener(new CurrentPiecesWatcher(piecesField, currentPiecesBlock));

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
        expiryDaysAfterOpeningField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) validateExpiryDate(); });

        switch (action) {
            case "add":
                initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch

                initializeFormLabels("Aggiungi prodotto", "Aggiungi");

                findViewById(R.id.currentPiecesFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
                currentPiecesField.setVisibility(View.GONE);
                findViewById(R.id.currentWeightFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
                currentWeightField.setVisibility(View.GONE);
                findViewById(R.id.currentWeightSliderLabel).setVisibility(View.VISIBLE);

                setCurrentFormToInitial();
                break;
            case "edit":
                initializeFormLabels("Modifica prodotto", "Salva");
                productToModifyId = getIntent().getLongExtra("id", 0);

                // Nascondi/mostra i campi
                quantityBlock.setVisibility(View.GONE);
                findViewById(R.id.consumedCheckBoxBlock).setVisibility(View.VISIBLE);

                new Thread(() -> {
                    initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                    SingleProduct p = productDatabase.productDao().get(productToModifyId);
                    runOnUiThread(() -> {
                        fillFieldsFromProduct(p);
                        setCurrentFormToInitial();
                    });
                }).start();
                break;
            case "update":
                initializeFormLabels("Aggiorna prodotto", "Salva");
                productToModifyId = getIntent().getLongExtra("id", 0);

                // Disabilita/nascondi i campi non inerenti all'aggiornamento di stato
                findViewById(R.id.consumeProduct).setVisibility(View.VISIBLE);
                quantityBlock.setVisibility(View.GONE);
                findViewById(R.id.nameFieldsBlock).setVisibility(View.GONE); // TODO mostrare in modo diverso
                findViewById(R.id.piecesBlock).setVisibility(View.GONE);
                findViewById(R.id.packagedCheckBoxBlock).setVisibility(View.GONE);
                findViewById(R.id.packagedBlock).setBackground(null);
                findViewById(R.id.packagedBlock).setPadding(0, 0, 0, 0);
                findViewById(R.id.datesBlock).setVisibility(View.GONE);
                findViewById(R.id.priceWeightBlock).setVisibility(View.GONE);
                findViewById(R.id.storageConditionsBlock).setVisibility(View.GONE);
                pointOfPurchaseBlock.setVisibility(View.GONE);

                new Thread(() -> {
                    initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                    SingleProduct p = productDatabase.productDao().get(productToModifyId);
                    runOnUiThread(() -> {
                        fillFieldsFromProduct(p);
                        setCurrentFormToInitial();
                    });
                }).start();
                break;
            case "shopping":
                if (getIntent().getSerializableExtra("productToEdit") != null) {
                    initializeFormLabels("Modifica prodotto", "Salva");
                    quantityField.setText(String.valueOf(getIntent().getIntExtra("quantity", 1)));
                    new Thread(() -> {
                        initializePointsOfPurchaseSpinner();
                        runOnUiThread(() -> fillFieldsFromProduct((SingleProduct) getIntent().getSerializableExtra("productToEdit")));
                    }).start();
                } else if (getIntent().getSerializableExtra("cartProducts") != null) {
                    new Thread(() -> {
                        initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                        if (addProducts((List<SingleProduct>) getIntent().getSerializableExtra("cartProducts")) > 0) { // TODO spostare new thread in addproducts()?
                            runOnUiThread(() -> { // TODO è necessario l'ui thread?
                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            });
                        }
                    }).start();
                } else {
                    initializeFormLabels("Aggiungi prodotto", "Aggiungi");
                }

                // Nascondi i campi precompilati
                pointOfPurchaseBlock.setVisibility(View.GONE);
                purchaseDateBlock.setVisibility(View.GONE);
                openedBlock.setVisibility(View.GONE);
                currentWeightBlock.setVisibility(View.GONE);
                consumedCheckboxBlock.setVisibility(View.GONE);

                setCurrentFormToInitial();
                break;
        }
    }

    private void setCurrentFormToInitial(){
        startingForm = getCurrentForm();
    }

    private void initializeFormLabels(String title, String confirmButtonText){
        setTitle(title);
        confirmButton.setText(confirmButtonText);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if(action.equals("edit") || action.equals("update"))
            menu.add(0, R.id.resetConsumption, Menu.NONE, "Resetta solo consumazione ");
        if(action.equals("edit")) {
            menu.add(0, R.id.reset, Menu.NONE, "Resetta");
            menu.add(0, R.id.delete, Menu.NONE, "Elimina");
        }
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
            case R.id.reset:
                dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            hardReset();
                            /*new Thread(() -> {
                                // modifica su db ?
                            }).start();*/
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                msg = "Vuoi ripristinare lo stato del prodotto? Verrano resettati tutti i campi relativi all'apertura e alla consumazione. Inoltre verrano resettati:\n\n" +
                      "- Data di confezionamento\n- Data di acquisto\n- Data di scadenza\n- Punto di acquisto";

                builder = new AlertDialog.Builder(this);
                builder.setMessage(msg)
                        .setTitle("Conferma ripristino (Hard reset)")
                        .setPositiveButton("Conferma", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();
                return true;
            case R.id.resetConsumption:
                dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            softReset();
                            /*new Thread(() -> {
                                // modifica su db ?
                            }).start();*/
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                msg = "Vuoi ripristinare lo stato di consumazione del prodotto? Verrano resettati tutti i campi relativi all'apertura e alla consumazione.";

                builder = new AlertDialog.Builder(this);
                builder.setMessage(msg)
                        .setTitle("Conferma ripristino (Soft reset)")
                        .setPositiveButton("Conferma", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hardReset(){
        softReset();

        packagingDateField.setText(""); // Resetta data di confezionamento
        purchaseDateField.setText(""); // Resetta data di acquisto
        noExpiryCheckbox.setChecked(false); // Resetta data di scadenza
        expiryDateField.setText("");
        pointOfPurchaseSpinner.setSelection(0); // Resetta punto di acquisto // TODO resetta solo se si tratta di un prodotto che si può comprare anche altrove ?
    }

    private void softReset(){
        // Resetta consumazione
        consumptionDateField.setText("");
        consumedCheckBox.setChecked(false);

        // Resetta apertura
        openingDateField.setText("");
        openedCheckBox.setChecked(false);

        // Resetta slider
        currentWeightSlider.setTag(R.id.percentageValue, "100");
        currentWeightSlider.setProgress(currentWeightSlider.getMax());
    }

    @Override
    public void onBackPressed() {
        String msg = null;

        if(!startingForm.equals(getCurrentForm()))
            msg = "Sono stati modificati alcuni campi, sei sicuro di voler uscire senza salvare?";

        if(msg!=null){
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
            builder.setMessage(msg)
                    .setTitle("Attenzione")
                    .setPositiveButton("Esci", dialogClickListener)
                    .setNegativeButton("Annulla", dialogClickListener)
                    .show();
        } else
            super.onBackPressed();
    }

    private ProductForm getCurrentForm(){
        return new ProductForm(createProductFromFields(), TextUtils.getInt(quantityField), TextUtils.getDate(expiryDateField), TextUtils.getInt(expiryDaysAfterOpeningField));
    }

    private void initializeSuggestions(){
        nameSuggestionsList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        brandSuggestionsList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        new Thread(() -> {
            List<SingleProduct> products = productDatabase.productDao().getAll(); // TODO Prendi tutti i prodotti non uguali
            if(getIntent().getSerializableExtra("suggestions")!=null){ // Se si tratta di un'aggiunta o di una modifica
                List<SingleProduct> cartProducts = (List<SingleProduct>) getIntent().getSerializableExtra("suggestions");
                products.addAll(cartProducts);
            }
            runOnUiThread(() -> addSuggestions(products));
        }).start();
    }

    private void addSuggestions(SingleProduct suggestion){
        List<SingleProduct> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        addSuggestions(suggestions);
    }

    private void addSuggestions(List<SingleProduct> suggestions){
        for(int i=0; i<suggestions.size(); i++){
            if(suggestions.get(i).getName()!=null)
                nameSuggestionsList.add(suggestions.get(i).getName());
            if(suggestions.get(i).getBrand()!=null)
                brandSuggestionsList.add(suggestions.get(i).getBrand());
        }

        ((AutoCompleteTextView)nameField).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nameSuggestionsList.toArray()));
        ((AutoCompleteTextView)brandField).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, brandSuggestionsList.toArray()));
    }

    // Compila tutti i campi con i dati del prodotto da modificare
    private void fillFieldsFromProduct(SingleProduct p) {

        // TODO Mettere a fattor comune con codice in case("add"), PiecesWatcher e PriceWeightRelationWatcher
        if(p.getWeight()==0){
            findViewById(R.id.currentWeightFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
            currentWeightField.setVisibility(View.GONE);
        }
        if(p.getPieces()==1){
            findViewById(R.id.currentPiecesFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
            currentPiecesField.setVisibility(View.GONE);
        }
        if(p.getPieces()==1 && p.getWeight()==0)
            findViewById(R.id.currentWeightSliderLabel).setVisibility(View.VISIBLE);

        if(p.isConsumed())
            consumedCheckBox.setChecked(true);

        TextUtils.editFieldNotFromUser(consumptionDateField, DateUtils.getFormattedDate(p.getConsumptionDate()));

        TextUtils.setText(p.getName(), nameField);

        TextUtils.setText(p.getBrand(), brandField);

        TextUtils.setPrice(p.getPrice(), priceField);

        TextUtils.setPrice(p.getPricePerKilo(), pricePerKiloField);

        TextUtils.setWeight(p.getWeight(), weightField);

        if(p.getWeight()>0)
            TextUtils.setWeight(p.getCurrentWeight(), currentWeightField);
        currentWeightSlider.setTag(R.id.percentageValue, String.valueOf(p.getPercentageQuantity()));

        if(p.getExpiringDaysAfterOpening()>0)
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, String.valueOf(p.getExpiringDaysAfterOpening()));

        TextUtils.setDate(p.getPurchaseDate(), purchaseDateField);

        storageConditionSpinner.setSelection(p.getStorageCondition());
        openedStorageConditionSpinner.setSelection(p.getOpenedStorageCondition());

        TextUtils.setDate(p.getPackagingDate(), packagingDateField);

        TextUtils.setPointOfPurchase(p.getPointOfPurchaseId(), pointOfPurchaseSpinner);

        piecesField.setText(String.valueOf(p.getPieces()));

        currentPiecesField.setText(String.valueOf(p.getCurrentPieces()));

        if(p.getExpiryDate()!=null) {
            if(p.getExpiryDate().equals(DateUtils.getNoExpiryDate())) {
                noExpiryCheckbox.setChecked(true);
                TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, "");
            } else {
                TextUtils.editFieldNotFromUser(expiryDateField, DateUtils.getFormattedDate(p.getExpiryDate()));
                if(!p.isPackaged())
                    changeToExpiringDateMode(true); // Mostra 'data di scadenza' e nascondi 'giorni entro cui consumare'
            }
        }

        // Se si tratta di un prodotto confezionato
        if(p.isPackaged()){
            packagedCheckBox.setChecked(true);

            // Se si tratta di un prodotto aperto confezionato
            if(p.isOpened()) {
                openedCheckBox.setChecked(true);
                if(p.getOpeningDate()!=null)
                    TextUtils.editFieldNotFromUser(openingDateField, DateUtils.getFormattedDate(p.getOpeningDate()));
            }
        }

        // Se si tratta di un prodotto fresco o confezionato aperto
        if(p.isOpened()){
            if(p.getPieces()==1){
                if(TextUtils.isEmpty(weightField))
                    currentWeightSlider.setProgress(p.getPercentageQuantity());
                else{
                    currentWeightSlider.setTag("currentWeight");
                    currentWeightSlider.setMax(TextUtils.getInt(weightField));
                    currentWeightSlider.setProgress(TextUtils.getInt(currentWeightField));
                }
            } else {
                currentWeightSlider.setTag("pieces");
                currentWeightSlider.setMax(p.getPieces());
                currentWeightSlider.setProgress(p.getCurrentPieces());
            }
        }
    }

    // Metodo chiamato alla pressione del tasto di conferma, che può essere l'aggiunta o la modifica del prodotto
    public void onConfirmButtonClick(View view) {
        // esegui tutte le funzioni della perdita del focus per avere il valore corretto effettivo
        onWeightFocusLost();
        onPriceFocusLost(priceField);
        onPriceFocusLost(pricePerKiloField);
        // TODO perdita focus giorni rimanenti ?

        // Il campo nome è obbligatorio
        if (TextUtils.isEmpty(nameField)) {
            Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
            setFocusAndScrollToView(findViewById(R.id.nameBlock));
        } else {
            SingleProduct newProduct = createProductFromFields();

            new Thread(() -> {
                int insertCount = 0; // counter inserimenti
                Intent resultIntent = new Intent();

                switch (action) {
                    case "update":  // Se si tratta di un aggiornamento
                        newProduct.setId(productToModifyId);
                        if (productDatabase.productDao().update(newProduct) > 0) {
                            insertCount = 1;
                            String msg = "Prodotti aggiornati: " + insertCount + "\nProdotti non aggiornati: " + (1 - insertCount); // TODO adattare a update
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                        }
                        break;
                    case "edit":  // Se si tratta di una modifica
                        newProduct.setId(productToModifyId);
                        if (productDatabase.productDao().update(newProduct) > 0) {
                            insertCount = 1;
                            String msg = "Prodotti modificati: " + insertCount + "\nProdotti non modificati: " + (1 - insertCount); // TODO adattare a edit
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                        }
                        break;
                    case "add":  // Se si tratta di un'aggiunta
                        List<SingleProduct> productsToAdd = new ArrayList<>();
                        for (int i = 0; i < TextUtils.getInt(quantityField); i++)
                            productsToAdd.add(newProduct);
                        insertCount = addProducts(productsToAdd);
                        break;
                    case "shopping":  // Se si tratta della modalità spesa
                        if (getIntent().getSerializableExtra("productToEdit") != null) {
                            resultIntent.putExtra("quantity", TextUtils.getInt(quantityField));
                            resultIntent.putExtra("position", getIntent().getIntExtra("position", 0));
                            resultIntent.putExtra("editedProduct", newProduct);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            resultIntent.putExtra("newProduct", newProduct);
                            resultIntent.putExtra("quantity", TextUtils.getInt(quantityField));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                        break;
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

    // TODO mettere a fattor comune con lo stesso metodo in MainAcitvity
    public void consumeProduct(View view) {
        SingleProduct newProduct = createProductFromFields();
        newProduct.setId(productToModifyId);
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    new Thread(() -> {
                        newProduct.setConsumed(true);
                        newProduct.setConsumptionDate(DateUtils.getCurrentDateWithoutTime());

                        if (productDatabase.productDao().update(newProduct) > 0) {
                            runOnUiThread(() -> {
                                Intent resultIntent = new Intent();
                                Toast.makeText(getApplicationContext(), "Prodotto settato come consumato", Toast.LENGTH_LONG).show();
                                resultIntent.putExtra("filter", newProduct.getActualStorageCondition());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            });
                        }
                    }).start();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };

        String msg = "Vuoi segnare come consumato il prodotto \"" + newProduct.getName() + "\"?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle("Conferma consumazione")
                .setPositiveButton("Conferma", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    // metodo usato per il debug/test
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
    private SingleProduct createProductFromFields() {
        SingleProduct p = new SingleProduct();

        if (consumedCheckBox.isChecked()) {
            p.setConsumed(true);
            p.setConsumptionDate(TextUtils.getDate(consumptionDateField));
        }

        p.setName(nameField.getText().toString());

        if (!TextUtils.isEmpty(brandField))
            p.setBrand(brandField.getText().toString());

        if (!TextUtils.isEmpty(priceField) && priceField.isEnabled())
            p.setPrice(TextUtils.getFloat(priceField));

        if (!TextUtils.isEmpty(weightField) && weightField.isEnabled())
            p.setWeight(TextUtils.getFloat(weightField));

        if (!TextUtils.isEmpty(currentWeightField))
            p.setCurrentWeight(TextUtils.getFloat(currentWeightField));

        if (!TextUtils.isEmpty(pricePerKiloField) && pricePerKiloField.isEnabled())
            p.setPricePerKilo(TextUtils.getFloat(pricePerKiloField));

        p.setPieces(TextUtils.getInt(piecesField));

        if (action.equals("shopping") && getIntent().getSerializableExtra("productToEdit") == null){
            p.setPurchaseDate(DateUtils.getCurrentDateWithoutTime()); // TODO settare anche l'ora se implementata
            p.setPointOfPurchaseId(getIntent().getLongExtra("pointOfPurchaseId", 0));
        } else {
            if(TextUtils.getDate(purchaseDateField)!=null)
                p.setPurchaseDate(TextUtils.getDate(purchaseDateField));
            if(pointOfPurchaseSpinner.getSelectedItemPosition()>0)
                p.setPointOfPurchaseId(((PointOfPurchase)pointOfPurchaseSpinner.getSelectedItem()).getId());
        }

        p.setPackagingDate(TextUtils.getDate(packagingDateField));

        p.setStorageCondition(storageConditionSpinner.getSelectedItemPosition());

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

            if(p.getPackagingDate()!=null)
                p.setOpeningDate(p.getPackagingDate());
            else if(p.getPurchaseDate()!=null)
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
            noExpiryCheckbox.setVisibility(View.VISIBLE);
            storageConditionSpinnerLabel.setText("Modalità di conservazione prima dell'apertura");
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare dopo l'apertura");

            if(!action.equals("shopping"))
                openedBlock.setVisibility(View.VISIBLE);

            if(!action.equals("update")){
                expiryDateBlock.setVisibility(View.VISIBLE);
                expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
                openedStorageConditionBlock.setVisibility(View.VISIBLE);
            }

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

            if(!action.equals("shopping")){
                openedBlock.setVisibility(View.GONE);
                openingDateBlock.setVisibility(View.GONE);
                currentWeightBlock.setVisibility(View.VISIBLE);
            }

            openedStorageConditionBlock.setVisibility(View.GONE);
            currentWeightSlider.setEnabled(true); // TODO ?
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

    private void initializeConsumedCheckBox(boolean addListener){
        if(consumedCheckBox.isChecked())
            consumptionDateBlock.setVisibility(View.VISIBLE);
        else
            consumptionDateBlock.setVisibility(View.GONE);

        if(addListener)
            consumedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeConsumedCheckBox(false));
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

        if (action.equals("shopping"))
            storageConditionSpinner.setSelection(FRIDGE_SELECTION); // TODO permettere di selezionare il valore di default
        else
            storageConditionSpinner.setSelection(getIntent().getIntExtra("filter", FRIDGE_SELECTION));
        openedStorageConditionSpinner.setSelection(storageConditionSpinner.getSelectedItemPosition());
    }

    private void initializePointsOfPurchaseSpinner() {
        List<PointOfPurchase> pointsOfPurchase = productDatabase.pointOfPurchaseDao().getPointsOfPurchase();
        runOnUiThread(() -> {
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
        });
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
                priceField.setText(priceAsString + "00"); // TODO FARE I CONTROLLI E GLI INSERIMENTI IN BASE A MAX_INT_DIGITS
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

    // immissione data con spinner date picker
    public void showSpinnerDatePickerDialog(View v){
        DialogFragment f = new DatePickerFragment();
        //DialogFragment f = new SpinnerDatePickerFragment(); // TODO risolvere bug e usarlo al posto di datepickerfragment
        Bundle args = new Bundle();
        args.putInt("dateFieldId", v.getId());
        args.putBoolean("spinnerMode", true);
        f.setArguments(args);
        f.show(getSupportFragmentManager(), "spinnerDatePicker");
    }

    // immissione data con datepicker android
    public void showDatePickerDialog(View v) {
        DialogFragment f = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("dateFieldId", v.getId());
        args.putBoolean("spinnerMode", false);
        f.setArguments(args);
        f.show(getSupportFragmentManager(), "datePicker");
    }

    // Modifica i pezzi tramite i relativi pulsanti
    public void editPieces(View view) {
        TextUtils.editQuantityByButtons((Button)view, piecesField, MIN_PIECES, MAX_PIECES);
    }

    // Modifica la quantità tramite i relativi pulsanti
    public void editQuantity(View view) {
        TextUtils.editQuantityByButtons((Button)view, quantityField, MIN_QUANTITY, MAX_QUANTITY);
    }

    // Svuota il contenuto del primo campo di tipo edittext corrispondente al pulsante premuto
    public void clearField(View view) {
        TextUtils.clearField(view);
    }

    // Sposta il focus su una determinata view
    private void setFocusAndScrollToView(final View view){
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
        validateExpiryDate();
    }

    private void changeToExpiringDateMode(boolean expiringDateMode){
        View expiryDaysAfterOpeningBlock = findViewById(R.id.expiryDaysAfterOpeningBlock);

        if(expiringDateMode) {
            expiryDateBlock.setVisibility(View.VISIBLE);
            expiryDaysAfterOpeningBlock.setVisibility(View.GONE);
        } else {
            expiryDateBlock.setVisibility(View.GONE);
            expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
        }
    }

    // Trigger manuale dell'onchange per mostrare eventuali warning sulla scadenza
    // Considera sia expiryDays che expiryDate
    private void validateExpiryDate(){
        String temp = expiryDateField.getText().toString();
        expiryDateField.setTag(R.id.expirySwitchControl, DateUtils.EXPIRY_SWITCH_CONTROL_TAG);
        expiryDateField.setText(temp); // TODO controllare date illegali calcolate con expiryDays
    }
}
