package com.example.robertotarullo.myfridge.bean;

import java.util.ArrayList;
import java.util.List;

public class Pack implements Product{
    private List<SingleProduct> products;

    public Pack(){
        this.products = new ArrayList<>();
    }

    public Pack(List<SingleProduct> products){
        this.products = products;
    }

    public boolean isEmpty(){
        return products.size()==0;
    }

    public List<SingleProduct> getProducts() {
        return products;
    }

    public void setProducts(List<SingleProduct> products) {
        this.products = products;
    }

    public void addProduct(SingleProduct singleProduct){
        products.add(singleProduct);
    }

    public int getSize(){
        return getProducts().size();
    }

    @Override
    public String getName() {
        if(getSize()>0)
            return getProducts().get(0).getName();
        return null;
    }

    @Override
    public void setName(String name) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setName(name);
    }

    @Override
    public String getBrand() {
        if(getSize()>0)
            return getProducts().get(0).getBrand();
        return null;
    }

    @Override
    public void setBrand(String brand) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setBrand(brand);
    }

    @Override
    public float getWeight() {
        if(getSize()>0)
            return getProducts().get(0).getWeight();
        return 0;
    }

    @Override
    public void setWeight(float weight) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setWeight(weight);
    }

    /*
    @Override
    // Ritorna una media da tutti i prodotti, compresi quelli consumati
    public int getPercentageQuantity() {
        if(getSize()>0){
            int sum = 0;
            for(int i=0; i<getSize(); i++) {
                if(!getProducts().get(i).isConsumed())
                    sum += getProducts().get(i).getPercentageQuantity();
            }
            return (int) Math.ceil(sum/(float)getSize());
        } else
            return 0;
    }*/

    @Override
    // Ritorna true se tutti i pezzi sono stati consumati, false se almeno uno non è consumato
    public boolean isConsumed() {
        int consumed = 0;
        for(int i=0; i<getSize(); i++){
            if(getProducts().get(i).isConsumed())
                consumed++;
        }
        return consumed==getSize();
    }

    @Override
    // Ritorna true se tutti i pezzi sono stati consumati, false se almeno uno non è consumato
    public boolean isOpened() {
        int opened = 0;
        for(int i=0; i<getSize(); i++){
            if(getProducts().get(i).isOpened())
                opened++;
        }
        return opened==getSize();
    }

    @Override
    public void setConsumed(boolean consumed) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setConsumed(consumed);
    }

    @Override
    public int getPieces() {
        if(getSize()>0)
            return getProducts().get(0).getPieces();
        return 0;
    }

    @Override
    public void setPieces(int pieces) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setPieces(pieces);
    }

    @Override
    public int getActualStorageCondition(){
        if(getSize()>0 && isOpened())
            return getProducts().get(0).getOpenedStorageCondition();
        return getProducts().get(0).getStorageCondition();
    }

    @Override
    public int getStorageCondition() {
        if(getSize()>0)
            return getProducts().get(0).getStorageCondition();
        return 0;
    }

    @Override
    public void setStorageCondition(int storageCondition) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setStorageCondition(storageCondition);
    }

    @Override
    public int getOpenedStorageCondition() {
        if(getSize()>0)
            return getProducts().get(0).getOpenedStorageCondition();
        return 0;
    }

    @Override
    public void setOpenedStorageCondition(int openedStorageCondition) {
        for(int i=0; i<getSize(); i++)
            getProducts().get(i).setOpenedStorageCondition(openedStorageCondition);
    }

    @Override
    public boolean isPackaged() {
        for(int i=0; i<getSize(); i++){
            if(!getProducts().get(i).isPackaged())
                return false;
        }
        return true;
    }
}
