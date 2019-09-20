package com.example.robertotarullo.myfridge.comparator;

import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.utils.DateUtils;

import java.util.Comparator;
import java.util.Date;

public class AscendingDateComparator implements Comparator<Product> {
    public int compare(Product p1, Product p2){

        // -1 mette in alto p1
        // 1 mette in alto p2
        // 0 mantiene l'ordine predefinito

        // Ordine NON consumati: data decrescente > mai > non specificata

        Date date1 = DateUtils.getActualExpiryDate(p1);
        Date date2 = DateUtils.getActualExpiryDate(p2);

        if(date1!=null && date2!=null){
            if(!date1.equals(date2) && date1.equals(DateUtils.getNoExpiryDate()))
                return 1;
            else if(!date1.equals(date2) && date2.equals(DateUtils.getNoExpiryDate()))
                return -1;
            else
                return date1.compareTo(date2);
        } else if(date1!=null)
            return -1;
        else if(date2!=null)
            return 1;
        else // entrambe null
            return 0;
    }
}
