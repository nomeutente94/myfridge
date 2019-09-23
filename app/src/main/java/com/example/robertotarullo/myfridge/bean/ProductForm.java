package com.example.robertotarullo.myfridge.bean;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

public class ProductForm {

    private SingleProduct formProduct;
    private int quantity, expiryDaysAfterOpening;
    private Date expiryDate;

    public ProductForm(SingleProduct formProduct, int quantity, Date expiryDate, int expiryDaysAfterOpening){
        this.formProduct = formProduct;
        this.quantity = quantity;
        this.expiryDaysAfterOpening = expiryDaysAfterOpening;
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProductForm){
            ProductForm that = (ProductForm)obj;
            return  that.formProduct.equals(this.formProduct) &&
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
        productFormAsString += "formProduct: " + formProduct + ", ";
        productFormAsString += "quantity: " + quantity + ", ";
        productFormAsString += "expiryDaysAfterOpening: " + expiryDaysAfterOpening + ", ";
        productFormAsString += "expiryDate: " + expiryDate + "]";
        return productFormAsString;
    }
}
