package com.example.robertotarullo.myfridge.Utils;

import android.app.Activity;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.Bean.Pack;
import com.example.robertotarullo.myfridge.Bean.Product;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;
import com.example.robertotarullo.myfridge.R;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class DateUtils {

    // codice campo
    public static final int CONSUMING_DATE = 1;
    public static final int EXPIRY_DATE = 2;
    public static final int PACKAGING_DATE = 3;
    public static final int OPENING_DATE = 4;
    public static final int PURCHASE_DATE = 5;

    // data minima possibile globale
    public static final int MIN_DAY = 1;
    public static final int MIN_MONTH = 1;
    public static final int MIN_YEAR = 1990; // la data 01/01/1970 porta a dei conflitti con la rappresentazione "mai" sulla data di scadenza

    // data massima possibile globale
    public static final int MAX_DAY = 31;
    public static final int MAX_MONTH = 12;
    public static final int MAX_YEAR = 2099;

    public static Date getActualExpiryDate(Product p){
        if(p!=null){
            SingleProduct productToCheck;

            if(p instanceof SingleProduct)
                productToCheck = (SingleProduct)p;
            else
                productToCheck = ((Pack)p).getProducts().get(0); // TODO si dà per scontato che tutti i prodotti di un gruppo abbiano la stessa data di scadenza

            if(productToCheck.isPackaged() && productToCheck.isOpened() && productToCheck.getOpeningDate()!=null && productToCheck.getExpiringDaysAfterOpening()>0)
                return getDateByAddingDays(productToCheck.getOpeningDate(), productToCheck.getExpiringDaysAfterOpening());
            return productToCheck.getExpiryDate();
        } else
            return null;
    }

    // Setta gli spinner alla data
    public static void setDate(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner, Date date){
        if(date!=null){
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

            int dayPosition = -1, monthPosition = -1, yearPosition = -1;

            for(int i=0; i<daySpinner.getAdapter().getCount() && dayPosition == -1; i++){
                if(dayFormat.format(date).equals(daySpinner.getItemAtPosition(i)))
                    dayPosition = i;
            }

            for(int i=0; i<monthSpinner.getAdapter().getCount() && monthPosition == -1; i++){
                if(monthFormat.format(date).equals(monthSpinner.getItemAtPosition(i)))
                    monthPosition = i;
            }

            for(int i=0; i<yearSpinner.getAdapter().getCount() && yearPosition == -1; i++){
                if(yearFormat.format(date).equals(yearSpinner.getItemAtPosition(i)))
                    yearPosition = i;
            }

            if(dayPosition>-1 && monthPosition>-1 && yearPosition>-1){
                daySpinner.setSelection(dayPosition);
                monthSpinner.setSelection(monthPosition);
                yearSpinner.setSelection(yearPosition);
            }
        }
    }

    /*public static boolean isDateValid(int day, int month, int year){
        return isDateValid()
    }*/

    // Ritorna false se la combinazione giorno-mese non esiste (es. 31/04)
    public static boolean isDateValid(String day, String month, String year){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateAsString = day + "/" + month + "/" + year; // TODO Permettere di settare il formato della data
        Date convertedDate = getDate(day, month, year); // Leggi data

        // Controlla se la data letta corrisponde a quella inserita
        if(dateAsString.equals(dateFormat.format(convertedDate)))
            return true;

        return false;
    }

    public static Date getExpiryDate(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner){
        int daySelection = daySpinner.getSelectedItemPosition();
        int monthSelection = monthSpinner.getSelectedItemPosition();
        int yearSelection = yearSpinner.getSelectedItemPosition();

        String day = daySpinner.getSelectedItem().toString();
        String month = monthSpinner.getSelectedItem().toString();
        String year = yearSpinner.getSelectedItem().toString();

        // Intuisci data parzialmente immessa
        if(daySpinner.getSelectedItemPosition()==0 && monthSpinner.getSelectedItemPosition()==0 && yearSpinner.getSelectedItemPosition()>0) { // Se è stato inserito soltanto YYYY
            day = "31";
            month = "12";
        } else if(daySpinner.getSelectedItemPosition()==0 && monthSpinner.getSelectedItemPosition()>0 && yearSpinner.getSelectedItemPosition()>0){ // Se è stato inserito soltanto MM e YYYY
            day = getLastDayOfMonth(month, year);
        } else if(daySpinner.getSelectedItemPosition()>0 && monthSpinner.getSelectedItemPosition()>0 && yearSpinner.getSelectedItemPosition()==0){ // Se è stato inserito soltanto DD e MM
            year = "2019"; // TODO calcolare anno attuale
        }

        return getDate(day, month, year);
    }

    public static Date getDate(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner){
        if(daySpinner.getSelectedItemPosition()>0 && monthSpinner.getSelectedItemPosition()>0 && yearSpinner.getSelectedItemPosition()>0)
            return getDate(daySpinner.getSelectedItem().toString(), monthSpinner.getSelectedItem().toString(), yearSpinner.getSelectedItem().toString());
        return null;
    }

    public static Date getNoExpiryDate(){
        return getDate("01", "01", "1970");
    }

    public static Date getCurrentDate(){
        return new Date();
    }

    public static Date getCurrentDateWithoutTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getDate(int day, int month, int year){
        String date = day + "/" + month + "/" + year;
        return getDate(date);
    }

    // Restituisce un oggetto data a partire dalle stringhe giorno, mese e anno, ritorna null se qualche valore non valido
    public static Date getDate(String day, String month, String year){
        String date = day + "/" + month + "/" + year;
        return getDate(date);
    }

    public static Date getDate(String date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // TODO Permettere di settare il formato della data

        try {
            Date convertedDate = dateFormat.parse(date);
            return convertedDate;
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getLastDayOfMonth(String month, String year){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // TODO Permettere di settare il formato della data
        String date = 01 + "/" + month + "/" + year;
        Calendar calendar = new GregorianCalendar();

        try {
            Date convertedDate = dateFormat.parse(date);
            calendar.setTime(convertedDate);
            return String.valueOf(calendar.getActualMaximum(Calendar.DATE));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // aggiunge giorni ad una data e la ritorna
    public static Date getDateByAddingDays(Date date, int days){
        if(date!=null && days>0){
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, days);
            return calendar.getTime();
        }
        return null;
    }

    // calcola la differenza in giorni tra due date
    public static int getDaysByDateDiff(Date dateToSubtract, Date biggerDate){
        if(dateToSubtract!=null && biggerDate!=null){
            long diff = biggerDate.getTime() - dateToSubtract.getTime();
            return (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }
        return 0;
    }

    // ottieni una stringa formattata da un oggetto Calendar
    public static String getFormattedDate(Calendar cal){
        String dateAsString = "";

        if(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length()==1)
            dateAsString += "0";

        dateAsString += String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "/";
        if(String.valueOf(cal.get(Calendar.MONTH)+1).length()==1)
            dateAsString += "0";

        dateAsString += String.valueOf(cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
        return dateAsString;
    }

    // ottieni una stringa formattata da un oggetto Calendar
    public static String getLanguageFormattedDate(Calendar cal){
        String dateAsString = "";

        dateAsString += String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + " ";

        String month = String.valueOf(cal.get(Calendar.MONTH)+1);
        String monthAsString = "error_month";

        if(month.equals("1"))
            monthAsString = "Gen";
        else if(month.equals("2"))
            monthAsString = "Feb";
        else if(month.equals("3"))
            monthAsString = "Mar";
        else if(month.equals("4"))
            monthAsString = "Apr";
        else if(month.equals("5"))
            monthAsString = "Mag";
        else if(month.equals("6"))
            monthAsString = "Giu";
        else if(month.equals("7"))
            monthAsString = "Lug";
        else if(month.equals("8"))
            monthAsString = "Ago";
        else if(month.equals("9"))
            monthAsString = "Set";
        else if(month.equals("10"))
            monthAsString = "Ott";
        else if(month.equals("11"))
            monthAsString = "Nov";
        else if(month.equals("12"))
            monthAsString = "Dic";

        dateAsString += monthAsString + " " + cal.get(Calendar.YEAR);

        return dateAsString;
    }

    // ottieni una stringa formattata da un oggetto Date
    public static String getFormattedDate(Date date){
        if(date!=null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return getFormattedDate(cal);
        }
        return null;
    }

    // ottieni una stringa formattata da un oggetto Date
    public static String getLanguageFormattedDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getLanguageFormattedDate(cal);
    }


    // Confronta due date di tipo Calendar ignorando l'orario
    public static boolean equalsNoTime(Calendar date1, Calendar date2){
        if(date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) && date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR))
            return true;
        else
            return false;
    }

    public static int getYear(TextView dateField){
        return Integer.valueOf(dateField.getText().toString().substring(6));
    }

    public static int getMonth(TextView dateField){
        return Integer.valueOf(dateField.getText().toString().substring(3,5));
    }

    public static int getDay(TextView dateField){
        return Integer.valueOf(dateField.getText().toString().substring(0,2));
    }

    public static boolean isDateEmpty(TextView dateField){
        if(dateField.getText().length()==0)
            return true;
        else
            return false;
    }

    public static Calendar getDate(TextView dateField){
        Calendar c = Calendar.getInstance();
        c.set(DateUtils.getYear(dateField), DateUtils.getMonth(dateField)-1, DateUtils.getDay(dateField));
        return c;
    }

    public static Date getDate(Calendar cal){
        if(cal!=null)
            return cal.getTime();
        return null;
    }

    public static Calendar getCalendar(Date date){
        if(date!=null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }
        return null;
    }

    private static Date getMin(Date date1, Date date2){
        if(date1==null)
            return date2;
        else if(date2==null)
            return date1;
        else if(date1.before(date2))
            return date1;
        else
            return date2;
    }

    private static Date getMax(Date date1, Date date2){
        if(date1==null)
            return date2;
        else if(date2==null)
            return date1;
        else if(date1.before(date2))
            return date2;
        else
            return date1;
    }

    public static Date getMaxDateAllowed(EditText dateField, Activity activity){
        return getAllowedDate(true, dateField, activity);
    }

    public static Date getMinDateAllowed(EditText dateField, Activity activity){
        return getAllowedDate(false, dateField, activity);
    }

    /*private static Date getMaxDateAllowed(EditText dateField, EditText consumingDateField, EditText expiryDateField, EditText packagingDateField, EditText openingDateField, EditText purchaseDateField){

    }*/

    private static Date getAllowedDate(boolean max, EditText dateField, Activity activity){
        EditText expiryDateField = activity.findViewById(R.id.expiryDateField);
        EditText purchaseDateField = activity.findViewById(R.id.purchaseDateField);
        EditText openingDateField = activity.findViewById(R.id.openingDateField);
        EditText packagingDateField = activity.findViewById(R.id.packagingDateField);
        EditText consumingDateField = null; // TODO = activity.findViewById(R.id.consumingDateField);

        Date expiryDate = TextUtils.getDate(expiryDateField);
        Date purchaseDate = TextUtils.getDate(purchaseDateField);
        Date openingDate = TextUtils.getDate(openingDateField);
        Date packagingDate = TextUtils.getDate(packagingDateField);
        Date consumingDate = TextUtils.getDate(consumingDateField);

        if(max){
            Date maxDate = getDate(new GregorianCalendar(MAX_YEAR, MAX_MONTH-1, MAX_DAY));

            if(dateField==consumingDateField){
                maxDate = getMin(getCurrentDate(), maxDate);    // consumingDate <= now
            } else if(dateField==expiryDateField){
                // nessun vincolo
            } else if(dateField==packagingDateField){
                maxDate = getMin(consumingDate, maxDate);       // packagingDate <= consumingDate
                maxDate = getMin(expiryDate, maxDate);          // packagingDate <= expiryDate
                maxDate = getMin(openingDate, maxDate);         // packagingDate <= openingDate
                maxDate = getMin(getCurrentDate(), maxDate);    // packagingDate <= now
                maxDate = getMin(purchaseDate, maxDate);        // packagingDate <= purchaseDate
            } else if(dateField==openingDateField){
                maxDate = getMin(consumingDate, maxDate);       // openingDate <= consumingDate
                maxDate = getMin(getCurrentDate(), maxDate);    // openingDate <= now
            } else if(dateField==purchaseDateField){
                maxDate = getMin(consumingDate, maxDate);       // purchaseDate <= consumingDate
                maxDate = getMin(openingDate, maxDate);         // purchaseDate <= openingDate
                maxDate = getMin(getCurrentDate(), maxDate);    // purchaseDate <= now
            }

            return maxDate;
        } else {
            Date minDate = getDate(new GregorianCalendar(MIN_YEAR, MIN_MONTH-1, MIN_DAY));

            if(dateField==consumingDateField){
                minDate = getMax(packagingDate, minDate);       // consumingDate >= packagingDate
                minDate = getMax(openingDate, minDate);         // consumingDate >= openingDate
                minDate = getMax(purchaseDate, minDate);        // consumingDate >= purchaseDate
            } else if(dateField==expiryDateField){
                minDate = getMax(packagingDate, minDate);       // expiryDate >= packagingDate
            } else if(dateField==packagingDateField){
                // nessun vincolo
            } else if(dateField==openingDateField){
                minDate = getMax(packagingDate, minDate);       // openingDate >= packagingDate
                minDate = getMax(purchaseDate, minDate);        // openingDate >= purchaseDate
            } else if(dateField==purchaseDateField){
                minDate = getMax(packagingDate, minDate);       // purchaseDate >= packagingDate
            }

            return minDate;
        }
    }
}
