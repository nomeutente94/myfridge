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
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class EditProduct extends AppCompatActivity {

    // Variabili intent ricevute dal chiamante
    public static final String ACTION = "action"; // Tipo di azione
    public static final String ACTION_TYPE = "actionType"; // Tipo di form
    public static final String FILTER = "filter"; // Filtro di partenza
    public static final String SUGGESTIONS = "suggestions"; // Aggiunge eventuali prodotti a quelli già esistenti da cui prendere suggerimenti
    public static final String PRODUCT_TO_EDIT = "productToEdit"; // Eventuale prodotto da modificare
    public static final String QUANTITY = "quantity"; // Eventuale quantità del prodotto da modificare

    private static final int PICK_REQUEST = 1;

    // Tipo di azione
    public enum Action {
        ADD,
        EDIT
    }

    public enum ActionType {
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
    // public static final int ROOM_SELECTION = 0, FRIDGE_SELECTION = 1, FREEZER_SELECTION = 2;
    public static final int MAX_QUANTITY = 99, MAX_PIECES = 99; // TODO conservare qui le variabili relative ai vincoli del form?
    private final boolean DEFAULT_EXPIRY_DATE_MODE = false;

    // Intent
    private Action action;
    private ActionType actionType;
    private ProductForm startingForm;
    private SingleProduct productToModify;

    // Views
    private ScrollView listScrollView;
    private AutoCompleteTextView nameField, brandField;
    private EditText pricePerKiloField, priceField, weightField, purchaseDateField, expiryDateField, openingDateField, expiryDaysAfterOpeningField, currentWeightField, packagingDateField, consumptionDateField, currentPercentageField;
    private Spinner storageConditionSpinner, openedStorageConditionSpinner, pointOfPurchaseSpinner;
    private CheckBox openedCheckBox, packagedCheckBox, noExpiryCheckbox, noExpiryDaysCheckbox, consumedCheckBox;
    private Button expiryDaysAfterOpeningClearButton, expiryDateClearButton, confirmButton, priceClearButton, pricePerKiloClearButton, weightClearButton, changeToExpiryDaysButton, changeToExpiryDateButton, addQuantityButton, subtractQuantityButton, addPieceButton, subtractPieceButton, consumeProductButton;
    private SeekBar currentWeightSlider;
    private TextView storageConditionSpinnerLabel, quantityField, piecesField, currentPiecesField, expiryDaysAfterOpeningLabel, expiryDateFieldLabel;

    // Variabili di controllo del form
    private boolean expiryDateMode;

    // Dichiarazione dei blocchi che hanno regole per la visibilità
    private LinearLayout currentPercentageBlock, openingDateBlock, expiryDateBlock, openedStorageConditionBlock, currentConsumptionBlock, quantityBlock, expiryDaysAfterOpeningBlock, pointOfPurchaseBlock, purchaseDateBlock, consumedBlock, consumptionDateBlock, openedBlock, currentPiecesBlock, currentWeightBlock, nameFieldsBlock, piecesBlock, packagedBlock, datesBlock, priceWeightBlock, storageConditionsBlock;

    // Dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Ottieni un riferimento al db
        productDatabase = ProductDatabase.getInstance(getApplicationContext());

        // Inizializzazione dei riferimenti alle view

        packagedCheckBox = findViewById(R.id.packagedCheckBox);
        openedCheckBox = findViewById(R.id.openedCheckBox);
        noExpiryCheckbox = findViewById(R.id.noExpiryCheckbox);
        noExpiryDaysCheckbox = findViewById(R.id.noExpiryDaysCheckbox);
        consumedCheckBox = findViewById(R.id.consumedCheckBox);

        nameField = findViewById(R.id.nameField);
        brandField = findViewById(R.id.brandField);
        pricePerKiloField = findViewById(R.id.pricePerKiloField);
        currentWeightField = findViewById(R.id.currentWeightField);
        priceField = findViewById(R.id.priceField);
        weightField = findViewById(R.id.weightField);
        purchaseDateField = findViewById(R.id.purchaseDateField);
        openingDateField = findViewById(R.id.openingDateField);
        expiryDateField = findViewById(R.id.expiryDateField);
        packagingDateField = findViewById(R.id.packagingDateField);
        expiryDaysAfterOpeningField = findViewById(R.id.expiryDaysAfterOpeningField);
        quantityField = findViewById(R.id.quantityField);
        piecesField = findViewById(R.id.piecesField);
        currentPiecesField = findViewById(R.id.currentPiecesField);
        consumptionDateField = findViewById(R.id.consumptionDateField);
        currentPercentageField = findViewById(R.id.currentPercentageField);

        storageConditionSpinner = findViewById(R.id.storageConditionSpinner);
        openedStorageConditionSpinner = findViewById(R.id.openedStorageConditionSpinner);
        pointOfPurchaseSpinner = findViewById(R.id.pointOfPurchaseSpinner);

        currentWeightSlider = findViewById(R.id.currentWeightSlider);

        changeToExpiryDateButton = findViewById(R.id.changeToExpiryDate);
        changeToExpiryDaysButton = findViewById(R.id.changeToExpiryDays);
        addQuantityButton = findViewById(R.id.quantityAddButton);
        subtractQuantityButton = findViewById(R.id.quantitySubtractButton);
        addPieceButton = findViewById(R.id.piecesAddButton);
        subtractPieceButton = findViewById(R.id.piecesSubtractButton);
        consumeProductButton = findViewById(R.id.consumeProductButton);
        confirmButton = findViewById(R.id.addButton);

        priceClearButton = findViewById(R.id.priceClearButton);
        pricePerKiloClearButton = findViewById(R.id.pricePerKiloClearButton);
        weightClearButton = findViewById(R.id.weightClearButton);
        expiryDateClearButton = findViewById(R.id.expiryDateClearButton);
        expiryDaysAfterOpeningClearButton = findViewById(R.id.expiryDaysAfterOpeningClearButton);

        listScrollView = findViewById(R.id.listScrollView);

        storageConditionSpinnerLabel = findViewById(R.id.storageConditionSpinnerLabel);
        expiryDaysAfterOpeningLabel = findViewById(R.id.expiryDaysAfterOpeningFieldLabel);
        expiryDateFieldLabel = findViewById(R.id.expiryDateFieldLabel);

        openingDateBlock = findViewById(R.id.openingDateBlock);
        expiryDateBlock = findViewById(R.id.expiryDateBlock);
        openedStorageConditionBlock = findViewById(R.id.openedStorageConditionBlock);
        currentConsumptionBlock = findViewById(R.id.currentConsumptionBlock);
        quantityBlock = findViewById(R.id.quantityBlock);
        expiryDaysAfterOpeningBlock = findViewById(R.id.expiryDaysAfterOpeningBlock);
        pointOfPurchaseBlock = findViewById(R.id.pointOfPurchaseBlock);
        purchaseDateBlock = findViewById(R.id.purchaseDateBlock);
        consumedBlock = findViewById(R.id.consumedBlock);
        consumptionDateBlock = findViewById(R.id.consumptionDateBlock);
        openedBlock = findViewById(R.id.openedBlock);
        currentPiecesBlock = findViewById(R.id.currentPiecesBlock);
        currentWeightBlock = findViewById(R.id.currentWeightBlock);
        nameFieldsBlock = findViewById(R.id.nameFieldsBlock);
        piecesBlock = findViewById(R.id.piecesBlock);
        packagedBlock = findViewById(R.id.packagedBlock);
        datesBlock = findViewById(R.id.datesBlock);
        priceWeightBlock = findViewById(R.id.priceWeightBlock);
        storageConditionsBlock = findViewById(R.id.storageConditionsBlock);
        currentPercentageBlock = findViewById(R.id.currentPercentageBlock);

        // Inizializza azioni
        action = (Action) getIntent().getSerializableExtra(ACTION);
        actionType = (ActionType) getIntent().getSerializableExtra(ACTION_TYPE);

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
        priceField.addTextChangedListener(new PriceWeightRelationWatcher(getString(R.string.field_price_tag), pricePerKiloField, weightField, pricePerKiloClearButton, weightClearButton, this));
        pricePerKiloField.addTextChangedListener(new PriceWeightRelationWatcher(getString(R.string.field_pricePerKilo_tag), priceField, weightField, priceClearButton, weightClearButton, this));
        weightField.addTextChangedListener(new PriceWeightRelationWatcher(getString(R.string.field_weight_tag), priceField, pricePerKiloField, priceClearButton, pricePerKiloClearButton, this));
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
        priceField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onPriceFocusLost(priceField); });
        pricePerKiloField.setOnFocusChangeListener((view, hasFocus) -> { if (!hasFocus) onPriceFocusLost(pricePerKiloField); });

        switch (action) {
            case ADD:
                initializeFormLabels(getString(R.string.activity_title_editproduct_add), getString(R.string.activity_button_add));

                nameField.requestFocus(); // Il campo nome ha il focus all'apertura del form
                setCurrentFormAsInitial();

                switch (actionType){
                    case NO_CONSUMPTION: // TODO correggere comportamenti
                        hideConsumptionFields();
                        break;
                    case SHOPPING:
                        hideNonShoppingFields();
                        break;
                    default:
                        consumedBlock.setVisibility(View.VISIBLE);
                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                        }).start();
                        break;
                }
                break;
            case EDIT:
                switch (actionType) {
                    /*case MANAGE: // TODO
                        initializeFormLabels(getString(R.string.activity_title_editproduct_edit), getString(R.string.button_confirm_edit));
                        productToModify = (SingleProduct)getIntent().getSerializableExtra(PRODUCT_TO_EDIT);

                        quantityBlock.setVisibility(View.GONE);

                        hideStatusFields();

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(productToModify);
                                setCurrentFormAsInitial();
                            });
                        }).start();
                        break;*/
                    case UPDATE:
                        initializeFormLabels(getString(R.string.activity_title_editproduct_update), getString(R.string.activity_button_edit));
                        productToModify = (SingleProduct)getIntent().getSerializableExtra(PRODUCT_TO_EDIT);

                        consumeProductButton.setVisibility(View.VISIBLE);
                        quantityBlock.setVisibility(View.GONE);
                        nameFieldsBlock.setVisibility(View.GONE); // TODO mostrare in modo diverso, non modificabile
                        piecesBlock.setVisibility(View.GONE);
                        packagedCheckBox.setVisibility(View.GONE);
                        datesBlock.setVisibility(View.GONE);
                        priceWeightBlock.setVisibility(View.GONE);
                        storageConditionsBlock.setVisibility(View.GONE);
                        pointOfPurchaseBlock.setVisibility(View.GONE);

                        packagedBlock.setBackground(null);
                        packagedBlock.setPadding(0, 0, 0, 0);

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(productToModify);

                                if (!productToModify.isOpened() && productToModify.isPackaged())
                                    TextUtils.setDate(DateUtils.getCurrentDateWithoutTime(), openingDateField);

                                setCurrentFormAsInitial();
                            });
                        }).start();
                        break;
                    case SHOPPING: // Modifica di un prodotto nel carrello
                        initializeFormLabels(getString(R.string.activity_title_editproduct_edit), getString(R.string.activity_button_edit));

                        hideNonShoppingFields();

                        quantityField.setText(String.valueOf(getIntent().getIntExtra(QUANTITY, ProductForm.MIN_QUANTITY)));

                        productToModify = (SingleProduct)getIntent().getSerializableExtra(PRODUCT_TO_EDIT);
                        fillFieldsFromProduct(productToModify);
                        setCurrentFormAsInitial();
                        break;
                    case CONSUMED:
                        initializeFormLabels(getString(R.string.activity_title_editproduct_edit), getString(R.string.activity_button_edit));
                        productToModify = (SingleProduct)getIntent().getSerializableExtra(PRODUCT_TO_EDIT);

                        quantityBlock.setVisibility(View.GONE);
                        hideConsumptionFields();
                        consumptionDateBlock.setPadding(0, 0, 0, 0);
                        consumedBlock.setVisibility(View.VISIBLE);
                        consumedCheckBox.setVisibility(View.GONE);

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(productToModify);
                                setCurrentFormAsInitial();
                            });
                        }).start();
                        break;
                    default:
                        initializeFormLabels(getString(R.string.activity_title_editproduct_edit), getString(R.string.activity_button_edit));
                        productToModify = (SingleProduct)getIntent().getSerializableExtra(PRODUCT_TO_EDIT);

                        quantityBlock.setVisibility(View.GONE);

                        new Thread(() -> {
                            initializePointsOfPurchaseSpinner(); // TODO mettere a fattor comune con le altre chiamate uguali nello switch
                            runOnUiThread(() -> {
                                fillFieldsFromProduct(productToModify);
                                setCurrentFormAsInitial();
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

    // Nasconde i campi relativi alla consumazione del prodotto
    private void hideConsumptionFields(){
        openedBlock.setVisibility(View.GONE);
        currentConsumptionBlock.setVisibility(View.GONE);
        consumedBlock.setVisibility(View.GONE);
    }

    private void setCurrentFormAsInitial(){
        startingForm = getCurrentForm();
    }

    private void initializeFormLabels(String title, String confirmButtonText){
        setTitle(title);
        confirmButton.setText(confirmButtonText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        switch(action){
            case ADD:
                menu.findItem(R.id.consume).setVisible(false);
                menu.findItem(R.id.restore).setVisible(false);
                menu.findItem(R.id.delete).setVisible(false);
                break;
            case EDIT:
                switch(actionType){
                    case CONSUMED:
                        menu.findItem(R.id.consume).setVisible(false);
                        break;
                    default:
                        menu.findItem(R.id.restore).setVisible(false);
                        break;
                }
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.consume:
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        new Thread(() -> {
                            SingleProduct p = createProductFromFields();
                            p.setId(productToModify.getId());
                            p.setConsumed(true);
                            p.setConsumptionDate(DateUtils.getCurrentDateWithoutTime());

                            if (productDatabase.productDao().update(p) > 0) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success_consume), Toast.LENGTH_LONG).show());
                            } else {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_databaseAction), Toast.LENGTH_LONG).show());
                            }

                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }).start();
                    }
                };
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_body_consume))
                        .setTitle(getString(R.string.dialog_title_consume))
                        .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                        .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                        .show();
                return true;
            case R.id.restore:
                dialogClickListener = (dialog, which) -> {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        new Thread(() -> {
                            SingleProduct p = createProductFromFields();
                            p.setId(productToModify.getId());
                            p.setConsumed(false);
                            p.setConsumptionDate(null);

                            if (productDatabase.productDao().update(p) > 0) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success_unconsume), Toast.LENGTH_LONG).show());
                            } else {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_databaseAction), Toast.LENGTH_LONG).show());
                            }

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(MainActivity.FILTER, p.getActualStorageCondition());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }).start();
                    }
                };
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_body_unconsume))
                        .setTitle(getString(R.string.dialog_title_unconsume))
                        .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                        .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                        .show();
                return true;
            case R.id.delete:
                dialogClickListener = (dialog, which) -> {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        new Thread(() -> {
                            if (productDatabase.productDao().deleteById(productToModify.getId()) > 0) {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(MainActivity.DELETE, true);
                                setResult(RESULT_OK, resultIntent);
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success_delete), Toast.LENGTH_LONG).show());
                                finish();
                            } else {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.error_databaseAction), Toast.LENGTH_LONG).show());
                            }
                        }).start();
                    }
                };
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_body_delete))
                        .setPositiveButton(getString(R.string.dialog_button_delete), dialogClickListener)
                        .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                        .show();
                return true;
            case R.id.reset:
                switch(actionType){
                    case UPDATE:
                        dialogClickListener = (dialog, which) -> {
                            if(which == DialogInterface.BUTTON_POSITIVE){
                                resetConsumptionState();
                            }
                        };
                        new AlertDialog.Builder(this)
                                .setMessage(getString(R.string.dialog_body_reset_consumption))
                                .setTitle(getString(R.string.dialog_title_reset_consumption))
                                .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                                .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                                .show();
                        return true;
                    case SHOPPING:
                        dialogClickListener = (dialog, which) -> {
                            if(which == DialogInterface.BUTTON_POSITIVE){
                                resetState();
                            }
                        };
                        new AlertDialog.Builder(this)
                                .setMessage(getString(R.string.dialog_body_reset_state))
                                .setTitle(getString(R.string.dialog_title_reset_state))
                                .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                                .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                                .show();
                        return true;
                    default:
                        View resetDialogView = getLayoutInflater().inflate(R.layout.lose_state_dialog, null);
                        RadioButton radioButtonPartial = resetDialogView.findViewById(R.id.radio_reset_partial);
                        RadioButton radioButtonMin = resetDialogView.findViewById(R.id.radio_reset_min);

                        dialogClickListener = (dialog, which) -> {
                            if(which == DialogInterface.BUTTON_POSITIVE){
                                if(radioButtonPartial.isChecked()) {
                                    resetState();
                                } else if(radioButtonMin.isChecked()) {
                                    resetConsumptionState();
                                }
                            }
                        };
                        new AlertDialog.Builder(this)
                                .setView(resetDialogView)
                                .setTitle(getString(R.string.dialog_title_reset_state))
                                .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                                .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                                .show();
                        return true;
                }
            case R.id.fillFromInsertedProduct:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(ACTION, MainActivity.Action.PICK);
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
        currentPercentageField.setText(String.valueOf(SingleProduct.DEFAULT_PERCENTAGEQUANTITY));
        currentWeightSlider.setProgress(currentWeightSlider.getMax());
    }

    // Mostra avviso nel caso di campi che modificano il prodotto
    @Override
    public void onBackPressed() {
        if(startingForm.equals(getCurrentForm()))
            super.onBackPressed();
        else {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                if(which == DialogInterface.BUTTON_POSITIVE){
                    super.onBackPressed();
                }
            };
            new AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_body_editproduct_back))
                .setTitle(getString(R.string.dialog_title_warning))
                .setPositiveButton(getString(R.string.dialog_button_exit), dialogClickListener)
                .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                .show();
        }
    }

    private ProductForm getCurrentForm(){
        return new ProductForm(createProductFromFields(), TextUtils.getInt(quantityField), TextUtils.getDate(expiryDateField), TextUtils.getInt(expiryDaysAfterOpeningField));
    }

    private void initializeSuggestions(){
        new Thread(() -> {
            List<SingleProduct> products = productDatabase.productDao().getAll(); // TODO Prendi tutti i prodotti non uguali

            runOnUiThread(() -> {
                @SuppressWarnings("unchecked")
                List<SingleProduct> cartProducts = (List<SingleProduct>) getIntent().getSerializableExtra(SUGGESTIONS);
                if(cartProducts != null){
                    products.addAll(cartProducts);
                }

                Set<String> nameSuggestionsList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                Set<String> brandSuggestionsList = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                for(int i=0; i<products.size(); i++){
                    if(products.get(i).getName()!=null)
                        nameSuggestionsList.add(products.get(i).getName());
                    if(products.get(i).getBrand()!=null)
                        brandSuggestionsList.add(products.get(i).getBrand());
                }

                nameField.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Objects.requireNonNull(nameSuggestionsList.toArray())));
                brandField.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Objects.requireNonNull(brandSuggestionsList.toArray())));
            });
        }).start();
    }

    // Compila tutti i campi con i dati del prodotto da modificare
    private void fillFieldsFromProduct(SingleProduct p) {

        // TODO IN MODALITÀ MANAGE RIEMPIRE SOLO CON I CAMPI CHE SERVONO

        // TODO Mettere a fattor comune con codice in case("add"), PiecesWatcher e PriceWeightRelationWatcher
        if (p.getPieces()==1 && p.getWeight()==0) {
            currentPercentageBlock.setVisibility(View.VISIBLE);
        } else {
            currentPercentageBlock.setVisibility(View.GONE);
        }

        if (p.getWeight()==0) {
            currentWeightBlock.setVisibility(View.GONE);
        } else {
            currentWeightBlock.setVisibility(View.VISIBLE);
        }

        if (p.getPieces()==1) {
            currentPiecesBlock.setVisibility(View.GONE);
        } else {
            currentPiecesBlock.setVisibility(View.VISIBLE);
        }

        if (p.isConsumed()) {
            consumedCheckBox.setChecked(true);
        } else {
            consumedCheckBox.setChecked(false);
        }

        TextUtils.editFieldNotFromUser(consumptionDateField, DateUtils.getFormattedDate(p.getConsumptionDate()));

        TextUtils.setText(p.getName(), nameField);

        TextUtils.setText(p.getBrand(), brandField);

        if(p.getPricePerKilo()==0 || p.getWeight()==0) {
            TextUtils.setPrice(p.getPrice(), priceField);
        }
        if(p.getPrice()==0 || p.getWeight()==0) {
            TextUtils.setPrice(p.getPricePerKilo(), pricePerKiloField);
        }
        if(p.getPrice()==0 || p.getPricePerKilo()==0) {
            TextUtils.setWeight(p.getWeight(), weightField);
        }

        if ((p.getWeight()>0 || (p.getPrice()>0 && p.getPricePerKilo()>0)) && p.getCurrentWeight()>0) { // Se il peso è definito/generato e currentWeight definito
            TextUtils.setWeight(p.getCurrentWeight(), currentWeightField);
        } else {
            TextUtils.setWeight(p.getWeight(), currentWeightField);
        }

        currentPercentageField.setText(String.valueOf(p.getPercentageQuantity()));

        if(p.getExpiringDaysAfterOpening()>0) {
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, String.valueOf(p.getExpiringDaysAfterOpening()));
        } else if(p.getExpiringDaysAfterOpening()==-1) {
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, "0");
        } else {
            TextUtils.editFieldNotFromUser(expiryDaysAfterOpeningField, "");
        }

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

        if (p.getPieces()==1 && (p.getWeight()==0 && (p.getPrice()==0 || p.getPricePerKilo()==0))) { // Se il pezzo è unico e il peso non è definito/generato
            currentWeightSlider.setTag(getString(R.string.currentweightslider_tag_percentage));
            currentWeightSlider.setMax((int) SingleProduct.DEFAULT_PERCENTAGEQUANTITY);
            currentWeightSlider.setProgress((int) Math.ceil(p.getPercentageQuantity()));
        } else if (p.getPieces()>SingleProduct.DEFAULT_PIECES) {
            currentWeightSlider.setTag(getString(R.string.currentweightslider_tag_pieces));
            currentWeightSlider.setMax(p.getPieces());
            currentWeightSlider.setProgress(p.getCurrentPieces());
        } else if (p.getWeight()>0 || (p.getPrice()>0 && p.getPricePerKilo()>0)) { // Se il peso è definito/generato
            currentWeightSlider.setTag(getString(R.string.currentweightslider_tag_weight));
            currentWeightSlider.setMax(TextUtils.getInt(weightField));
            currentWeightSlider.setProgress(TextUtils.getInt(currentWeightField));
        }
    }

    private void insertProduct(SingleProduct newProduct){
        new Thread(() -> {
            Intent resultIntent = new Intent();

            switch (action) {
                case ADD:  // Se si tratta di un'aggiunta
                    switch(actionType){
                        case SHOPPING:
                            resultIntent.putExtra(Cart.NEW_PRODUCT, newProduct);
                            resultIntent.putExtra(Cart.QUANTITY, TextUtils.getInt(quantityField));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                            break;
                        default:
                            List<SingleProduct> productsToAdd = new ArrayList<>();
                            for (int i = 0; i < TextUtils.getInt(quantityField); i++)
                                productsToAdd.add(newProduct);
                            productDatabase.productDao().insertAll(productsToAdd); // TODO gestire valore di ritorno
                            resultIntent.putExtra(MainActivity.FILTER, newProduct.getActualStorageCondition());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                            break;
                    }
                case EDIT:  // Se si tratta di una modifica
                    final Runnable showDbErrorAsToast = () -> Toast.makeText(getApplicationContext(), getString(R.string.error_databaseAction), Toast.LENGTH_LONG).show(); // TODO standardizzare ?

                    switch(actionType){
                        case UPDATE:
                            newProduct.setId(productToModify.getId());
                            if (productDatabase.productDao().update(newProduct) > 0) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success_update), Toast.LENGTH_LONG).show());
                                resultIntent.putExtra(MainActivity.FILTER, newProduct.getActualStorageCondition());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                runOnUiThread(showDbErrorAsToast);
                            }
                            break;
                        case SHOPPING:
                            if (productToModify != null) {
                                resultIntent.putExtra(Cart.QUANTITY, TextUtils.getInt(quantityField));
                                resultIntent.putExtra(Cart.EDITED_PRODUCT, newProduct);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_generic), Toast.LENGTH_LONG).show();
                            }
                            break;
                        default:
                            newProduct.setId(productToModify.getId());
                            if (productDatabase.productDao().update(newProduct) > 0) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success_edit), Toast.LENGTH_LONG).show());
                                resultIntent.putExtra(MainActivity.FILTER, newProduct.getActualStorageCondition());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                runOnUiThread(showDbErrorAsToast);
                            }
                            break;
                    }
            }
        }).start();
    }

    // Metodo chiamato alla pressione del tasto di conferma, che può essere l'aggiunta o la modifica del prodotto
    public void onConfirmButtonClick(View view) {
        if (TextUtils.isEmpty(nameField)) { // Il campo nome è obbligatorio
            Toast.makeText(getApplicationContext(), getString(R.string.error_fields_name_empty), Toast.LENGTH_LONG).show();
            setFocusAndScrollToView(findViewById(R.id.nameBlock));
        } else if(actionType==ActionType.MANAGE){ // TODO implementare
            SingleProduct templateProduct = createGeneralProductFromFields(); // Prodotto compilato con solo i campi strettamente relativi al prodotto

            if(action==Action.EDIT){
                new Thread(() -> {
                    SingleProduct p = productDatabase.productDao().get(productToModify.getId());
                    p.setPackaged(templateProduct.isPackaged());
                    p.setName(templateProduct.getName());
                    p.setBrand(templateProduct.getBrand());
                    p.setPrice(templateProduct.getPrice());
                    p.setWeight(templateProduct.getWeight());
                    p.setPricePerKilo(templateProduct.getPricePerKilo());
                    p.setPieces(templateProduct.getPieces());
                    p.setStorageCondition(templateProduct.getStorageCondition());
                    p.setOpenedStorageCondition(templateProduct.getOpenedStorageCondition());
                    p.setExpiringDaysAfterOpening(templateProduct.getExpiringDaysAfterOpening());

                    if (productDatabase.productDao().update(p) > 0) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Prodotto modificato", Toast.LENGTH_LONG).show()); // STRINGS.XML
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }).start();
            }
        } else {
            SingleProduct formProduct = createProductFromFields();
            List<String> dateWarnings = DateUtils.getDateWarnings(formProduct, action, actionType);

            if(dateWarnings.size()>0){
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        insertProduct(formProduct);
                    }
                };

                StringBuilder msg = new StringBuilder(getString(R.string.dialog_body_warning_suspiciousdates) + "\n\n");
                for(int i=0; i<dateWarnings.size(); i++){
                    msg.append(getString(R.string.dialog_body_warning_suspiciousdates_singleentry, dateWarnings.get(i))); // TODO Trovare il modo di parametrizzare in strings.xml
                }
                msg.append("\n").append(getString(R.string.dialog_body_warning_suspiciousdates_question)); // TODO Specializzare in base al tipo di modifica

                new AlertDialog.Builder(this)
                        .setMessage(msg.toString())
                        .setTitle(getString(R.string.dialog_title_warning_suspiciousdates)) // TODO Specializzare in base al tipo di modifica
                        .setPositiveButton(getString(R.string.dialog_button_continue), dialogClickListener) // TODO Specializzare in base al tipo di modifica
                        .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener) // TODO Specializzare in base al tipo di modifica
                        .show();
            } else {
                insertProduct(formProduct);
            }
        }
    }

    // TODO mettere a fattor comune con lo stesso metodo in MainAcitvity
    public void consumeProduct(View view) {
        SingleProduct newProduct = createProductFromFields();
        newProduct.setId(productToModify.getId());
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if(which == DialogInterface.BUTTON_POSITIVE){
                new Thread(() -> {
                    newProduct.setConsumed(true);
                    newProduct.setConsumptionDate(DateUtils.getCurrentDateWithoutTime());

                    if (productDatabase.productDao().update(newProduct) > 0) {
                        runOnUiThread(() -> {
                            Intent resultIntent = new Intent();
                            Toast.makeText(getApplicationContext(), getString(R.string.success_consume), Toast.LENGTH_LONG).show();
                            resultIntent.putExtra(MainActivity.FILTER, newProduct.getActualStorageCondition());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });
                    }
                }).start();
            }
        };
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_body_consume))
                .setTitle(getString(R.string.dialog_title_consume))
                .setPositiveButton(getString(R.string.dialog_button_confirm), dialogClickListener)
                .setNegativeButton(getString(R.string.dialog_button_discard), dialogClickListener)
                .show();
    }

    // Esegui tutte le funzioni della perdita del focus per avere il valore corretto effettivo alla pressione del pulsante di conferma modifiche
    // TODO necessario?
    private void loseFieldsFocus(){
        onPriceFocusLost(priceField);
        onPriceFocusLost(pricePerKiloField);
    }

    // TODO mettere a fattor comune con createProductFromFields()
    // TODO da rivedere
    private SingleProduct createGeneralProductFromFields() {
        loseFieldsFocus(); // TODO necessario?

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

        if(expiryDaysAfterOpeningBlock.getVisibility()==View.VISIBLE && expiryDaysAfterOpeningBlock.isEnabled() && !noExpiryCheckbox.isChecked()) {
            p.setExpiringDaysAfterOpening(TextUtils.getInt(expiryDaysAfterOpeningField));
        }

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
        if (consumedCheckBox.isChecked()) {
            p.setConsumptionDate(TextUtils.getDate(consumptionDateField));
        }

        p.setPurchaseDate(TextUtils.getDate(purchaseDateField));

        if(pointOfPurchaseSpinner.getSelectedItemPosition()>0) {
            p.setPointOfPurchaseId(((PointOfPurchase) pointOfPurchaseSpinner.getSelectedItem()).getId());
        }

        // campi che dipendono dal tipo e dall'apertura del prodotto confezionato
        if(packagedCheckBox.isChecked()){
            if(openedCheckBox.isChecked()) {
                p.setOpened(true);
                p.setOpeningDate(TextUtils.getDate(openingDateField));
            }
        } else {
            p.setPackagingDate(TextUtils.getDate(packagingDateField));
        }

        if(noExpiryCheckbox.isChecked()) {
            p.setExpiryDate(DateUtils.getNoExpiryDate());
        } else if(expiryDateBlock.getVisibility()==View.VISIBLE) {
            p.setExpiryDate(TextUtils.getDate(expiryDateField));
        }

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
            //currentConsumptionBlock.setVisibility(View.VISIBLE);
        } else {
            openingDateBlock.setVisibility(View.GONE);
            //currentConsumptionBlock.setVisibility(View.GONE);
            currentWeightSlider.setProgress(currentWeightSlider.getMax());
            currentPercentageField.setText(String.valueOf(SingleProduct.DEFAULT_PERCENTAGEQUANTITY));
        }

        if(addListener) {
            openedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeOpenedCheckBox(false));
        }
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
            storageConditionSpinnerLabel.setText(getString(R.string.label_storageConditionSpinner_packaged));
            expiryDaysAfterOpeningLabel.setText(getString(R.string.label_expiryDaysAfterOpening_packaged));

            if(actionType!=ActionType.SHOPPING && actionType!=ActionType.CONSUMED && actionType!=ActionType.MANAGE) {
                openedBlock.setVisibility(View.VISIBLE);
                if(openedCheckBox.isChecked()) {
                    openingDateBlock.setVisibility(View.VISIBLE);
                }
            }

            if(actionType != ActionType.UPDATE){
                if(actionType != ActionType.MANAGE) {
                    expiryDateBlock.setVisibility(View.VISIBLE);
                }
                expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
                openedStorageConditionBlock.setVisibility(View.VISIBLE);
            }

            if(currentWeightSlider.getProgress()<currentWeightSlider.getMax()) {
                openedCheckBox.setChecked(true);
            } else {
                openedCheckBox.setChecked(false);
            }
        } else {
            if(actionType!=ActionType.MANAGE) {
                packagingDateBlock.setVisibility(View.VISIBLE);
            }
            noExpiryDaysCheckbox.setVisibility(View.VISIBLE);
            expiryDaysAfterOpeningBlock.setVisibility(View.VISIBLE);
            if(actionType != ActionType.MANAGE){
                changeToExpiryDateButton.setVisibility(View.VISIBLE);
                changeToExpiryDaysButton.setVisibility(View.VISIBLE);
            }
            storageConditionSpinnerLabel.setText(getString(R.string.label_storageConditionSpinner));
            expiryDaysAfterOpeningLabel.setText(getString(R.string.label_expiryDaysAfterOpening));
            expiryDateBlock.setVisibility(View.GONE);

            if(actionType != ActionType.SHOPPING && actionType!=ActionType.CONSUMED && actionType!=ActionType.MANAGE){
                openedBlock.setVisibility(View.GONE);
                openingDateBlock.setVisibility(View.GONE);
            }

            openedStorageConditionBlock.setVisibility(View.GONE);

            if(currentWeightSlider.getProgress() < currentWeightSlider.getMax() && !openedCheckBox.isChecked()) {
                currentWeightSlider.setProgress(currentWeightSlider.getMax());
                currentPercentageField.setText(String.valueOf(SingleProduct.DEFAULT_PERCENTAGEQUANTITY));
            }

            // Mostra l'ultimo visualizzato
            if(expiryDateMode) {
                changeExpiryMode(null);
            }
        }

        if(addListener) {
            packagedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializePackagedCheckBox(false)); // TODO mettere a fattor comune
        }
    }

    private void initializeNoExpiryCheckBox(boolean addListener){
        enableNoExpiryCheckBoxBehaviour(noExpiryCheckbox.isChecked());
        noExpiryDaysCheckbox.setChecked(noExpiryCheckbox.isChecked());

        if(addListener) {
            noExpiryCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeNoExpiryCheckBox(false)); // TODO mettere a fattor comune
        }
    }

    private void initializeConsumedCheckBox(boolean addListener){
        if(consumedCheckBox.isChecked()) {
            consumptionDateBlock.setVisibility(View.VISIBLE);
        } else {
            consumptionDateBlock.setVisibility(View.GONE);
        }

        if(addListener) {
            consumedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeConsumedCheckBox(false)); // TODO mettere a fattor comune
        }
    }

    private void enableNoExpiryCheckBoxBehaviour(boolean enable){
        int disabledHintColor = Color.parseColor("#d8d8d8");
        int enabledHintColor = Color.parseColor("#a7a7a7");

        expiryDateField.setEnabled(!enable);
        expiryDateClearButton.setEnabled(!enable);
        expiryDateFieldLabel.setEnabled(!enable);
        expiryDaysAfterOpeningField.setEnabled(!enable);
        expiryDaysAfterOpeningClearButton.setEnabled(!enable);
        expiryDaysAfterOpeningLabel.setEnabled(!enable);
        changeToExpiryDateButton.setEnabled(!enable);
        changeToExpiryDaysButton.setEnabled(!enable);

        if (enable) {
            expiryDateField.setHintTextColor(disabledHintColor);
            expiryDaysAfterOpeningField.setHintTextColor(disabledHintColor);
        } else {
            expiryDateField.setHintTextColor(enabledHintColor);
            expiryDaysAfterOpeningField.setHintTextColor(enabledHintColor);
        }
    }

    // TODO controllare thread
    private void initializeStorageSpinners() {
        new Thread(() -> {
            List<Filter> filters = productDatabase.filterDao().getFilters();

            runOnUiThread(() -> {
                storageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, filters));
                openedStorageConditionSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, filters));

                if (actionType==ActionType.SHOPPING) {
                    storageConditionSpinner.setSelection(0); // TODO permettere di selezionare il valore di default
                } else {
                    storageConditionSpinner.setSelection(getIntent().getIntExtra(MainActivity.FILTER, 0));
                }
                openedStorageConditionSpinner.setSelection(storageConditionSpinner.getSelectedItemPosition());
            });
        }).start();
    }

    private void initializePointsOfPurchaseSpinner() {
        List<PointOfPurchase> pointsOfPurchase = productDatabase.pointOfPurchaseDao().getPointsOfPurchase(); // TODO Chiamare da mainactivity e passarli qui da intent?
        runOnUiThread(() -> {
            // Aggiungi un prodotto fake che rappresenti la selezione nulla
            PointOfPurchase noSelection = new PointOfPurchase();
            if(pointsOfPurchase.size()>0) {
                noSelection.setName(getString(R.string.spinner_noSelection));
            } else {
                noSelection.setName(getString(R.string.spinner_noPointsOfPurchase));
                pointOfPurchaseSpinner.setEnabled(false);
            }
            pointsOfPurchase.add(0, noSelection);
            pointOfPurchaseSpinner.setAdapter(new PointsOfPurchaseSpinnerAdapter(this, R.layout.storage_condition_spinner_item, pointsOfPurchase));
        });
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

    // Immissione data
    public void showDatePickerDialog(View v) {
        Bundle args = new Bundle();

        if(v == packagingDateField || v == expiryDateField){ // !!! Elencare qui tutti i campi che devono usare lo SpinnerDatePicker !!!
            DialogFragment f = new SpinnerDatePickerFragment();
            args.putInt(SpinnerDatePickerFragment.DATE_FIELD_ID, v.getId());
            f.setArguments(args);
            f.show(getSupportFragmentManager(), null);
        } else {
            DialogFragment f = new DatePickerFragment();
            args.putInt(DatePickerFragment.DATE_FIELD_ID, v.getId());
            f.setArguments(args);
            f.show(getSupportFragmentManager(), null);
        }
    }

    // Modifica i pezzi tramite i relativi pulsanti
    public void editPieces(View view) {
        TextUtils.editQuantityByButtons((Button)view, piecesField, SingleProduct.DEFAULT_PIECES, MAX_PIECES);
    }

    // Modifica la quantità tramite i relativi pulsanti
    public void editQuantity(View view) {
        TextUtils.editQuantityByButtons((Button)view, quantityField, ProductForm.MIN_QUANTITY, MAX_QUANTITY);
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
        if(view==changeToExpiryDateButton) {
            expiryDateMode = true;
        } else if(view==changeToExpiryDaysButton) {
            expiryDateMode = false;
        }
        changeToExpiringDateMode(expiryDateBlock.getVisibility()==View.GONE && expiryDaysAfterOpeningBlock.getVisibility()==View.VISIBLE);
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

        /*if (requestCode == PICK_REQUEST) { // TODO
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
        }*/
    }
}
