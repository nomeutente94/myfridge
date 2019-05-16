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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.DateSpinnerAdapter;
import com.example.robertotarullo.myfridge.Adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.Bean.ProductForm;
import com.example.robertotarullo.myfridge.Bean.Pack;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.Adapter.PointsOfPurchaseSpinnerAdapter;
import com.example.robertotarullo.myfridge.Bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.Database.DatabaseUtils;
import com.example.robertotarullo.myfridge.Database.ProductDatabase;
import com.example.robertotarullo.myfridge.Listener.CurrentWeightSliderListener;
import com.example.robertotarullo.myfridge.Listener.DateSpinnerListener;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;
import com.example.robertotarullo.myfridge.Fragment.DatePickerFragment;
import com.example.robertotarullo.myfridge.InputFilter.DaysInputFilter;
import com.example.robertotarullo.myfridge.InputFilter.PriceInputFilter;
import com.example.robertotarullo.myfridge.InputFilter.WeightInputFilter;
import com.example.robertotarullo.myfridge.Utils.PriceUtils;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.TextWatcher.PriceWeightRelationWatcher;
import com.example.robertotarullo.myfridge.Listener.StorageConditionSpinnerListener;

import java.util.ArrayList;
import java.util.List;

public class AddProduct extends AppCompatActivity {

    // variabili statiche
    public static final int FREEZER_MAX_CELSIUS = 0, FRIDGE_MIN_CELSIUS = 3, FRIDGE_MAX_CELSIUS = 6;
    public static final int FRIDGE_DEFAULT_CELSIUS = 5, FREEZER_DEFAULT_CELSIUS = -18, ROOM_DEFAULT_CELSIUS = 20;  // ideale (4-6) per frigo
    public static final int ROOM_SELECTION = 0, FRIDGE_SELECTION = 1, FREEZER_SELECTION = 2;
    private static final int MAX_QUANTITY = 99, MIN_QUANTITY = 1, MAX_PIECES = 99, MIN_PIECES = 1;

    // intent
    private String action;
    private ProductForm startingForm;

    // views
    private ScrollView listScrollView;
    private EditText nameField, brandField, pricePerKiloField, priceField, weightField, purchaseDateField, openingDateField, expiryDaysAfterOpeningField, currentWeightField, packNameField;
    private Spinner storageConditionSpinner, openedStorageConditionSpinner, pointOfPurchaseSpinner, expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner;
    private CheckBox openedCheckBox, differentStorageConditionAfterOpeningCheckBox, packCheckBox, packagedCheckBox;
    private Button confirmButton, priceClearButton, pricePerKiloClearButton, weightClearButton;
    private SeekBar currentWeightSlider;
    private TextView storageConditionSpinnerLabel, quantityField, piecesField, currentPiecesField, expiryDaysAfterOpeningLabel, illegalExpiryDateTextView;
    private List<String> openedStorageList;
    private StorageSpinnerArrayAdapter storageSpinnerAdapter;

    // dichiarazione dei blocchi che hanno regole per la visibilità
    private LinearLayout pricePerKiloBlock, openingDateBlock, expiryDateBlock, openedCheckBoxBlock, openedStorageConditionBlock, currentWeightBlock, differentStorageConditionAfterOpeningCheckBoxBlock, quantityBlock, packCheckBoxBlock, packNameBlock, currentPiecesBlock;

    // dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    @Override
    public void onBackPressed() {
        // chiedere conferma all'utente se tornare all'attività chiamante nel caso qualche campo sia stato modificato
        ProductForm currentForm = new ProductForm(createProductFromFields(), packNameField.getText().toString(), TextUtils.getInt(quantityField));
        if(!startingForm.equals(currentForm)){
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Ottieni un riferimento al db
        productDatabase = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class, DatabaseUtils.DATABASE_NAME).build();

        // riferimenti ai campi
        packagedCheckBox = findViewById(R.id.packagedCheckBox);
        packNameField = findViewById(R.id.packageNameField);
        packCheckBox = findViewById(R.id.packCheckBox);
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
        expiryDateDaySpinner = findViewById(R.id.expiryDateDaySpinner);
        expiryDateMonthSpinner = findViewById(R.id.expiryDateMonthSpinner);
        expiryDateYearSpinner = findViewById(R.id.expiryDateYearSpinner);
        expiryDaysAfterOpeningField = findViewById(R.id.expiryDaysAfterOpeningField);
        openedCheckBox = findViewById(R.id.openedCheckBox);
        currentWeightSlider = findViewById(R.id.currentWeightSlider);
        differentStorageConditionAfterOpeningCheckBox = findViewById(R.id.differentStorageConditionAfterOpeningCheckBox);
        quantityField = findViewById(R.id.quantityField);
        piecesField = findViewById(R.id.piecesField);
        currentPiecesField = findViewById(R.id.currentPiecesField);
        expiryDaysAfterOpeningLabel = findViewById(R.id.expiryDaysAfterOpeningFieldLabel);
        illegalExpiryDateTextView = findViewById(R.id.illegalExpiryDateTextView);

        // riferimenti ad altre view
        listScrollView = findViewById(R.id.listScrollView);
        storageConditionSpinnerLabel = findViewById(R.id.storageConditionSpinnerLabel);
        confirmButton = findViewById(R.id.addButton);

        // riferimenti ai blocchi che hanno regole per la visibilità
        currentPiecesBlock = findViewById(R.id.currentPiecesBlock);
        pricePerKiloBlock = findViewById(R.id.pricePerKiloBlock);
        openingDateBlock = findViewById(R.id.openingDateBlock);
        expiryDateBlock = findViewById(R.id.expiryDateBlock);
        openedCheckBoxBlock = findViewById(R.id.openedCheckBoxBlock);
        openedStorageConditionBlock = findViewById(R.id.openedStorageConditionBlock);
        currentWeightBlock = findViewById(R.id.currentWeightBlock);
        differentStorageConditionAfterOpeningCheckBoxBlock = findViewById(R.id.differentStorageConditionAfterOpeningCheckBoxBlock);
        quantityBlock = findViewById(R.id.quantityBlock);
        packCheckBoxBlock = findViewById(R.id.packCheckBoxBlock);
        packNameBlock = findViewById(R.id.packageNameBlock);

        // riferimenti ai pulsanti clear di campi coinvolti in relazioni
        priceClearButton = findViewById(R.id.priceClearButton);
        pricePerKiloClearButton = findViewById(R.id.pricePerKiloClearButton);
        weightClearButton = findViewById(R.id.weightClearButton);

        // Popola spinners
        initializeStorageSpinners();
        initializePointsOfPurchaseSpinner();
        initializeExpiryDateSpinner();

        // Comportamenti delle checkbox
        initializePackCheckBox();
        packCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializePackCheckBox());

