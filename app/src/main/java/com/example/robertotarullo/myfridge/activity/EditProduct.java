package com.example.robertotarullo.myfridge.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.robertotarullo.myfridge.adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.bean.Filter;
import com.example.robertotarullo.myfridge.bean.Pack;
import com.example.robertotarullo.myfridge.bean.ProductForm;
import com.example.robertotarullo.myfridge.bean.SingleProduct;
import com.example.robertotarullo.myfridge.adapter.PointsOfPurchaseSpinnerAdapter;
import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.database.ProductDatabase;
import com.example.robertotarullo.myfridge.filter.NameBrandInputFilter;
import com.example.robertotarullo.myfridge.fragment.SpinnerDatePickerFragment;
import com.example.robertotarullo.myfridge.watcher.CurrentWeightSliderListener;
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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EditProduct extends AppCompatActivity {

    // Variabili intent
    public static final String ACTION = "action"; // Tipo di azione
    public static final String ACTION_TYPE = "actionType"; // Tipo di form
    public static final String ID = "id"; // Id del prodotto da modificare
    public static final String FILTER = "filter"; // Filtro di partenza
    public static final String SUGGESTIONS = "suggestions"; // Aggiunge eventuali prodotti a quelli già esistenti da cui prendere suggerimenti

    public static final String PRODUCT_TO_EDIT = "productToEdit"; // TODO usato da shopping
    public static final String QUANTITY = "quantity"; // TODO usato da shopping

    private static final int PICK_REQUEST = 1;

    // Tipo di azione
    public enum Action{
        ADD,
        EDIT,
        EDIT_PACK,
    }

    public enum ActionType{
        DEFAULT,
        CONSUMED,
        UPDATE,
        NO_CONSUMPTION,
        SHOPPING,
        MANAGE
    }

    // Variabili statiche
    public static final int FREEZER_MAX_CELSIUS = 0, FRIDGE_MIN_CELSIUS = 3, FRIDGE_MAX_CELSIUS = 6;
    public static final int FRIDGE_DEFAULT_CELSIUS = 5, FREEZER_DEFAULT_CELSIUS = -18, ROOM_DEFAULT_CELSIUS = 20;  // ideale (4-6) per frigo
    public static final int ROOM_SELECTION = 0, FRIDGE_SELECTION = 1, FREEZER_SELECTION = 2;
    public static final int MAX_QUANTITY = 99, MIN_QUANTITY = 1, MAX_PIECES = 99, MIN_PIECES = 1;
    private final boolean DEFAULT_EXPIRY_DATE_MODE = false;

    // Intent
    private Action action;
    private ActionType actionType;
    private ProductForm startingForm;
    private long productToModifyId;
    private Pack packToModify;

    // Variabili per i suggerimenti dei campi
    private Set<String> nameSuggestionsList, brandSuggestionsList;

    // Views
    private ScrollView listScrollView;
    private EditText nameField, brandField, pricePerKiloField, priceField, weightField, purchaseDateField, expiryDateField, openingDateField, expiryDaysAfterOpeningField, currentWeightField, packagingDateField, consumptionDateField, currentPercentageField;
    private Spinner storageConditionSpinner, openedStorageConditionSpinner, pointOfPurchaseSpinner;
    private CheckBox openedCheckBox, packagedCheckBox, noExpiryCheckbox, noExpiryDaysCheckbox, consumedCheckBox;
    private Button confirmButton, priceClearButton, pricePerKiloClearButton, weightClearButton, changeToExpiryDaysButton, changeToExpiryDateButton, addQuantityButton, subtractQuantityButton, addPieceButton, subtractPieceButton;
    private SeekBar currentWeightSlider;
    private TextView storageConditionSpinnerLabel, quantityField, piecesField, currentPiecesField, expiryDaysAfterOpeningLabel;

    // Variabili di controllo del form
    private boolean expiryDateMode;

    // Dichiarazione dei blocchi che hanno regole per la visibilità
    private LinearLayout openingDateBlock, expiryDateBlock, openedCheckBoxBlock, openedStorageConditionBlock, currentWeightBlock, quantityBlock, expiryDaysAfterOpeningBlock, pointOfPurchaseBlock, purchaseDateBlock, consumedCheckboxBlock, consumptionDateBlock, openedBlock;

    // Dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Ottieni un riferimento al db
        productDatabase = ProductDatabase.getInstance(getApplicationContext());

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
        noExpiryDaysCheckbox = findViewById(R.id.noExpiryDaysCheckbox);
        consumedCheckBox = findViewById(R.id.consumedCheckBox);
        consumptionDateField = findViewById(R.id.consumptionDateField);
        currentPercentageField = findViewById(R.id.currentPercentageField);

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

        // Riferimenti ai blocchi che hanno regole per la visibilità
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

        // Riferimenti ai pulsanti clear di campi coinvolti in relazioni
        priceClearButton = findViewById(R.id.priceClearButton);
        pricePerKiloClearButton = findViewById(R.id.pricePerKiloClearButton);
        weightClearButton = findViewById(R.id.weightClearButton);

        action = (Action) getIntent().getSerializableExtra("action");
        actionType = (ActionType) getIntent().getSerializableExtra("actionType");

        // Inizializza gli array per i suggerimenti
        initializeSuggestions();

        // Popola spinners
        initializeStorageSpinners();

        // Variabili di controllo del form
        expiryDateMode = DEFAULT_EXPIRY_DATE_MODE; // TODO configurabile: valore iniziale a preferenza dell'utente

        // Comportamenti delle checkbox
        initializeOpenedCheckBox(true);
        initializePackagedCheckBox(true);
        initializeNoExpiryCheckBox(true);
        noExpiryDaysCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> noExpiryCheckbox.setChecked(noExpiryDaysCheckbox.isChecked()));
        initializeConsumedCheckBox(true);

        // Validazione e comportamento
        currentPercentageField.setText("100");

        // TODO passare context al posto delle view fisse
        priceField.addTextChangedListener(new PriceWeightRelationWatcher(priceField.getTag().toString(), pricePerKiloField, weightField, pricePerKiloClearButton, weightClearButton, this));
        pricePerKiloField.addTextChangedListener(new PriceWeightRelationWatcher(pricePerKiloField.getTag().toString(), priceField, weightField, priceClearButton, weightClearButton, this));
        weightField.addTextChangedListener(new PriceWeightRelationWatcher(weightField.getTag().toString(), priceField, pricePerKiloField, priceClearButton, pricePerKiloClearButton, this));
        currentWeightSlider.setOnSeekBarChangeListener(new CurrentWeightSliderListener(this));
        quantityField.addTextChangedListener(new QuantityWatcher(addQuantityButton, subtractQuantityButton));
        piecesField.addTextChangedListener(new PiecesWatcher(this));

        // InputFilters
        nameField.setFilters(new InputFilter[]{new NameBrandInputFilter()});
        brandField.setFilters(new InputFilter[]{new NameBrandInputFilter()});
        expiryDaysAfterOpeningField.setFilters(new InputFilter[]{new DaysInputFilter()});
        priceField.setFilters(new InputFilter[]{new PriceInputFilter()});
        pricePerKiloField.setFilters(new InputFilter[]{new PriceInputFilter()});
        weightField.setFilters(new InputFilter[]{new WeightInputFilter()});
        currentWeightField.setFilters(new InputFilter[]{new WeightInputFilter()});

        // Comportamento alla perdita del focus
        weightField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onWeightFocusLost(); });
        priceField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onPriceFocusLost(priceField); });
        pricePerKiloField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onPriceFocusLost(pricePerKiloField); });
        //expiryDaysAfterOpeningField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) validateExpiryDate(); });

        switch (action) {
            case ADD:
                nameField.requestFocus(); // Il campo nome ha il focus all'apertura del form

                initializeFormLabels("Aggiungi prodotto", "Aggiungi");

                findViewById(R.id.currentPiecesFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
                currentPiecesField.setVisibility(View.GONE);

                findViewById(R.id.currentWeightFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
                currentWeightField.setVisibility(View.GONE);

                setCurrentFormToInitial();

                switch (actionType){
                    case NO_CONSUMPTION:
                        hideConsumptionFields();
                        break;
                    case SHOPPING:
                        hideNonShoppingFields();
                        break;
                    case DEFAULT:
                        findViewById(R.id.consumedCheckBoxBlock).setVisibility(View.VISIBLE);
                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                        }).start();
                        break;
                }
                break;
            case EDIT_PACK:
                switch (actionType) {
                    case MANAGE: // TODO UNIRE A EDIT MANAGE
                        initializeFormLabels("Modifica prodotto", "Salva");
                        packToModify = (Pack)getIntent().getSerializableExtra("pack");

                        // Nascondi/mostra i campi
                        quantityBlock.setVisibility(View.GONE);

                        hideStatusFields();

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            SingleProduct p = packToModify.getProducts().get(0);
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(p);
                                setCurrentFormToInitial();
                            });
                        }).start();
                        break;
                }
                break;
            case EDIT:
                switch (actionType) {
                    case MANAGE: // TODO UNIRE A EDIT_PACK MANAGE
                        initializeFormLabels("Modifica prodotto", "Salva");
                        productToModifyId = getIntent().getLongExtra("id", 0);

                        // Nascondi/mostra i campi
                        quantityBlock.setVisibility(View.GONE);

                        hideStatusFields();

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            SingleProduct p = productDatabase.productDao().get(productToModifyId);
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(p);
                                setCurrentFormToInitial();
                            });
                        }).start();
                        break;
                    case UPDATE:
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

                                if (!p.isOpened() && p.isPackaged())
                                    TextUtils.setDate(DateUtils.getCurrentDateWithoutTime(), openingDateField);

                                setCurrentFormToInitial();
                            });
                        }).start();
                        break;
                    case SHOPPING: // Modifica di un prodotto nel carrello
                        initializeFormLabels("Modifica prodotto", "Salva");
                        hideNonShoppingFields();
                        quantityField.setText(String.valueOf(getIntent().getIntExtra("quantity", 1)));
                        fillFieldsFromProduct((SingleProduct) getIntent().getSerializableExtra("productToEdit"));
                        setCurrentFormToInitial();
                        break;
                    case CONSUMED:
                        initializeFormLabels("Modifica prodotto", "Salva");
                        productToModifyId = getIntent().getLongExtra("id", 0);

                        // Nascondi/mostra i campi
                        quantityBlock.setVisibility(View.GONE);
                        hideConsumptionFields();
                        consumptionDateBlock.setPadding(0, 0, 0, 0);
                        findViewById(R.id.consumedCheckBoxBlock).setVisibility(View.VISIBLE);
                        findViewById(R.id.consumedCheckBoxLabel).setVisibility(View.GONE); // TODO Unire label e checkbox
                        findViewById(R.id.consumedCheckBox).setVisibility(View.GONE); // TODO Unire label e checkbox

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            SingleProduct p = productDatabase.productDao().get(productToModifyId);
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(p);
                                setCurrentFormToInitial();
                            });
                        }).start();
                        break;
                    default: // modifica completa
                        initializeFormLabels("Modifica prodotto", "Salva");
                        productToModifyId = getIntent().getLongExtra("id", 0);

                        // Nascondi/mostra i campi
                        quantityBlock.setVisibility(View.GONE);

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            SingleProduct p = productDatabase.productDao().get(productToModifyId);
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(p);
                                setCurrentFormToInitial();
                            });
                        }).start();
                        break;
                }
                break;
        }
    }

    private void hideStatusFields(){
        hideConsumptionFields();
        expiryDateBlock.setVisibility(View.GONE);
        findViewById(R.id.packagingDateBlock).setVisibility(View.GONE);
        purchaseDateBlock.setVisibility(View.GONE);
        pointOfPurchaseBlock.setVisibility(View.GONE);
        expiryDaysAfterOpeningBlock.setPadding(0, 0, 0, 0);
        changeToExpiryDateButton.setVisibility(View.GONE);
        changeToExpiryDaysButton.setVisibility(View.GONE);
    }

    // Nascondi i campi precompilati / da ignorare in modalità spesa
    private void hideNonShoppingFields(){
        openedCheckBox.setVisibility(View.GONE);
        pointOfPurchaseBlock.setVisibility(View.GONE);
        purchaseDateBlock.setVisibility(View.GONE);
        hideConsumptionFields();
    }

    private void hideConsumptionFields(){
        openedBlock.setVisibility(View.GONE);
        currentWeightBlock.setVisibility(View.GONE);
        consumedCheckboxBlock.setVisibility(View.GONE);
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

        if(action==Action.EDIT && actionType==ActionType.DEFAULT)
            menu.add(0, R.id.consume, Menu.NONE, "Consuma");

        if(action==Action.EDIT && actionType==ActionType.CONSUMED)
            menu.add(0, R.id.restore, Menu.NONE, "Ripristina");
        else if(actionType!=ActionType.MANAGE)
            menu.add(0, R.id.reset, Menu.NONE, "Resetta...");

        if(action==Action.EDIT && (actionType==ActionType.DEFAULT || actionType==ActionType.UPDATE))
            menu.add(0, R.id.delete, Menu.NONE, "Elimina");

        if(action==Action.ADD)
            menu.add(0, R.id.fillFromInsertedProduct, Menu.NONE, "Compila da prodotto...");

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.consume:
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            new Thread(() -> {
                                SingleProduct p = createProductFromFields();
                                p.setId(productToModifyId);
                                p.setConsumed(true);
                                p.setConsumptionDate(DateUtils.getCurrentDateWithoutTime());

                                if (productDatabase.productDao().update(p) > 0) {
                                    String msg = "Prodotti modificati: 1\nProdotti non modificati: 0";
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                                }

                                Intent resultIntent = new Intent();
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }).start();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                String msg = "Vuoi settare come consumato il prodotto " +nameField.getText().toString()+ "?";

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(msg)
                        .setTitle("Conferma consumazione")
                        .setPositiveButton("Conferma", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();

                return true;
            case R.id.restore:
                dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            new Thread(() -> {
                                SingleProduct p = createProductFromFields();
                                p.setId(productToModifyId);
                                p.setConsumed(false);
                                p.setConsumptionDate(null);

                                if (productDatabase.productDao().update(p) > 0) {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Prodotti modificati: 1\nProdotti non modificati: 0", Toast.LENGTH_LONG).show()); // STRINGS.XML
                                }

                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("filter", p.getActualStorageCondition());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }).start();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                msg = "Vuoi settare come non consumato il prodotto " +nameField.getText().toString()+ "?";

                builder = new AlertDialog.Builder(this);
                builder.setMessage(msg)
                        .setTitle("Conferma ripristino consumazione")
                        .setPositiveButton("Conferma", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();

                return true;
            case R.id.delete:
                dialogClickListener = (dialog, which) -> {
                    switch (which) {
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

                msg = "Vuoi eliminare definitivamente il prodotto?";

                builder = new AlertDialog.Builder(this);
                builder.setMessage(msg)
                        .setTitle("Conferma eliminazione")
                        .setPositiveButton("Elimina", dialogClickListener)
                        .setNegativeButton("Annulla", dialogClickListener)
                        .show();

                return true;
            case R.id.reset:
                if (actionType == ActionType.UPDATE) {
                    dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                resetConsumptionState();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    };

                    msg = "Vuoi ripristinare lo stato di consumazione del prodotto? Verrano resettati tutti i campi relativi all'apertura e alla consumazione.";

                    builder = new AlertDialog.Builder(this);
                    builder.setMessage(msg)
                            .setTitle("Conferma ripristino consumazione")
                            .setPositiveButton("Conferma", dialogClickListener)
                            .setNegativeButton("Annulla", dialogClickListener)
                            .show();
                    return true;
                } else if(actionType == ActionType.SHOPPING){
                    dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                resetState();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    };

                    msg = "Vuoi ripristinare lo stato del prodotto? Verrano resettati tutti i campi relativi allo stato (data di produzione, data di scadenza)";

                    builder = new AlertDialog.Builder(this);
                    builder.setMessage(msg)
                            .setTitle("Conferma ripristino stato")
                            .setPositiveButton("Conferma", dialogClickListener)
                            .setNegativeButton("Annulla", dialogClickListener)
                            .show();
                    return true;
                } else {
                    View resetDialogView = getLayoutInflater().inflate(R.layout.lose_state_dialog, null);

                    RadioButton radioButtonPartial = resetDialogView.findViewById(R.id.radio_reset_partial);
                    RadioButton radioButtonMin = resetDialogView.findViewById(R.id.radio_reset_min);

                    dialogClickListener = (dialog, which) -> {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                if(radioButtonPartial.isChecked())
                                    resetState();
                                else if(radioButtonMin.isChecked())
                                    resetConsumptionState();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    };

                    builder = new AlertDialog.Builder(this);
                    builder.setView(resetDialogView)
                           .setTitle("Conferma ripristino stato")
                           .setPositiveButton("Conferma", dialogClickListener)
                           .setNegativeButton("Annulla", dialogClickListener)
                           .show();
                    return true;
                }
            case R.id.fillFromInsertedProduct:
                // permetti all'utente di scegliere un prodotto già inserito
                Intent intent = new Intent(this, ProductsList.class);
                intent.putExtra("action", ProductsList.Action.PICK);
                startActivityForResult(intent, PICK_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetState(){
        resetConsumptionState();

        packagingDateField.setText(""); // Resetta data di confezionamento
        purchaseDateField.setText(""); // Resetta data di acquisto
        expiryDateField.setText("");
        pointOfPurchaseSpinner.setSelection(0); // Resetta punto di acquisto // TODO resetta solo se si tratta di un prodotto che si può comprare anche altrove ?
    }

    private void resetConsumptionState(){
        // Resetta consumazione
        consumptionDateField.setText("");
        consumedCheckBox.setChecked(false);

        // Resetta apertura
        openingDateField.setText("");
        openedCheckBox.setChecked(false);

        // Resetta slider
        currentPercentageField.setText("100");
        currentWeightSlider.setProgress(currentWeightSlider.getMax());
    }

    // Mostra avviso nel caso di campi che modificano il prodotto
    @Override
    public void onBackPressed() {
        if(startingForm.equals(getCurrentForm()))
            super.onBackPressed();
        else {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        super.onBackPressed();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };
            new AlertDialog.Builder(this)
                .setMessage("Sono stati modificati alcuni campi, sei sicuro di voler uscire senza salvare?")
                .setTitle("Attenzione")
                .setPositiveButton("Esci", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
        }
    }

    private ProductForm getCurrentForm(){
        return new ProductForm(createProductFromFields(), TextUtils.getInt(quantityField), TextUtils.getDate(expiryDateField), TextUtils.getInt(expiryDaysAfterOpeningField));
    }

    private void initializeSuggestions(){
        nameSuggestionsList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        brandSuggestionsList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        new Thread(() -> {
            List<SingleProduct> products = productDatabase.productDao().getAll(); // TODO Prendi tutti i prodotti non uguali
            if(getIntent().getSerializableExtra(SUGGESTIONS)!=null){ // Se si tratta di un'aggiunta o di una modifica
                List<SingleProduct> cartProducts = (List<SingleProduct>) getIntent().getSerializableExtra(SUGGESTIONS);
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

        // TODO IN MODALITÀ MANAGE RIEMPIRE SOLO CON I CAMPI CHE SERVONO

        // TODO Mettere a fattor comune con codice in case("add"), PiecesWatcher e PriceWeightRelationWatcher
        if(p.getPieces()==1 && p.getWeight()==0) {
            currentPercentageField.setVisibility(View.VISIBLE);
            findViewById(R.id.currentPercentageFieldLabel).setVisibility(View.VISIBLE);
        } else {
            currentPercentageField.setVisibility(View.GONE);
            findViewById(R.id.currentPercentageFieldLabel).setVisibility(View.GONE);
        }

        if(p.getWeight()==0){
            findViewById(R.id.currentWeightFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
            currentWeightField.setVisibility(View.GONE);
        } else {
            findViewById(R.id.currentWeightFieldLabel).setVisibility(View.VISIBLE); // TODO controllare l'intero blocco contentente label + field
            currentWeightField.setVisibility(View.VISIBLE);
        }

        if(p.getPieces()==1){
            findViewById(R.id.currentPiecesFieldLabel).setVisibility(View.GONE); // TODO controllare l'intero blocco contentente label + field
            currentPiecesField.setVisibility(View.GONE);
        } else {
            findViewById(R.id.currentPiecesFieldLabel).setVisibility(View.VISIBLE); // TODO controllare l'intero blocco contentente label + field
            currentPiecesField.setVisibility(View.VISIBLE);
            currentPiecesField.setVisibility(View.VISIBLE);
        }

        if(p.isConsumed())
            consumedCheckBox.setChecked(true);
        else
            consumedCheckBox.setChecked(false);

        TextUtils.editFieldNotFromUser(consumptionDateField, DateUtils.getFormattedDate(p.getConsumptionDate()));

        TextUtils.setText(p.getName(), nameField);

        TextUtils.setText(p.getBrand(), brandField);

        if(p.getPricePerKilo()==0 || p.getWeight()==0)
            TextUtils.setPrice(p.getPrice(), priceField);
        if(p.getPrice()==0 || p.getWeight()==0)
            TextUtils.setPrice(p.getPricePerKilo(), pricePerKiloField);
        if(p.getPrice()==0 || p.getPricePerKilo()==0)
            TextUtils.setWeight(p.getWeight(), weightField);

        if((p.getWeight()>0 || (p.getPrice()>0 && p.getPricePerKilo()>0)) && p.getCurrentWeight()>0) // Se il peso è definito/generato e currentWeight definito
            TextUtils.setWeight(p.getCurrentWeight(), currentWeightField);
        else
            TextUtils.setWeight(p.getWeight(), currentWeightField);

        currentPercentageField.setText(String.valueOf(p.getPercentageQuantity()));

        if(p.getExpiringDaysAfterOpening()>0)
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, String.valueOf(p.getExpiringDaysAfterOpening()));
        else if(p.getExpiringDaysAfterOpening()==-1)
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, "0");
        else
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, "");

        TextUtils.setDate(p.getPurchaseDate(), purchaseDateField);

        storageConditionSpinner.setSelection(p.getStorageCondition());
        openedStorageConditionSpinner.setSelection(p.getOpenedStorageCondition());

        TextUtils.setDate(p.getPackagingDate(), packagingDateField);

        TextUtils.setPointOfPurchase(p.getPointOfPurchaseId(), pointOfPurchaseSpinner);

        piecesField.setText(String.valueOf(p.getPieces()));

        currentPiecesField.setText(String.valueOf(p.getCurrentPieces()));

        if(p.getExpiryDate()!=null) {
            if(p.getExpiryDate().equals(DateUtils.getNoExpiryDate())) {                                         // se data di scadenza 'mai'
                noExpiryCheckbox.setChecked(true);
                TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, "");                           // svuota eventuale expiryDays
                TextUtils.editFieldNotFromUser(expiryDateField, "");                                       // svuota eventuale expiryDate
            } else {
                TextUtils.editFieldNotFromUser(expiryDateField, DateUtils.getFormattedDate(p.getExpiryDate())); // se data di scadenza standard
                if(!p.isPackaged() && actionType!=ActionType.MANAGE)                                            // Se prodotto fresco...
                    changeToExpiringDateMode(true);                                                             // .. mostra 'data di scadenza' e nascondi 'giorni entro cui consumare'
            }
        } else {
            TextUtils.editFieldNotFromUser(expiryDateField, "");
            noExpiryCheckbox.setChecked(false);
            changeToExpiringDateMode(DEFAULT_EXPIRY_DATE_MODE); // TODO CONFIGURABILE DA utente nelle impostazioni
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
        } else {
            TextUtils.editFieldNotFromUser(openingDateField, "");
            openedCheckBox.setChecked(false);
            packagedCheckBox.setChecked(false);
        }

        if (p.getPieces()==1 && (p.getWeight()==0 && (p.getPrice()==0 || p.getPricePerKilo()==0))){ // Se il pezzo è unico e il peso non è definito/generato
            currentWeightSlider.setTag("percentage");
            currentWeightSlider.setMax(SingleProduct.DEFAULT_PERCENTAGEQUANTITY);
            currentWeightSlider.setProgress((int) Math.ceil(p.getPercentageQuantity()));
        } else if (p.getPieces()>1) {
            currentWeightSlider.setTag("pieces");
            currentWeightSlider.setMax(p.getPieces());
            currentWeightSlider.setProgress(p.getCurrentPieces());
        } else if (p.getWeight()>0 || (p.getPrice()>0 && p.getPricePerKilo()>0)) { // Se il peso è definito/generato
            currentWeightSlider.setTag("currentWeight");
            currentWeightSlider.setMax(TextUtils.getInt(weightField));
            currentWeightSlider.setProgress(TextUtils.getInt(currentWeightField));
        }
    }

    private void insertProduct(SingleProduct newProduct){
        new Thread(() -> {
            int insertCount = 0; // counter inserimenti
            Intent resultIntent = new Intent();

            switch (action) {
                case EDIT:  // Se si tratta di una modifica
                    if(actionType == ActionType.UPDATE) { // Se si tratta di un aggiornamento
                        newProduct.setId(productToModifyId);
                        if (productDatabase.productDao().update(newProduct) > 0) {
                            insertCount = 1;
                            String msg = "Prodotti aggiornati: " + insertCount + "\nProdotti non aggiornati: " + (1 - insertCount); // TODO adattare a update
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                        }
                        break;
                    } else if(actionType == ActionType.SHOPPING){ // Se si tratta della modalità spesa
                        if (getIntent().getSerializableExtra("productToEdit") != null) {
                            resultIntent.putExtra("quantity", TextUtils.getInt(quantityField));
                            resultIntent.putExtra("editedProduct", newProduct);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    } else { // modifica standard
                        newProduct.setId(productToModifyId);
                        if (productDatabase.productDao().update(newProduct) > 0) {
                            insertCount = 1;
                            String msg = "Prodotti modificati: " + insertCount + "\nProdotti non modificati: " + (1 - insertCount); // TODO adattare a edit
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()); // STRINGS.XML
                        }
                        break;
                    }
                case ADD:  // Se si tratta di un'aggiunta
                    if(actionType==ActionType.SHOPPING) {
                        resultIntent.putExtra("newProduct", newProduct);
                        resultIntent.putExtra("quantity", TextUtils.getInt(quantityField));
                        setResult(RESULT_OK, resultIntent);
                        finish();
                        break;
                    } else {
                        List<SingleProduct> productsToAdd = new ArrayList<>();
                        for (int i = 0; i < TextUtils.getInt(quantityField); i++)
                            productsToAdd.add(newProduct);
                        productDatabase.productDao().insertAll(productsToAdd); // TODO gestire valore di ritorno
                        resultIntent.putExtra("filter", newProduct.getActualStorageCondition());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                        break;
                    }
            }

            if(insertCount>0){
                resultIntent.putExtra("filter", newProduct.getActualStorageCondition());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }).start();
    }

    // Metodo chiamato alla pressione del tasto di conferma, che può essere l'aggiunta o la modifica del prodotto
    public void onConfirmButtonClick(View view) {

        // Il campo nome è obbligatorio
        if (TextUtils.isEmpty(nameField)) {
            Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
            setFocusAndScrollToView(findViewById(R.id.nameBlock));
        } else if(actionType==ActionType.MANAGE){

            SingleProduct generalProduct = createGeneralProductFromFields(); // Prodotto compilato con solo i campi strettamente relativi al prodotto

            if(action==Action.EDIT){
                new Thread(() -> {
                    SingleProduct p = productDatabase.productDao().get(productToModifyId);
                    p.setPackaged(generalProduct.isPackaged());
                    p.setName(generalProduct.getName());
                    p.setBrand(generalProduct.getBrand());
                    p.setPrice(generalProduct.getPrice());
                    p.setWeight(generalProduct.getWeight());
                    p.setPricePerKilo(generalProduct.getPricePerKilo());
                    p.setPieces(generalProduct.getPieces());
                    p.setStorageCondition(generalProduct.getStorageCondition());
                    p.setOpenedStorageCondition(generalProduct.getOpenedStorageCondition());
                    p.setExpiringDaysAfterOpening(generalProduct.getExpiringDaysAfterOpening());

                    if (productDatabase.productDao().update(p) > 0) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Prodotto modificato", Toast.LENGTH_LONG).show()); // STRINGS.XML
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }).start();
            } else if(action==Action.EDIT_PACK){
                for(int i=0; i<packToModify.getProducts().size(); i++){
                    packToModify.getProducts().get(i).setPackaged(generalProduct.isPackaged());
                    packToModify.getProducts().get(i).setName(generalProduct.getName());
                    packToModify.getProducts().get(i).setBrand(generalProduct.getBrand());
                    packToModify.getProducts().get(i).setPrice(generalProduct.getPrice());
                    packToModify.getProducts().get(i).setWeight(generalProduct.getWeight());
                    packToModify.getProducts().get(i).setPricePerKilo(generalProduct.getPricePerKilo());
                    packToModify.getProducts().get(i).setPieces(generalProduct.getPieces());
                    packToModify.getProducts().get(i).setStorageCondition(generalProduct.getStorageCondition());
                    packToModify.getProducts().get(i).setOpenedStorageCondition(generalProduct.getOpenedStorageCondition());
                    packToModify.getProducts().get(i).setExpiringDaysAfterOpening(generalProduct.getExpiringDaysAfterOpening());
                }

                new Thread(() -> {
                    if (productDatabase.productDao().updateAll(packToModify.getProducts()) > 0) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Prodotto modificato", Toast.LENGTH_LONG).show()); // STRINGS.XML
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }).start();
            }
        } else {
            // Mostra warning in caso di date sospette
            SingleProduct formProduct = createProductFromFields();

            List<String> dateWarnings = DateUtils.getDateWarnings(formProduct, action, actionType);
            if(dateWarnings.size()>0){

                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            insertProduct(formProduct);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                StringBuilder msg = new StringBuilder("Sono state rilevate una o più incoerenze sui valori inseriti nei campi:\n\n");
                for(int i=0; i<dateWarnings.size(); i++){
                    msg.append("- ");
                    msg.append(dateWarnings.get(i));
                    msg.append(";\n");
                }
                msg.append("\nSei sicuro di voler continuare comunque?"); // TODO Specializzare in base al tipo di modifica

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(msg.toString())
                        .setTitle("Incoerenze rilevate") // TODO Specializzare in base al tipo di modifica
                        .setPositiveButton("Continua", dialogClickListener) // TODO Specializzare in base al tipo di modifica
                        .setNegativeButton("Annulla", dialogClickListener) // TODO Specializzare in base al tipo di modifica
                        .show();
            } else
                insertProduct(formProduct);
        }
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

        new AlertDialog.Builder(this)
                .setMessage("Vuoi segnare come consumato il prodotto \"" + newProduct.getName() + "\"?")
                .setTitle("Conferma consumazione")
                .setPositiveButton("Conferma", dialogClickListener)
                .setNegativeButton("Annulla", dialogClickListener)
                .show();
    }

    private void loseFieldsFocus(){
        // Esegui tutte le funzioni della perdita del focus per avere il valore corretto effettivo
        onWeightFocusLost();
        onPriceFocusLost(priceField);
        onPriceFocusLost(pricePerKiloField);
    }

    // TODO mettere a fattor comune con createProductFromFields()
    private SingleProduct createGeneralProductFromFields() {
        loseFieldsFocus();

        SingleProduct p = new SingleProduct();
        p.setName(TextUtils.getNameWithoutExtraSpaces(nameField));

        p.setBrand(TextUtils.getNameWithoutExtraSpaces(brandField));

        if (priceField.isEnabled())
            p.setPrice(TextUtils.getFloat(priceField));

        if (weightField.isEnabled())
            p.setWeight(TextUtils.getFloat(weightField));

        if (pricePerKiloField.isEnabled())
            p.setPricePerKilo(TextUtils.getFloat(pricePerKiloField));

        p.setPieces(TextUtils.getInt(piecesField));

        p.setStorageCondition(storageConditionSpinner.getSelectedItemPosition());

        if(expiryDaysAfterOpeningBlock.getVisibility()==View.VISIBLE && expiryDaysAfterOpeningBlock.isEnabled() && !noExpiryCheckbox.isChecked())
            p.setExpiringDaysAfterOpening(TextUtils.getInt(expiryDaysAfterOpeningField));

        if(packagedCheckBox.isChecked()){
            p.setPackaged(true);
            p.setOpenedStorageCondition(openedStorageConditionSpinner.getSelectedItemPosition());
        }

        return p;
    }

    // Costruisce l'oggetto prodotto dai valori presenti nei campi
    private SingleProduct createProductFromFields() {
        loseFieldsFocus();

        SingleProduct p = createGeneralProductFromFields();

        p.setConsumed(consumedCheckBox.isChecked());
        if (consumedCheckBox.isChecked())
            p.setConsumptionDate(TextUtils.getDate(consumptionDateField));

        p.setPurchaseDate(TextUtils.getDate(purchaseDateField));

        if(pointOfPurchaseSpinner.getSelectedItemPosition()>0)
            p.setPointOfPurchaseId(((PointOfPurchase)pointOfPurchaseSpinner.getSelectedItem()).getId());

        // campi che dipendono dal tipo e dall'apertura del prodotto confezionato
        if(packagedCheckBox.isChecked()){
            if(openedCheckBox.isChecked()) {
                p.setOpened(true);
                p.setOpeningDate(TextUtils.getDate(openingDateField));
            }
        } else {
            p.setPackagingDate(TextUtils.getDate(packagingDateField));
        }

        if(noExpiryCheckbox.isChecked())
            p.setExpiryDate(DateUtils.getNoExpiryDate());
        else if(expiryDateBlock.getVisibility()==View.VISIBLE)
            p.setExpiryDate(TextUtils.getDate(expiryDateField));

        // Si salva la consumazione se si tratta di un prodotto confezionato aperto OPPURE di un prodotto fresco
        if(!packagedCheckBox.isChecked() || openedCheckBox.isChecked()){
            p.setPercentageQuantity(TextUtils.getFloat(currentPercentageField));
            p.setCurrentPieces(TextUtils.getInt(currentPiecesField));
            p.setCurrentWeight(TextUtils.getFloat(currentWeightField));
        }

        return p;
    }

    private void initializeOpenedCheckBox(boolean addListener) {
        if(openedCheckBox.isChecked()) {
            openingDateBlock.setVisibility(View.VISIBLE);
            //currentWeightBlock.setVisibility(View.VISIBLE);

        } else {
            openingDateBlock.setVisibility(View.GONE);
            //currentWeightBlock.setVisibility(View.GONE);
            currentWeightSlider.setProgress(currentWeightSlider.getMax());
            currentPercentageField.setText("100");
        }

        if(addListener)
            openedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeOpenedCheckBox(false));
    }

    private void initializePackagedCheckBox(boolean addListener) {
        enableNoExpiryCheckBoxBehaviour(noExpiryCheckbox.isChecked());

        View packagingDateBlock = findViewById(R.id.packagingDateBlock);

        if(packagedCheckBox.isChecked()){
            if(actionType!=ActionType.MANAGE) {
                packagingDateBlock.setVisibility(View.GONE);
                noExpiryDaysCheckbox.setVisibility(View.GONE);
            }
            changeToExpiryDateButton.setVisibility(View.GONE);
            changeToExpiryDaysButton.setVisibility(View.GONE);
            storageConditionSpinnerLabel.setText("Modalità di conservazione prima dell'apertura");
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare dopo l'apertura");

            if(actionType!=ActionType.SHOPPING && actionType!=ActionType.CONSUMED && actionType!=ActionType.MANAGE) {
                openedBlock.setVisibility(View.VISIBLE);
                if(openedCheckBox.isChecked())
                    openingDateBlock.setVisibility(View.VISIBLE);
            }

            if(actionType != ActionType.UPDATE){
                if(actionType != ActionType.MANAGE)
                    expiryDateBlock.setVisibility(View.VISIBLE);
                expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
                openedStorageConditionBlock.setVisibility(View.VISIBLE);
            }

            if(currentWeightSlider.getProgress()<currentWeightSlider.getMax())
                openedCheckBox.setChecked(true);
            else
                openedCheckBox.setChecked(false);
        } else {
            if(actionType!=ActionType.MANAGE)
                packagingDateBlock.setVisibility(View.VISIBLE);
            noExpiryDaysCheckbox.setVisibility(View.VISIBLE);
            expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
            if(actionType != ActionType.MANAGE){
                changeToExpiryDateButton.setVisibility(View.VISIBLE);
                changeToExpiryDaysButton.setVisibility(View.VISIBLE);
            }
            storageConditionSpinnerLabel.setText("Modalità di conservazione");
            expiryDaysAfterOpeningLabel.setText("Giorni entro cui consumare");
            expiryDateBlock.setVisibility(View.GONE);

            if(actionType != ActionType.SHOPPING && actionType!=ActionType.CONSUMED && actionType!=ActionType.MANAGE){
                openedBlock.setVisibility(View.GONE);
                openingDateBlock.setVisibility(View.GONE);
            }

            openedStorageConditionBlock.setVisibility(View.GONE);

            if(currentWeightSlider.getProgress() < currentWeightSlider.getMax() && !openedCheckBox.isChecked()) {
                currentWeightSlider.setProgress(currentWeightSlider.getMax());
                currentPercentageField.setText("100");
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
        noExpiryDaysCheckbox.setChecked(noExpiryCheckbox.isChecked());

        if(addListener)
            noExpiryCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeNoExpiryCheckBox(false));
    }

    private void initializeConsumedCheckBox(boolean addListener){
        if(consumedCheckBox.isChecked()) {
            consumptionDateBlock.setVisibility(View.VISIBLE);
        } else {
            consumptionDateBlock.setVisibility(View.GONE);
        }

        if(addListener)
            consumedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeConsumedCheckBox(false));
    }

    private void enableNoExpiryCheckBoxBehaviour(boolean enable){
        expiryDateField.setEnabled(!enable);
        findViewById(R.id.expiryDateClearButton).setEnabled(!enable);
        findViewById(R.id.expiryDateFieldLabel).setEnabled(!enable);
        expiryDaysAfterOpeningField.setEnabled(!enable);
        findViewById(R.id.expiryDaysAfterOpeningClearButton).setEnabled(!enable);
        findViewById(R.id.expiryDaysAfterOpeningFieldLabel).setEnabled(!enable);
        changeToExpiryDateButton.setEnabled(!enable);
        changeToExpiryDaysButton.setEnabled(!enable);

        if(enable) {
            expiryDateField.setHint("Mai");
            expiryDaysAfterOpeningField.setHint(" - ");
            expiryDateField.setHintTextColor(Color.parseColor("#d8d8d8"));
            expiryDaysAfterOpeningField.setHintTextColor(Color.parseColor("#d8d8d8"));
        } else {
            expiryDateField.setHint("Data non impostata");
            expiryDaysAfterOpeningField.setHint("0");
            expiryDateField.setHintTextColor(Color.parseColor("#a7a7a7"));
            expiryDaysAfterOpeningField.setHintTextColor(Color.parseColor("#a7a7a7"));
        }
    }

    // TODO controllare thread
    private void initializeStorageSpinners() {
        new Thread(() -> {
            List<Filter> filters = productDatabase.filterDao().getFilters();

            runOnUiThread(() -> {
                storageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, filters));
                openedStorageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, filters));

                if (actionType==ActionType.SHOPPING)
                    storageConditionSpinner.setSelection(FRIDGE_SELECTION); // TODO permettere di selezionare il valore di default
                else
                    storageConditionSpinner.setSelection(getIntent().getIntExtra("filter", FRIDGE_SELECTION));
                openedStorageConditionSpinner.setSelection(storageConditionSpinner.getSelectedItemPosition());
            });
        }).start();
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

    // immissione data
    public void showDatePickerDialog(View v) {
        DialogFragment f;
        Bundle args = new Bundle();

        if(v==packagingDateField || v==expiryDateField){
            f = new SpinnerDatePickerFragment();
            //f = new DatePickerFragment();
            //args.putBoolean("spinnerMode", true);
            args.putInt("dateFieldId", v.getId());
            f.setArguments(args);
            f.show(getSupportFragmentManager(), "spinnerDatePicker");
        } else {
            f = new DatePickerFragment();
            args.putInt("dateFieldId", v.getId());
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
        //validateExpiryDate();
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
    /*private void validateExpiryDate(){
        String temp = expiryDateField.getText().toString();
        expiryDateField.setTag(R.id.expirySwitchControl, DateUtils.EXPIRY_SWITCH_CONTROL_TAG);
        expiryDateField.setText(temp); // TODO controllare date illegali calcolate con expiryDays
    }*/

    // TODO continuare implementazione
    private void resetFormFields(){
        packagedCheckBox.setChecked(false);
        nameField.setText("");
        brandField.setText("");
        priceField.setText("");
        pricePerKiloField.setText("");
        weightField.setText("");
        piecesField.setText("1");
        expiryDaysAfterOpeningField.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_REQUEST) {
            if (resultCode == RESULT_OK) {
                long productId = data.getLongExtra("productId", 0);
                new Thread(() -> {
                    SingleProduct p = productDatabase.productDao().get(productId);
                    p.loseState(); // perdi lo stato
                    runOnUiThread(() -> {
                        // resetta tutti i campi che possono essere compilati (attributi non relativi allo stato)
                        // TODO necessario se si resetta anche dopo ?
                        // gli spinner non si toccano perchè prenderanno il valore di p

                        resetFormFields();
                        fillFieldsFromProduct(p);
                    });
                }).start();
            }
        }
    }
}
