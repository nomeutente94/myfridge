package com.example.robertotarullo.myfridge.Bean;

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

    @Override
    public String getName() {
        if(getProducts().size()>0)
            return getProducts().get(0).getName();
        return null;
    }

    @Override
    public void setName(String name) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setName(name);
    }

    @Override
    public String getBrand() {
        if(getProducts().size()>0)
            return getProducts().get(0).getBrand();
        return null;
    }

    @Override
    public void setBrand(String brand) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setBrand(brand);
    }

    @Override
    public float getWeight() {
        if(getProducts().size()>0)
            return getProducts().get(0).getWeight();
        return 0;
    }

    @Override
    public void setWeight(float weight) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setWeight(weight);
    }

    @Override
    // Ritorna una media da tutti i prodotti
    public int getPercentageQuantity() {
        if(getProducts().size()>0){
            int sum = 0;
            for(int i=0; i<getProducts().size(); i++) {
                if(!getProducts().get(i).isConsumed())
                    sum += getProducts().get(i).getPercentageQuantity();
            }
            return (int) Math.ceil(sum/(float)getProducts().size());
        } else
            return 0;
    }

    @Override
    // Ritorna true se tutti i pezzi sono stati consumati, false se almeno uno non è consumato
    public boolean isConsumed() {
        int consumed = 0;
        for(int i=0; i<getProducts().size(); i++){
            if(getProducts().get(i).isConsumed())
                consumed++;
        }
        return consumed==getProducts().size();
    }

    @Override
    public void setConsumed(boolean consumed) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setConsumed(consumed);
    }

    @Override
    public int getPieces() {
        if(getProducts().size()>0)
            return getProducts().get(0).getPieces();
        return 0;
    }

    @Override
    public void setPieces(int pieces) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setPieces(pieces);
    }

    @Override
    public int getStorageCondition() {
        if(getProducts().size()>0)
            return getProducts().get(0).getStorageCondition();
        return 0;
    }

    @Override
    public void setStorageCondition(int storageCondition) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setStorageCondition(storageCondition);
    }

    @Override
    public int getOpenedStorageCondition() {
        if(getProducts().size()>0)
            return getProducts().get(0).getOpenedStorageCondition();
        return 0;
    }

    @Override
    public void setOpenedStorageCondition(int openedStorageCondition) {
        for(int i=0; i<getProducts().size(); i++)
            getProducts().get(i).setOpenedStorageCondition(openedStorageCondition);
    }

    @Override
    public boolean isPackaged() {
        for(int i=0; i<getProducts().size(); i++){
            if(!getProducts().get(i).isPackaged())
                return false;
        }
        return true;
    }
}
