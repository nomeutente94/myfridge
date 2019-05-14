package com.example.robertotarullo.myfridge.Utils;

import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public abstract class DateUtils {

    public static void setDate(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner, Date date){
        if(date!=null){
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

            String day = dayFormat.format(date);
            String month = monthFormat.format(date);
            String year = yearFormat.format(date);

            boolean found = false;

            for(int i=0; i<daySpinner.getAdapter().getCount() && !found; i++){
                if(day.equals(daySpinner.getItemAtPosition(i))) {
                    daySpinner.setSelection(i);
                    found = true;
                }
            }
            found = false;
            for(int i=0; i<monthSpinner.getAdapter().getCount() && !found; i++){
                if(month.equals(monthSpinner.getItemAtPosition(i))) {
                    monthSpinner.setSelection(i);
                    found = true;
                }
            }
            found = false;
            for(int i=0; i<yearSpinner.getAdapter().getCount() && !found; i++){
                if(year.equals(yearSpinner.getItemAtPosition(i))) {
                    yearSpinner.setSelection(i);
                    found = true;
                }
            }
        }
    }

    public static Date getDate(Spinner daySpinner, Spinner monthSpinner, Spinner yearSpinner){
        String day, month, year;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        if(daySpinner.getSelectedItemPosition()==0 && monthSpinner.getSelectedItemPosition()==0 && yearSpinner.getSelectedItemPosition()>0) {
            day = "31";
            month = "12";
            year = yearSpinner.getSelectedItem().toString();
        }
        else if(daySpinner.getSelectedItemPosition()==0 && monthSpinner.getSelectedItemPosition()>0 && yearSpinner.getSelectedItemPosition()>0){
            month = monthSpinner.getSelectedItem().toString();
            year = yearSpinner.getSelectedItem().toString();
            day = getLastDayOfMonth(month, year);
        } else {
            day = daySpinner.getSelectedItem().toString();
            month = monthSpinner.getSelectedItem().toString();
            year = yearSpinner.getSelectedItem().toString();
        }

        String date = day + "/" + month + "/" + year;

        Date convertedDate = null;

        try {
            convertedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            System.out.println("Data non valida: " + date);
        }

        System.out.println("DATA LETTA: " + convertedDate);

        return convertedDate;
    }

    public static String getLastDayOfMonth(String month, String year){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
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

    // aggiunge giorni ad una data e la ritorna
    public static int getDaysByDateDiff(Date date1, Date date2){
        if(date1!=null && date2!=null){
            long diff = date2.getTime() - date1.getTime();
            return (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }
        return 0;
    }

    // ottieni una stringa formattata da un oggetto Calendar
    public static String getFormattedDate(Calendar cal){
        String dateAsString = "";
        if(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)).length()==1){
            dateAsString += "0";
        }
        dateAsString += String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "/";
        if(String.valueOf(cal.get(Calendar.MONTH)+1).length()==1){
            dateAsString += "0";
        }
        dateAsString += String.valueOf(cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
        return dateAsString;
    }

    // ottieni una stringa formattata da un oggetto Date
    public static String getFormattedDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getFormattedDate(cal);
    }

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


}
