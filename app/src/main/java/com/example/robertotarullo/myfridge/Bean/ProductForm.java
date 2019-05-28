package com.example.robertotarullo.myfridge.Bean;

import java.util.Date;

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
                    (that.expiryDate==this.expiryDate || (that.expiryDate!=null && that.expiryDate.equals(this.expiryDate)));
        }

        return false;
    }
}
