package com.example.robertotarullo.myfridge.bean;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

public class ProductForm {

    public static final int MIN_QUANTITY = 1;

    private SingleProduct product;
    private int quantity, expiryDaysAfterOpening;
    private Date expiryDate;

    public ProductForm(SingleProduct product, int quantity){
        this.product = product;
        this.quantity = quantity;
    }

    public ProductForm(SingleProduct product, int quantity, Date expiryDate, int expiryDaysAfterOpening){
        this.product = product;
        this.quantity = quantity;
        this.expiryDaysAfterOpening = expiryDaysAfterOpening;
        this.expiryDate = expiryDate;
    }

    public int getQuantity(){
        return quantity;
    }

    public SingleProduct getProduct(){
        return product;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProductForm){
            ProductForm that = (ProductForm)obj;
            return  that.product.equals(this.product) &&
                    that.quantity==this.quantity &&
                    that.expiryDaysAfterOpening==this.expiryDaysAfterOpening &&
                    Objects.equals(that.expiryDate, this.expiryDate);
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        String productFormAsString = "[";
        productFormAsString += "product: " + product + ", ";
        productFormAsString += "quantity: " + quantity + ", ";
        productFormAsString += "expiryDaysAfterOpening: " + expiryDaysAfterOpening + ", ";
        productFormAsString += "expiryDate: " + expiryDate + "]";
        return productFormAsString;
    }
}
