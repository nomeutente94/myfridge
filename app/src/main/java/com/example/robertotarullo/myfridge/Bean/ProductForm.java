package com.example.robertotarullo.myfridge.Bean;

public class ProductForm {

    private SingleProduct formProduct;
    private int quantity;

    public ProductForm(SingleProduct formProduct, int quantity){
        this.formProduct = formProduct;
        this.quantity = quantity;
    }

    public SingleProduct getFormProduct() {
        return formProduct;
    }

    public void setFormProduct(SingleProduct formProduct) {
        this.formProduct = formProduct;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProductForm){
            ProductForm that = (ProductForm)obj;
            return that.getFormProduct().equals(this.formProduct) && that.getQuantity()==quantity;
        }

        return false;
    }
}
