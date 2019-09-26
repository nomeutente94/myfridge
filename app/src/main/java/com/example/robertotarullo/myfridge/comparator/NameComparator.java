package com.example.robertotarullo.myfridge.comparator;

import com.example.robertotarullo.myfridge.bean.Product;
import com.example.robertotarullo.myfridge.bean.SingleProduct;

import java.util.Comparator;
import java.util.Date;

public class NameComparator implements Comparator<Product> {
    public int compare(Product p1, Product p2){
        return p1.getName().compareTo(p2.getName());
    }
}
