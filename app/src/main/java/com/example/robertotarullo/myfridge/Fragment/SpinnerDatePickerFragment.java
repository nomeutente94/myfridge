package com.example.robertotarullo.myfridge.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.robertotarullo.myfridge.Adapter.DateSpinnerAdapter;
import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.Utils.DateUtils;
import com.example.robertotarullo.myfridge.Utils.TextUtils;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SpinnerDatePickerFragment extends DialogFragment{

    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;

    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;

    private static final int MIN_YEAR = 1990; // la data 01/01/1970 porta a dei conflitti con la rappresentazione "mai" sulla data di scadenza
    private static final int YEAR_OFFSET = 100;
    private static final int MAX_YEAR = MIN_YEAR + YEAR_OFFSET;

    private Calendar maxDate = new GregorianCalendar(MAX_YEAR, MAX_MONTH, MAX_DAY);
    private Calendar minDate = new GregorianCalendar(MIN_YEAR, MIN_MONTH, MIN_DAY);

    private Spinner daySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private EditText dateField;

    private int day;
    private int month;
    private int year;

    private String dayAsString;
    private String monthAsString;
    private String yearAsString;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.spinner_date_picker, null);

        dateField = getActivity().findViewById(getArguments().getInt("id"));

        daySpinner = view.findViewById(R.id.expiryDateDaySpinner);
        monthSpinner = view.findViewById(R.id.expiryDateMonthSpinner);
        yearSpinner = view.findViewById(R.id.expiryDateYearSpinner);

        setAllowedDateRange(); // Setta range di date permesse
        initializeExpiryDateSpinner(); // Popola i campi
        DateUtils.setDate(daySpinner, monthSpinner, yearSpinner, TextUtils.getDate(dateField)); // Setta alla data presente nel form

        builder.setView(view)
            .setTitle("Seleziona data")
            .setPositiveButton("Ok", (dialog, id) -> {})
            .setNegativeButton("Annulla", (dialog, id) -> SpinnerDatePickerFragment.this.getDialog().cancel());
        return builder.create();
    }

    // Setta range di date permesse
    private void setAllowedDateRange() {
        if(dateField==getActivity().findViewById(R.id.expiryDateField)){
            // Nessun vincolo
        } else if(dateField==getActivity().findViewById(R.id.packagingDateField)){
            List<Date> dates = new ArrayList<>();
            dates.add(Calendar.getInstance().getTime()); // ≤ now
            dates.add(TextUtils.getDate(getActivity().findViewById(R.id.purchaseDateField))); // ≤ purchaseDate
            // TODO dates.add(TextUtils.getDate(getActivity().findViewById(R.id.consumingDateField))); // ≤ consumingDate
            dates.add(TextUtils.getDate(getActivity().findViewById(R.id.expiryDateField))); // ≤ expiryDate
            dates.add(TextUtils.getDate(getActivity().findViewById(R.id.openingDateField))); // ≤ openingDate

            maxDate = DateUtils.getCalendar(DateUtils.getMinDate(dates));
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if(getDialog() != null){
            Button positiveButton = ((AlertDialog)getDialog()).getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                day = daySpinner.getSelectedItemPosition();
                month = monthSpinner.getSelectedItemPosition();
                year = yearSpinner.getSelectedItemPosition();

                dayAsString = daySpinner.getSelectedItem().toString();
                monthAsString = monthSpinner.getSelectedItem().toString();
                yearAsString = yearSpinner.getSelectedItem().toString();

                if(isDateValid()){
                    dateField.setText(DateUtils.getFormattedDate(DateUtils.getExpiryDate(daySpinner, monthSpinner, yearSpinner)));
                    dismiss();
                } else
                    Toast.makeText(this.getActivity(), "La data immessa non è valida", Toast.LENGTH_LONG).show();
            });
        }
    }

    private boolean isDateValid(){
        Date currentDate = DateUtils.getDate(daySpinner, monthSpinner, yearSpinner);

        if(minDate.getTime().before(currentDate) && maxDate.getTime().after(currentDate)){
            if(isExpiryDateValid() || isPackagingDateValid())
                return true;
        }
        return false;
    }

    // Validazione per data di produzione
    private boolean isPackagingDateValid(){
        if(dateField==getActivity().findViewById(R.id.packagingDateField)){
            if(
                (day==0 || month==0 || year==0) || // Se qualche campo non è compilato
                (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString)) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    // Validazione per data di scadenza
    private boolean isExpiryDateValid(){
        if(dateField==getActivity().findViewById(R.id.expiryDateField)){
            if(
                (day==0 && month==0 && year==0) || // Se non è stato compilato nessun campo
                (day>0 && month==0 && year==0) || // Se è stato compilato solo il giorno di scadenza
                (day==0 && month>0 && year==0) || // Se è stato compilato solo il mese di scadenza
                ((day>0 && month>0 && year==0) && (!DateUtils.isDateValid(dayAsString, monthAsString, "2019"))) || // Se è stato compilato 29/02 ma l'anno corrente non è bisestile // TODO settare anno corrente
                ((day>0 && month>0 && year>0) && (!DateUtils.isDateValid(dayAsString, monthAsString, yearAsString))) // Se è stato compilato 29/02 ma l'anno non è bisestile
            ){
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void initializeExpiryDateSpinner() {
        /* Mostra solo il range permesso di date dai valori di minDate e maxDate
        if(!DateUtils.isDateEmpty(dateField)){
            Calendar currentDate = Calendar.getInstance();
            currentDate.setTime(TextUtils.getDate(dateField));

            if(maxDate.get(Calendar.YEAR)==currentDate.get(Calendar.YEAR))
                MAX_MONTH = (maxDate.get(Calendar.MONTH)+1);
            if(maxDate.get(Calendar.MONTH)==(currentDate.get(Calendar.MONTH)))
                MAX_DAY = maxDate.get(Calendar.DAY_OF_MONTH);

            if(minDate.get(Calendar.YEAR)==currentDate.get(Calendar.YEAR))
                MIN_MONTH = (minDate.get(Calendar.MONTH)+1);
            if(minDate.get(Calendar.MONTH)==(currentDate.get(Calendar.MONTH)))
                MIN_DAY = minDate.get(Calendar.DAY_OF_MONTH);
        }
        */

        List<String> days = new ArrayList<>();
        List<String> months = new ArrayList<>();
        List<String> years = new ArrayList<>();

        days.add("DD");
        months.add("MM");
        years.add("YYYY");

        // Inizializza anni
        for(int i=minDate.get(Calendar.YEAR); i<=maxDate.get(Calendar.YEAR); i++)
            years.add(String.valueOf(i));

        // Inizializza mesi
        for(int i = MIN_MONTH; i<= MAX_MONTH; i++) {
            if (String.valueOf(i).length() == 1)
                months.add("0" + i);
            else
                months.add(String.valueOf(i));
        }

        // Inizializza giorni
        for(int i = MIN_DAY; i<= MAX_DAY; i++){
            if(String.valueOf(i).length()==1)
                days.add("0" + i);
            else
                days.add(String.valueOf(i));
        }

        daySpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, days));
        monthSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, months));
        yearSpinner.setAdapter(new DateSpinnerAdapter(this.getActivity(), R.layout.date_spinner_item, years));
    }
}
