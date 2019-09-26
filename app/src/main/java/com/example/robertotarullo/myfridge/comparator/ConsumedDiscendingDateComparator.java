package com.example.robertotarullo.myfridge.comparator;

import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;

import java.util.Comparator;
import java.util.Date;

public class ConsumedDiscendingDateComparator implements Comparator<Product> {
    public int compare(Product p1, Product p2){

        // -1 mette in alto p1
        // 1 mette in alto p2
        // 0 mantiene l'ordine predefinito

        // Ordine: data decrescente > non specificata
        Date date1 = ((SingleProduct)p1).getConsumptionDate();
        Date date2 = ((SingleProduct)p2).getConsumptionDate();

        if(date1!=null && date2!=null){
            return -date1.compareTo(date2);
        } else if(date1!=null)
            return -1;
        else if(date2!=null)
            return 1;
        else // entrambe null
            return 0;
    }
}
