package com.example.robertotarullo.myfridge.Bean;

public class ProductForm {

    private SingleProduct formProduct;
    private String packName;
    private int quantity;

    public ProductForm(SingleProduct formProduct, String packName, int quantity){
        this.formProduct = formProduct;
        this.packName = packName;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ProductForm){
            ProductForm that = (ProductForm)obj;
            return that.getFormProduct().equals(this.formProduct) && that.getPackName().equals(packName) && that.getQuantity()==quantity;
        }

        return false;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }
}