        initializeDifferentStorageConditionAfterOpeningCheckBox();
        differentStorageConditionAfterOpeningCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeDifferentStorageConditionAfterOpeningCheckBox());

        initializeOpenedCheckBox();
        openedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeOpenedCheckBox());

        initializePackagedCheckBox();
        packagedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializePackagedCheckBox());

        // Validazione e comportamento
        currentWeightSlider.setTag(R.id.percentageValue, "100");
        priceField.addTextChangedListener(new PriceWeightRelationWatcher(priceField.getTag().toString(), pricePerKiloField, weightField, pricePerKiloClearButton, weightClearButton, currentWeightField, currentWeightSlider));
        pricePerKiloField.addTextChangedListener(new PriceWeightRelationWatcher(pricePerKiloField.getTag().toString(), priceField, weightField, priceClearButton, weightClearButton, currentWeightField, currentWeightSlider));
        weightField.addTextChangedListener(new PriceWeightRelationWatcher(weightField.getTag().toString(), priceField, pricePerKiloField, priceClearButton, pricePerKiloClearButton, currentWeightField, currentWeightSlider));
        purchaseDateField.addTextChangedListener(new DateWatcher(purchaseDateField));
        openingDateField.addTextChangedListener(new DateWatcher(openingDateField));
        //expiryDateField.addTextChangedListener(new DateWatcher(expiryDateField));
        currentWeightSlider.setOnSeekBarChangeListener(new CurrentWeightSliderListener(weightField, currentWeightField, piecesField, currentPiecesField));

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

        action = getIntent().getStringExtra("action");
        if(action.equals("add")) {
            setTitle("Aggiungi prodotto");
            confirmButton.setText("Aggiungi prodotto");
            startingForm = new ProductForm(createProductFromFields(), packNameField.getText().toString(), TextUtils.getInt(quantityField));
        } else if(action.equals("edit")) {
            setTitle("Modifica prodotto");
            confirmButton.setText("Modifica prodotto");
            quantityBlock.setVisibility(View.GONE);
            fillForm();
        }
    }

    private void initializeExpiryDateSpinner() {
        List<String> days = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> years = new ArrayList<>();

        days.add("DD");
        months.add("MM");
        years.add("YYYY");
        for(int i=1; i<=31; i++){
            if(String.valueOf(i).length()==1)
                days.add("0" + String.valueOf(i));
            else
                days.add(String.valueOf(i));
        }

        for(int i=1; i<=12; i++)
            if(String.valueOf(i).length()==1)
                months.add("0" + String.valueOf(i));
            else
                months.add(String.valueOf(i));
        for(int i=2019; i<=2099; i++)
            years.add(String.valueOf(i));

        expiryDateDaySpinner.setAdapter(new DateSpinnerAdapter(this, R.layout.date_spinner_item, days));
        expiryDateMonthSpinner.setAdapter(new DateSpinnerAdapter(this, R.layout.date_spinner_item, months));
        expiryDateYearSpinner.setAdapter(new DateSpinnerAdapter(this, R.layout.date_spinner_item, years));

        expiryDateDaySpinner.setOnItemSelectedListener(new DateSpinnerListener(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner, findViewById(R.id.illegalExpiryDateTextView)));
        expiryDateMonthSpinner.setOnItemSelectedListener(new DateSpinnerListener(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner, findViewById(R.id.illegalExpiryDateTextView)));
        expiryDateYearSpinner.setOnItemSelectedListener(new DateSpinnerListener(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner, findViewById(R.id.illegalExpiryDateTextView)));
    }

    public void editFieldNotFromUser(EditText dateField, String text){
        dateField.setTag(R.id.warningEdit, "lock");
        dateField.setText(text);
        dateField.setTag(R.id.warningEdit, null);
    }

    // Compila tutti i campi con i dati del prodotto da modificare
    private void fillForm() {
        new Thread(() -> {
            SingleProduct p = productDatabase.productDao().get(getIntent().getLongExtra("id", 0));

            runOnUiThread(() -> {
                packCheckBox.setTag(String.valueOf(p.getPackageId()));

                // Campi immutabili
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
                for(int i=1; i<p.getPieces(); i++)
                    editPieces(true);
                currentPiecesField.setText(String.valueOf(p.getCurrentPieces()));

                // Se si tratta di un prodotto confezionato
                if(p.isPackaged()){
                    packagedCheckBox.setChecked(true);

                    DateUtils.setDate(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner, p.getExpiryDate());

                    // Se si tratta di un prodotto chiuso confezionato
                    if(p.isOpened()) {
                        openedCheckBox.setChecked(true);
                        if(p.getOpeningDate()!=null)
                            editFieldNotFromUser(openingDateField, DateUtils.getFormattedDate(p.getOpeningDate()));
                    }

                    if(p.getStorageCondition()!=p.getOpenedStorageCondition()) {
                        differentStorageConditionAfterOpeningCheckBox.setChecked(true);
                        String openedStorageCondition;
                        if(p.getOpenedStorageCondition()==0)
                            openedStorageCondition = "Temperatura ambiente";
                        else if(p.getOpenedStorageCondition()==1)
                            openedStorageCondition = "Frigorifero";
                        else
                            openedStorageCondition = "Congelatore";
                        if(openedStorageConditionSpinner.getItemAtPosition(0).equals(openedStorageCondition))
                            openedStorageConditionSpinner.setSelection(0);
                        else
                            openedStorageConditionSpinner.setSelection(1);
                    } else {
                        openedStorageConditionSpinner.setSelection(p.getStorageCondition());
                    }
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

                startingForm = new ProductForm(createProductFromFields(), packNameField.getText().toString(), TextUtils.getInt(quantityField));
            });
        }).start();
    }

    // Metodo chiamato alla pressione del tasto di conferma, che può essere l'aggiunta o la modifica del prodotto
    public void onConfirmButtonClick(View view) {
        // esegui tutte le funzioni della perdita del focus per avere il valore corretto effettivo
        onWeightFocusLost();
        onPriceFocusLost(priceField);
        onPriceFocusLost(pricePerKiloField);

        // Il campo nome è obbligatorio, il nome della confezione se si tratta di una confezione
        if(packCheckBox.isChecked() && TextUtils.isEmpty(packNameField)) {
            Toast.makeText(getApplicationContext(), "Il campo nome confezione non può essere vuoto", Toast.LENGTH_LONG).show();
            setFocusAndScrollToView(findViewById(R.id.packageNameBlock));
        } else if (TextUtils.isEmpty(nameField)) {
            Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
            setFocusAndScrollToView(findViewById(R.id.nameBlock));
        } else if(expiryDateDaySpinner.getSelectedItemPosition()>0 && expiryDateMonthSpinner.getSelectedItemPosition()==0 && expiryDateYearSpinner.getSelectedItemPosition()==0){ // Se è stato compilato solo il giorno di scadenza
            illegalExpiryDateTextView.setVisibility(View.VISIBLE);
            //setFocusAndScrollToView(illegalExpiryDateTextView);
        } else if(expiryDateDaySpinner.getSelectedItemPosition()==0 && expiryDateMonthSpinner.getSelectedItemPosition()>0 && expiryDateYearSpinner.getSelectedItemPosition()==0) { // Se è stato compilato solo il mese di scadenza
            illegalExpiryDateTextView.setVisibility(View.VISIBLE);
            //setFocusAndScrollToView(illegalExpiryDateTextView);
        } else if(expiryDateDaySpinner.getSelectedItemPosition()>0 && expiryDateMonthSpinner.getSelectedItemPosition()>0 && expiryDateYearSpinner.getSelectedItemPosition()==0){ // se è stato compilato 29/02 mentre l'anno corrente non è bisestile
            if(!DateUtils.isDateValid(expiryDateDaySpinner.getSelectedItem().toString(), expiryDateMonthSpinner.getSelectedItem().toString(), "2019")){ // TODO settare anno corrente
                illegalExpiryDateTextView.setVisibility(View.VISIBLE);
                //setFocusAndScrollToView(illegalExpiryDateTextView);
            }
        } else if (illegalExpiryDateTextView.getVisibility() == View.VISIBLE) { // TODO Non far dipendere questo controllo dalla visibilità del messaggio di errore, il metodo di avviso potrebbe cambiare in futuro!
            Toast.makeText(getApplicationContext(), "La data di scadenza immessa non è valida", Toast.LENGTH_LONG).show();
            //setFocusAndScrollToView(illegalExpiryDateTextView);
        } else {
            SingleProduct newProduct = createProductFromFields();

            new Thread(() -> {
                int insertCount = 0; // counter inserimenti

                // Se si tratta di una modifica
                if(action.equals("edit")) {
                    newProduct.setId(getIntent().getLongExtra("id", 0));
                    if(packCheckBox.getTag()!=null)
                        newProduct.setPackageId(Long.valueOf(packCheckBox.getTag().toString()));
                    else
                        newProduct.setPackageId(getIntent().getLongExtra("package", 0));
                    productDatabase.productDao().update(newProduct);
                    insertCount++;
                    String msg = "Prodotti modificati: " + insertCount + "\nProdotti non modificati: " + (TextUtils.getInt(quantityField)-insertCount);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                } else {
                    // crea una nuova eventuale confezione
                    if(packCheckBox.isChecked() && TextUtils.getInt(quantityField)>1) {
                        Pack pack = new Pack();
                        pack.setName(packNameField.getText().toString());
                        newProduct.setPackageId(productDatabase.packDao().insertPack(pack));
                    }

                    // inserisci uno o più prodotti
                    for(int i=0; i<TextUtils.getInt(quantityField); i++){
                        if(productDatabase.productDao().insert(newProduct)!=-1)  // se l'inserimento è andato a buon fine
                            insertCount++;  // incrementa counter inserimenti
                    }

                    String msg = "Prodotti aggiunti: " + insertCount + "\nProdotti non aggiunti: " + (TextUtils.getInt(quantityField)-insertCount);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                }

                if(insertCount>0){
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("filter", newProduct.getActualStorageCondition());
                    resultIntent.putExtra("packId", newProduct.getPackageId());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }).start();
        }
    }

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
        System.out.println("packageid: " + p.getPackageId());
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

        // Campi immutabili
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
        if(TextUtils.getDate(purchaseDateField)!=null)
            p.setPurchaseDate(TextUtils.getDate(purchaseDateField));
        p.setStorageCondition(storageConditionSpinner.getSelectedItemPosition());
        if(pointOfPurchaseSpinner.getSelectedItemPosition()>0)
            p.setPointOfPurchaseId(((PointOfPurchase)pointOfPurchaseSpinner.getSelectedItem()).getId());
        if(getIntent().getLongExtra("package", 0)>0) // Se si tratta di un inserimento in una confezione
            p.setPackageId(getIntent().getLongExtra("package", 0));
        if(!TextUtils.isEmpty(expiryDaysAfterOpeningField))
            p.setExpiringDaysAfterOpening(TextUtils.getInt(expiryDaysAfterOpeningField));
        p.setCurrentPieces(TextUtils.getInt(currentPiecesField));

        // campi che dipendono dal tipo e dall'apertura del prodotto confezionato
        if(packagedCheckBox.isChecked()){
            p.setPackaged(true);

            p.setExpiryDate(DateUtils.getExpiryDate(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner));

            if(openedCheckBox.isChecked()) {
                p.setOpened(true);

                if(TextUtils.getDate(openingDateField)!=null)
                    p.setOpeningDate(TextUtils.getDate(openingDateField));

                if(openedStorageConditionSpinner.getSelectedItem().equals("Temperatura ambiente"))
                    p.setOpenedStorageCondition(0);
                else if(openedStorageConditionSpinner.getSelectedItem().equals("Frigorifero"))
                    p.setOpenedStorageCondition(1);
                else if(openedStorageConditionSpinner.getSelectedItem().equals("Congelatore"))
                    p.setOpenedStorageCondition(2);
            } else {
                p.setOpened(false);
            }
        } else { // compilazione dei campi di prodotti confezionati se prodotto non confezionato
            p.setOpened(true);
            p.setOpeningDate(p.getPurchaseDate());
            p.setExpiryDate(DateUtils.getDateByAddingDays(p.getOpeningDate(), p.getExpiringDaysAfterOpening()));
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

        return p;
    }

    private void initializeOpenedCheckBox() {
        if(openedCheckBox.isChecked()) {
            currentWeightBlock.setVisibility(View.VISIBLE);
            openingDateBlock.setVisibility(View.VISIBLE);
            currentWeightSlider.setVisibility(View.VISIBLE);
        } else {
            currentWeightBlock.setVisibility(View.GONE);
            openingDateBlock.setVisibility(View.GONE);
            currentWeightSlider.setVisibility(View.GONE);
        }
    }

    private void initializeDifferentStorageConditionAfterOpeningCheckBox(){
        if(differentStorageConditionAfterOpeningCheckBox.isChecked()) {
            openedStorageConditionSpinner.setEnabled(true);
            storageConditionSpinnerLabel.setText("Modalità di conservazione prima dell'apertura");

            // cancella la nuova voce
            if(storageConditionSpinner.getSelectedItemPosition()==0)
                openedStorageList.remove(0);
            else if(storageConditionSpinner.getSelectedItemPosition()==1)
                openedStorageList.remove(1);
            else if(storageConditionSpinner.getSelectedItemPosition()==2)
                openedStorageList.remove(2);
            storageSpinnerAdapter.notifyDataSetChanged();
            //openedStorageConditionSpinner.setSelection(0);
        } else {
            openedStorageConditionSpinner.setEnabled(false);
            storageConditionSpinnerLabel.setText("Modalità di conservazione");

            // ripristina la voce
            openedStorageList.clear();
            openedStorageList.add("Temperatura ambiente");
            openedStorageList.add("Frigorifero");
            openedStorageList.add("Congelatore");
            storageSpinnerAdapter.notifyDataSetChanged();
            openedStorageConditionSpinner.setSelection(storageConditionSpinner.getSelectedItemPosition());
        }
    }

    private void initializePackCheckBox() {
        TextView nameFieldLabel = findViewById(R.id.nameFieldLabel);
        TextView packagedCheckBoxLabel = findViewById(R.id.packagedCheckBoxLabel);
        TextView openedCheckBoxLabel = findViewById(R.id.openedCheckBoxLabel);
        TextView openingDateFieldLabel = findViewById(R.id.openingDateFieldLabel);
        TextView currentWeightLabel = findViewById(R.id.currentWeightFieldLabel);
        TextView piecesFieldLabel = findViewById(R.id.piecesFieldLabel);
        TextView priceFieldLabel = findViewById(R.id.priceFieldLabel);
        TextView pricePerKiloFieldLabel = findViewById(R.id.pricePerKiloFieldLabel);
        TextView weightLabel = findViewById(R.id.weightFieldLabel);

        if (packCheckBox.isChecked()) {
            packNameField.setEnabled(true);
            nameFieldLabel.setText("Nome (singolo prodotto)");
            priceFieldLabel.setText("Prezzo (singolo prodotto)");
            pricePerKiloFieldLabel.setText("Prezzo/Kg (singolo prodotto)");
            weightLabel.setText("Peso (singolo prodotto)");
            packagedCheckBoxLabel.setText("I prodotti contenuti sono confezionati?");
            openedCheckBoxLabel.setText("E' stato aperto? (!!! MODIFICA OGNI PRODOTTO !!!)");
            openingDateFieldLabel.setText("Data di apertura (!!! MODIFICA OGNI PRODOTTO !!!)");
            currentWeightLabel.setText("Peso attuale (!!! MODIFICA OGNI PRODOTTO !!!)");
            piecesFieldLabel.setText("N. pezzi totali (!!! MODIFICA OGNI PRODOTTO !!!)");
        } else {
            packNameField.setEnabled(false);
            nameFieldLabel.setText("Nome");
            priceFieldLabel.setText("Prezzo");
            pricePerKiloFieldLabel.setText("Prezzo/Kg");
            weightLabel.setText("Peso");
            packagedCheckBoxLabel.setText("E' un prodotto confezionato?");
            openedCheckBoxLabel.setText("E' stato aperto?");
            openingDateFieldLabel.setText("Data di apertura");
            currentWeightLabel.setText("Peso attuale (grammi)");
            piecesFieldLabel.setText("N. pezzi totali");
        }
    }

    private void initializePackagedCheckBox() {
        if(packagedCheckBox.isChecked()){
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare dopo l'apertura");
            expiryDateBlock.setVisibility(View.VISIBLE);
            openedCheckBoxBlock.setVisibility(View.VISIBLE);
            openedStorageConditionBlock.setVisibility(View.VISIBLE);
            storageConditionSpinnerLabel.setText("Modalità di conservazione prima dell'apertura");
            differentStorageConditionAfterOpeningCheckBoxBlock.setVisibility(View.VISIBLE);
            if(!openedCheckBox.isChecked()) {
                currentWeightBlock.setVisibility(View.GONE);
            } else
                openingDateBlock.setVisibility(View.VISIBLE);

            if(currentWeightSlider.getProgress()<currentWeightSlider.getMax())
                openedCheckBox.setChecked(true);
            else
                openedCheckBox.setChecked(false);
        } else {
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare");
            expiryDateBlock.setVisibility(View.GONE);
            openedCheckBoxBlock.setVisibility(View.GONE);
            openingDateBlock.setVisibility(View.GONE);
            openedStorageConditionBlock.setVisibility(View.GONE);
            storageConditionSpinnerLabel.setText("Modalità di conservazione");
            differentStorageConditionAfterOpeningCheckBoxBlock.setVisibility(View.GONE);
            currentWeightSlider.setEnabled(true);
            currentWeightBlock.setVisibility(View.VISIBLE);
            currentWeightSlider.setVisibility(View.VISIBLE);

            if(currentWeightSlider.getProgress()<currentWeightSlider.getMax() && !openedCheckBox.isChecked()) {
                currentWeightSlider.setProgress(currentWeightSlider.getMax());
                currentWeightSlider.setTag(R.id.percentageValue, 100);
            }
        }
    }

    private void initializeStorageSpinners() {
        List<String> storageList = new ArrayList();
        storageList.add("Temperatura ambiente");
        storageList.add("Frigorifero");
        storageList.add("Congelatore");

        storageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, storageList));
        storageConditionSpinner.setSelection(getIntent().getIntExtra("filter", 0));

        openedStorageList = new ArrayList();
        openedStorageList.addAll(storageList);
        storageSpinnerAdapter = new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, openedStorageList);
        openedStorageConditionSpinner.setAdapter(storageSpinnerAdapter);
        storageConditionSpinner.setOnItemSelectedListener(new StorageConditionSpinnerListener(storageSpinnerAdapter, openedStorageList, openedStorageConditionSpinner));
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
        DialogFragment f = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putString("tag", v.getTag().toString());
        f.setArguments(args);
        f.show(getSupportFragmentManager(), "datePicker");
    }

    // Modifica i pezzi tramite i relativi pulsanti
    public void editPieces(boolean add) {
        Button addButton = findViewById(R.id.piecesAddButton);
        Button subtractButton = findViewById(R.id.piecesSubtractButton);
        TextView field = piecesField;
        int min = MIN_PIECES;
        int max = MAX_PIECES;

        if(add)
            TextUtils.editQuantityByButtons(addButton, subtractButton, field, min, max);
        else
            TextUtils.editQuantityByButtons(subtractButton, addButton, field, min, max);

        if(TextUtils.getInt(piecesField)>1){
            // setta lo slider in base al numero di pezzi
            currentWeightSlider.setTag("pieces");

            // calcola il numero di pezzi rimanenti rispetto al valore percentuale
            float currentPiecesAsFloat = (Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()) * TextUtils.getInt(piecesField)) / (float)100;
            int currentPieces = (int) Math.ceil(currentPiecesAsFloat);
            currentWeightSlider.setMax(TextUtils.getInt(piecesField));
            currentWeightSlider.setProgress(currentPieces);
            currentPiecesField.setText(String.valueOf(currentPieces));

            if(!TextUtils.isEmpty(weightField)){
                // calcola il nuovo currentWeight rispetto ai pezzi
                float currentWeightAsFloat = (TextUtils.getInt(weightField) * currentWeightSlider.getProgress()) / (float)TextUtils.getInt(piecesField);
                int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                currentWeightField.setText(String.valueOf(currentWeightAsFloat));
            }

            currentPiecesBlock.setVisibility(View.VISIBLE);

        } else { // setta lo slider in base al peso, se non compilato in percentuale generica
            currentPiecesField.setText(piecesField.getText());
            if(!TextUtils.isEmpty(weightField)){
                // ripristina lo slide rispetto al peso attuale
                currentWeightSlider.setTag("currentWeight");
                // calcola il nuovo currentWeight rispetto al valore percentuale
                float currentWeightAsFloat = (Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()) * TextUtils.getInt(weightField)) / (float)100;
                int currentWeight = (int) Math.ceil(currentWeightAsFloat);
                currentWeightSlider.setMax(TextUtils.getInt(weightField));
                currentWeightSlider.setProgress(currentWeight);
            } else {
                // ripristina lo slide rispetto al valore percentuale
                currentWeightSlider.setTag("percentage");
                currentWeightSlider.setMax(100);
                currentWeightSlider.setProgress(Integer.valueOf(currentWeightSlider.getTag(R.id.percentageValue).toString()));
            }

            currentPiecesBlock.setVisibility(View.GONE);
        }
    }

    // Modifica i pezzi tramite i relativi pulsanti
    public void editPieces(View view) {
        if(view.getTag().toString().equals("add"))
            editPieces(true);
        else if(view.getTag().toString().equals("subtract"))
            editPieces(false);
    }

    // Modifica la quantità tramite i relativi pulsanti
    public void editQuantity(View view) {
        Button addButton = findViewById(R.id.quantityAddButton);
        Button subtractButton = findViewById(R.id.quantitySubtractButton);
        TextView field = quantityField;
        int min = MIN_QUANTITY;
        int max = MAX_QUANTITY;

        if(view.getTag().toString().equals("add")) {
            TextUtils.editQuantityByButtons(addButton, subtractButton, field, min, max);

            if(getIntent().getLongExtra("package", 0)==0) {
                packCheckBoxBlock.setVisibility(View.VISIBLE);
                packNameBlock.setVisibility(View.VISIBLE);
            }
        }
        else if(view.getTag().toString().equals("subtract")) {
            TextUtils.editQuantityByButtons(subtractButton, addButton, field, min, max);
            if(TextUtils.getInt(field)==1) {
                packCheckBoxBlock.setVisibility(View.GONE);
                packNameBlock.setVisibility(View.GONE);
            }
        }
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
            expiryDateDaySpinner.setSelection(0);
            expiryDateMonthSpinner.setSelection(0);
            expiryDateYearSpinner.setSelection(0);
        } else if(view.getTag().toString().equals("purchaseDate")) {
            purchaseDateField.setText("");
            purchaseDateField.requestFocus();
        } else if(view.getTag().toString().equals("openingDate")) {
            openingDateField.setText("");
            openingDateField.requestFocus();
        } else if(view.getTag().toString().equals("expiryDaysAfterOpening")){
            expiryDaysAfterOpeningField.setText("");
            expiryDaysAfterOpeningField.requestFocus();
        } else if(view.getTag().toString().equals("packageName")) {
            packNameField.setText("");
            packNameField.requestFocus();
        }
    }

    // Sposta il focus su una determinata view
    private final void setFocusAndScrollToView(final View view){
        findViewById(R.id.listScrollView).post(() -> {
            findViewById(R.id.listScrollView).scrollTo(0, view.getTop());
            view.requestFocus();
        });
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
            // TODO Riabilitare i controlli sulle date, sia per quelle con input Spinner che quelle con EditText
            /*
            if(dateField.getTag().toString().equals("purchaseDate") && !DateUtils.isDateEmpty(purchaseDateField) && purchaseDateField.getTag(R.id.warningEdit)==null){ // purchaseDate
                if(!DateUtils.isDateEmpty(expiryDateField) && TextUtils.getDate(purchaseDateField).after(TextUtils.getDate(expiryDateField)))
                    showDateWarning(previousDate, purchaseDateField,"La data di acquisto selezionata è uguale o successiva alla data di scadenza, continuare comunque aggiungendo un prodotto già scaduto?");
            } else if(dateField.getTag().toString().equals("openingDate") && !DateUtils.isDateEmpty(openingDateField) && openingDateField.getTag(R.id.warningEdit)==null){
                if(!DateUtils.isDateEmpty(expiryDateField) && TextUtils.getDate(openingDateField).after(TextUtils.getDate(expiryDateField)))
                    showDateWarning(previousDate, openingDateField,"La data di apertura selezionata è uguale o successiva alla data di scadenza, continuare comunque aggiungendo un prodotto già scaduto?");
            } else if(dateField.getTag().toString().equals("expiryDate") && !DateUtils.isDateEmpty(expiryDateField) && expiryDateField.getTag(R.id.warningEdit)==null){
                if(TextUtils.getDate(expiryDateField).before(Calendar.getInstance().getTime()))
                    showDateWarning(previousDate, expiryDateField,"La data di scadenza selezionata è uguale o precedente alla data odierna, continuare comunque aggiungendo un prodotto già scaduto?");
                else if(!DateUtils.isDateEmpty(openingDateField) && TextUtils.getDate(expiryDateField).before(TextUtils.getDate(openingDateField)))
                    showDateWarning(previousDate, expiryDateField,"La data di scadenza selezionata è uguale o precedente alla data di apertura, continuare comunque aggiungendo un prodotto già scaduto?");
                else if(!DateUtils.isDateEmpty(purchaseDateField) && TextUtils.getDate(expiryDateField).before(TextUtils.getDate(purchaseDateField)))
                    showDateWarning(previousDate, expiryDateField,"La data di scadenza selezionata è uguale o precedente alla data di acquisto, continuare comunque aggiungendo un prodotto già scaduto?");
            }
            */
        }
    }
}
