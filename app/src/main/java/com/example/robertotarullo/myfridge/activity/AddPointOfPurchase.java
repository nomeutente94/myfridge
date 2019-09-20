package com.example.robertotarullo.myfridge.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.database.DatabaseUtils;
import com.example.robertotarullo.myfridge.database.ProductDatabase;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.utils.TextUtils;

public class AddPointOfPurchase extends AppCompatActivity {

    private EditText nameField;
    private Button addButton;

    // dichiarazione delle variabili di database
    private ProductDatabase productDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Aggiungi punto di acquisto");
        setContentView(R.layout.activity_add_point_of_purchase);

        nameField = findViewById(R.id.nameField);

        addButton = findViewById(R.id.addButton);

        // Ottieni un riferimento al db
        productDatabase = DatabaseUtils.getDatabase(getApplicationContext());

        addButton.setOnClickListener(v -> {
            if (isEmpty(nameField)) {
                Toast.makeText(getApplicationContext(), "Il campo nome non può essere vuoto", Toast.LENGTH_LONG).show();
                setFocusAndScrollToView(findViewById(R.id.nameBlock));
            } else {
                final PointOfPurchase p = new PointOfPurchase();
                p.setName(nameField.getText().toString());

                new Thread(() -> {
                    p.setId(productDatabase.pointOfPurchaseDao().insertPointOfPurchase(p));
                    if(p.getId()!=-1) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Punto di acquisto aggiunto", Toast.LENGTH_LONG).show());
                        finish();
                    } else
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nell'inserimento del punto di acquisto", Toast.LENGTH_LONG).show());
                }).start();
            }
        });
    }

    // Sposta il focus su una determinata view
    private final void setFocusAndScrollToView(final View view){
        findViewById(R.id.listScrollView).post(() -> {
            findViewById(R.id.listScrollView).scrollTo(0, view.getTop());
            view.requestFocus();
        });
    }

    // Controlla se un campo di testo è vuoto
    private static boolean isEmpty(EditText etText) {
        if(etText.getText().toString().length() > 0)
            return false;
        return true;
    }

    public void clearField(View view) {
        TextUtils.clearField(view);
    }
}
