package com.example.robertotarullo.myfridge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.robertotarullo.myfridge.adapter.PointsOfPurchaseSpinnerAdapter;
import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.database.ProductDatabase;
import com.example.robertotarullo.myfridge.R;

import java.util.List;

public class ShoppingForm extends AppCompatActivity {

    private ProductDatabase productDatabase;
    private Spinner pointOfPurchaseSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_form);
        setTitle("Seleziona punto d'acquisto");

        productDatabase = ProductDatabase.getInstance(getApplicationContext());
        pointOfPurchaseSpinner = findViewById(R.id.pointOfPurchaseSpinner);

        initializePointsOfPurchaseSpinner();
    }

    private void initializePointsOfPurchaseSpinner() { // TODO stesso codice di EditProduct
        new Thread(() -> {
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
        }).start();
    }

    public void onConfirmButtonClick(View view) {
        if(pointOfPurchaseSpinner.getSelectedItemPosition()==0){
            Toast.makeText(getApplicationContext(), "Selezionare prima un punto di acquisto", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, Cart.class);
            PointOfPurchase currentSelection = (PointOfPurchase)pointOfPurchaseSpinner.getSelectedItem();
            intent.putExtra(Cart.POINT_OF_PURCHASE_ID, currentSelection.getId());
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            //if (resultCode == RESULT_OK) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            //}
        }
    }
}
