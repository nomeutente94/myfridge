package com.example.robertotarullo.myfridge.Bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.example.robertotarullo.myfridge.Utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Pack implements Product{
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    @Ignore
    private List<SingleProduct> products;

    public List<SingleProduct> getProducts() {
        return products;
    }

    public void setProducts(List<SingleProduct> products) {
        this.products = products;
    }

    public void addProduct(SingleProduct singleProduct){
        if(products==null)
            products = new ArrayList<>();
        products.add(singleProduct);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getBrand() {
        if(products.size()>0)
            return products.get(0).getBrand();
        return null;
    }

    @Override
    public void setBrand(String brand) {
        for(int i=0; i<products.size(); i++)
            products.get(i).setBrand(brand);
    }

    @Override
    public float getPrice() {
        if(products.size()>0)
            return products.get(0).getPrice();
        return 0;
    }

    @Override
    public void setPrice(float price) {
        for(int i=0; i<products.size(); i++)
            products.get(i).setPrice(price);
    }

    @Override
    public void setPricePerKilo(float price) {

    }

    @Override
    public float getPricePerKilo() {
        return 0;
    }

    @Override
    public Date getExpiryDate() {
        if(products.size()>0)
            return products.get(0).getExpiryDate();
        return null;
    }

    @Override
    public void setExpiryDate(Date expiryDate) {
        for(int i=0; i<products.size(); i++)
            products.get(0).setExpiryDate(expiryDate);
    }

    @Override
    public float getWeight() {
        if(products.size()>0)
            return products.get(0).getWeight();
        return 0;
    }

    @Override
    public void setWeight(float weight) {

    }

    @Override
    public float getCurrentWeight() {
        if(products.size()>0)
            return products.get(0).getCurrentWeight();
        return 0;
    }

    @Override
    public void setCurrentWeight(float currentWeight) {
        // ??
    }

    @Override
    public Date getPurchaseDate() {
        if(products.size()>0)
            return products.get(0).getExpiryDate();
        return null;
    }

    @Override
    public void setPurchaseDate(Date purchaseDate) {
        for(int i=0; i<products.size(); i++)
            products.get(i).setPurchaseDate(purchaseDate);
    }

    public boolean belongsToStorageCondition(int storageCondition){
        for(int i=0; i<products.size(); i++){
            if(products.get(i).getStorageCondition()==storageCondition)
                return true;
        }
        return false;
    }

    public int[] getStorageConditions() {
        int[] storageConditions = new int[products.size()];
        for(int i=0; i<storageConditions.length; i++)
            storageConditions[i] = products.get(i).getStorageCondition();

        return storageConditions;
    }

    @Override
    public long getPointOfPurchaseId() {
        if(products.size()>0)
            return products.get(0).getPointOfPurchaseId();
        return 0;
    }

    @Override
    public void setPointOfPurchaseId(long pointOfPurchaseId) {

    }

    @Override
    public int getPercentageQuantity() {
        if(products.size()>0){
            int sum = 0;
            for(int i=0; i<products.size(); i++) {
                if(!products.get(i).isConsumed())
                    sum += products.get(i).getPercentageQuantity();
            }
            return (int) Math.ceil(sum/(float)products.size());
        } else
            return 0;
    }

    @Override
    public void setPercentageQuantity(int percentageQuantity) {

    }

    @Override
    // Ritorna true se tutti i pezzi sono stati consumati, false se almeno uno non è consumato
    public boolean isConsumed() {
        int consumed = 0;
        for(int i=0; i<products.size(); i++){
            if(products.get(i).isConsumed())
                consumed++;
        }
        return consumed==products.size();
    }

    @Override
    public void setConsumed(boolean consumed) {
        for(int i=0; i<products.size(); i++)
            products.get(i).setConsumed(consumed);
    }

    @Override
    public long getPackageId() {
        return id;
    }

    @Override
    public void setPackageId(long packageId) {
        this.id = id;
    }

    @Override
    public int getPieces() {
        return products.size();
    }

    @Override
    public void setPieces(int pieces) {

    }

    // Ritorna il numero di pezzi ancora non consumati
    public int getCurrentPieces() {
        int pieces = 0;
        for(int i=0; i<products.size(); i++){
            if(!products.get(i).isConsumed())
                pieces++;
        }
        return pieces;
    }

    public void setCurrentPieces(int pieces) {

    }

    @Override
    public boolean isPackaged() {
        for(int i=0; i<products.size(); i++){
            if(!products.get(i).isPackaged())
                return false;
        }
        return true;
    }

    @Override
    public void setPackaged(boolean packaged) {
        for(int i=0; i<products.size(); i++){
            products.get(i).setPackaged(packaged);
            if(!packaged){
                products.get(i).setOpened(true);
                products.get(i).setOpeningDate(products.get(i).getPurchaseDate());
                products.get(i).setExpiryDate(DateUtils.getDateByAddingDays(products.get(i).getOpeningDate(), products.get(i).getExpiringDaysAfterOpening()));
                products.get(i).setOpenedStorageCondition(products.get(i).getStorageCondition());
            }
        }
    }
}
