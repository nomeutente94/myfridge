package com.example.robertotarullo.myfridge.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.bean.Filter;
import com.example.robertotarullo.myfridge.database.ProductDatabase;

import java.util.List;

public class Settings extends AppCompatActivity {

    // Dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    // View
    private Spinner defaultFilterSpinner;
    private CheckBox lastFilterCheckbox;

    // Impostazioni
    private SharedPreferences prefs;

    // Valori di default
    public static final int INITIAL_FILTER_DEFAULT = 2;
    public static final boolean USE_LAST_USED_FILTER_DEFAULT = false;

    // Chiavi delle impostazioni
    public static final String INITIAL_FILTER = "INITIAL_FILTER";
    public static final String USE_LAST_USED_FILTER = "USE_LAST_USED_FILTER";
    public static final String LAST_USED_FILTER = "LAST_USED_FILTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getString(R.string.activity_title_settings));

        // Ottieni un riferimento al DB
        productDatabase = ProductDatabase.getInstance(getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Riferimenti alle view
        defaultFilterSpinner = findViewById(R.id.defaultFilterSpinner);
        lastFilterCheckbox = findViewById(R.id.lastFilterCheckBox);

        lastFilterCheckbox.setChecked(prefs.getBoolean(USE_LAST_USED_FILTER, USE_LAST_USED_FILTER_DEFAULT));
        initializeLastFilterCheckBox(true);

        syncFilters();
    }

    private void syncFilters(){
        new Thread(() -> {
            List<Filter> filters = productDatabase.filterDao().getFilters();

            runOnUiThread(() -> {
                defaultFilterSpinner.setAdapter(new StorageSpinnerArrayAdapter(this, R.layout.storage_condition_spinner_item, filters));

                for(int i=0; i<filters.size(); i++){
                    if(filters.get(i).getId() == prefs.getLong(INITIAL_FILTER, INITIAL_FILTER_DEFAULT)){
                        defaultFilterSpinner.setSelection(i);
                        break;
                    }
                }
            });
        }).start();
    }

    private void saveStateToPreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(INITIAL_FILTER, ((Filter)defaultFilterSpinner.getSelectedItem()).getId());
        editor.putBoolean(USE_LAST_USED_FILTER, lastFilterCheckbox.isChecked());
        editor.commit();
    }

    private void initializeLastFilterCheckBox(boolean addListener) {
        if(lastFilterCheckbox.isChecked()) {
            defaultFilterSpinner.setEnabled(false);
        } else {
            defaultFilterSpinner.setEnabled(true);
        }

        if(addListener)
            lastFilterCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> initializeLastFilterCheckBox(false));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveStateToPreferences(); // TODO Chiamare qui o alla modifica?
    }
}
