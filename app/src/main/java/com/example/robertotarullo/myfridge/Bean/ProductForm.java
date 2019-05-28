package com.example.robertotarullo.myfridge.Bean;

public class ProductForm {

    private SingleProduct formProduct;
    private int quantity;
    private boolean visibleExpiryDate; // TODO settare invece che se true se c'è qualche differenza in ENTRAMBI I CAMPI

    public ProductForm(SingleProduct formProduct, int quantity, boolean visibleExpiryDate){
        this.formProduct = formProduct;
        this.quantity = quantity;
        this.visibleExpiryDate = visibleExpiryDate;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProductForm){
            ProductForm that = (ProductForm)obj;
            return that.formProduct.equals(this.formProduct) && that.quantity==quantity && that.visibleExpiryDate==visibleExpiryDate;
        }

        return false;
    }
}
