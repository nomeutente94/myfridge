package com.example.robertotarullo.myfridge.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.DateSpinnerAdapter;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class SpinnerDatePickerFragment extends DialogFragment{

    private Spinner expiryDateDaySpinner;
    private Spinner expiryDateMonthSpinner;
    private Spinner expiryDateYearSpinner;
    private EditText dateField;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.spinner_date_picker, null);

        dateField = getActivity().findViewById(R.id.expiryDateField);

        expiryDateDaySpinner = view.findViewById(R.id.expiryDateDaySpinner);
        expiryDateMonthSpinner = view.findViewById(R.id.expiryDateMonthSpinner);
        expiryDateYearSpinner = view.findViewById(R.id.expiryDateYearSpinner);

        initializeExpiryDateSpinner(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner);
        DateUtils.setDate(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner, TextUtils.getDate(dateField));

        builder.setView(view)
            .setTitle("Data di scadenza")
            .setPositiveButton("Ok", (dialog, id) -> {})
            .setNegativeButton("Annulla", (dialog, id) -> SpinnerDatePickerFragment.this.getDialog().cancel());
        return builder.create();
    }

    @Override
    public void onStart(){
        super.onStart();
        if(getDialog() != null){
            Button positiveButton = ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                int day = expiryDateDaySpinner.getSelectedItemPosition();
                int month = expiryDateMonthSpinner.getSelectedItemPosition();
                int year = expiryDateYearSpinner.getSelectedItemPosition();

                String dayAsString = expiryDateDaySpinner.getSelectedItem().toString();
                String monthAsString = expiryDateMonthSpinner.getSelectedItem().toString();
                String yearAsString = expiryDateYearSpinner.getSelectedItem().toString();

                if(
                    (day==0 && month==0 && year==0) || // Se non è stato compilato nessun campo
                    (day>0 && month==0 && year==0) || // Se è stato compilato solo il giorno di scadenza
                    (day==0 && month>0 && year==0) || // Se è stato compilato solo il mese di scadenza
                    ((day>0 && month>0 && year==0) && (!DateUtils.isDateValid(dayAsString, monthAsString, "2019"))) || // Se è stato compilato 29/02 ma l'anno corrente non è bisestile // TODO settare anno corrente
                    ((day>0 && month>0 && year>0) && (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString))) // Se è stato compilato 29/02 ma l'anno non è bisestile
                ){
                    Toast.makeText(this.getActivity(), "La data di scadenza immessa non è valida", Toast.LENGTH_LONG).show();
                } else {
                    dateField.setText(DateUtils.getFormattedDate(DateUtils.getExpiryDate(expiryDateDaySpinner, expiryDateMonthSpinner, expiryDateYearSpinner)));
                    dismiss();
                }
            });
        }
    }

    private void initializeExpiryDateSpinner(Spinner expiryDateDaySpinner, Spinner expiryDateMonthSpinner, Spinner expiryDateYearSpinner) {
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

        expiryDateDaySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days));
        expiryDateMonthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months));
        expiryDateYearSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years));
    }
}
