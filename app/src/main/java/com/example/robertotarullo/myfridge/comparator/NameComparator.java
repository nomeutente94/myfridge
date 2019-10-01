package com.example.robertotarullo.myfridge.comparator;

import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;

import java.util.Comparator;
import java.util.Date;

public class NameComparator implements Comparator<Product> {
    public int compare(Product p1, Product p2){
        if(p1.getName().compareTo(p2.getName())==0){

            if(p1.getBrand()==null && p2.getBrand()==null)
                return 0;
            else if(p1.getBrand()!=null && p2.getBrand()==null)
                return 1;
            else if(p1.getBrand()==null && p2.getBrand()!=null)
                return -1;
            else
                return p1.getBrand().compareTo(p2.getBrand());

        }
        return p1.getName().compareTo(p2.getName());
    }
}
