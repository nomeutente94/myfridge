package com.example.robertotarullo.myfridge.Listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.StorageSpinnerArrayAdapter;
import com.example.robertotarullo.myfridge.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageConditionSpinnerListener implements AdapterView.OnItemSelectedListener {
    private StorageSpinnerArrayAdapter storageSpinnerAdapter;
    private List<String> openedStorageList;
    private Spinner openedStorageConditionSpinner;

    public StorageConditionSpinnerListener(StorageSpinnerArrayAdapter storageSpinnerAdapter, List<String> openedStorageList, Spinner openedStorageConditionSpinner){
        this.storageSpinnerAdapter = storageSpinnerAdapter;
        this.openedStorageList = openedStorageList;
        this.openedStorageConditionSpinner = openedStorageConditionSpinner;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        System.out.println("openedStorageCondition precedente: " + openedStorageConditionSpinner.getSelectedItem());

        if(openedStorageConditionSpinner.isEnabled()){
            // ripristina la voce mancante
            if(openedStorageList.size()==2){
                if(openedStorageList.get(0).equals("Frigorifero") && openedStorageList.get(1).equals("Congelatore"))
                    openedStorageList.add(0, "Temperatura ambiente");
                else if(openedStorageList.get(0).equals("Temperatura ambiente") && openedStorageList.get(1).equals("Congelatore"))
                    openedStorageList.add(1, "Frigorifero");
                else if(openedStorageList.get(0).equals("Temperatura ambiente") && openedStorageList.get(1).equals("Frigorifero"))
                    openedStorageList.add("Congelatore");
            }

            // cancella la nuova voce
            if(position==0)
                openedStorageList.remove(0);
            else if(position==1)
                openedStorageList.remove(1);
            else if(position==2)
                openedStorageList.remove(2);

            storageSpinnerAdapter.notifyDataSetChanged();
            //openedStorageConditionSpinner.setSelection(0);
        } else {
            openedStorageConditionSpinner.setSelection(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
