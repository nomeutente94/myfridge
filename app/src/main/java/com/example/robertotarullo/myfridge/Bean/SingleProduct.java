package com.example.robertotarullo.myfridge.Bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import com.example.robertotarullo.myfridge.Utils.DateUtils;

import java.util.Date;

@Entity/*(foreignKeys = {
        @ForeignKey(entity = PointOfPurchase.class,
                parentColumns = "id",
                childColumns = "pointOfPurchaseId",
                onDelete = ForeignKey.NO_ACTION
        ),
        @ForeignKey(entity = Pack.class,
                parentColumns = "id",
                childColumns = "packageId",
                onDelete = ForeignKey.NO_ACTION
        )})*/

public class SingleProduct implements Product {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private boolean packaged; // indica se si tratta di un prodotto confezionato o meno
    private String name; // nome del prodotto
    private String brand; // marca del prodotto
    private float price; // prezzo del prodotto per la quantità specificata
    private float pricePerKilo; // prezzo al kilo
    private float weight; // peso del prodotto al momento dell'acquisto
    private float currentWeight; // peso attuale del prodotto
    private int percentageQuantity; // indica la percentuale generale rimanente del prodotto
    private int pieces; // indica il numero totale di pezzi, 1 se non ve ne sono
    private int currentPieces; // indica i pezzi rimanenti
    private int expiringDaysAfterOpening; // giorni entro cui consumare il prodotto (da quando aperto nel caso di una confezione, dalla data di acquisto altrimenti)
    private Date purchaseDate; // data di acquisto del prodotto
    private int storageCondition; // indica la modalità di conservazione
    private long pointOfPurchaseId; // indica il punto di acquisto
    private long packageId; // id della confezione di appartenza, 0 se non esiste
    private boolean consumed; // indica se il prodotto è stato esaurito o meno

    // Attributi di prodotto confezionato                                                           Se prodotto fresco (NON confezionato):
    private boolean opened; // indica se il prodotto è stato aperto                                 (= true)
    private Date openingDate; // data di apertura del prodotto                                      (= purchaseDate)
    private Date expiryDate; // data di scadenza                                                    (= openingDate + expiringDaysAfterOpening)
    private int openedStorageCondition; // Modalità di conservazione dopo l'apertura                (= storageCondition)

    public SingleProduct(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(float currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public int getStorageCondition() {
        return storageCondition;
    }

    public void setStorageCondition(int storageCondition) {
        this.storageCondition = storageCondition;
    }

    public long getPointOfPurchaseId() {
        return pointOfPurchaseId;
    }

    public void setPointOfPurchaseId(long pointOfPurchaseId) {
        this.pointOfPurchaseId = pointOfPurchaseId;
    }

    public int getActualStorageCondition(){
        if(opened)
            return openedStorageCondition;
        return storageCondition;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getCurrentPieces() {
        return currentPieces;
    }

    public void setCurrentPieces(int currentPieces) {
        this.currentPieces = currentPieces;
    }

    public int getPercentageQuantity() {
        return percentageQuantity;
    }

    public void setPercentageQuantity(int percentageQuantity) {
        this.percentageQuantity = percentageQuantity;
    }

    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    @Override
    public float getPricePerKilo() {
        return pricePerKilo;
    }

    @Override
    public void setPricePerKilo(float pricePerKilo) {
        this.pricePerKilo = pricePerKilo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }

    public int getOpenedStorageCondition() {
        return openedStorageCondition;
    }

    public void setOpenedStorageCondition(int openedStorageCondition) {
        this.openedStorageCondition = openedStorageCondition;
    }

    public int getExpiringDaysAfterOpening() {
        return expiringDaysAfterOpening;
    }

    public void setExpiringDaysAfterOpening(int expiringDaysAfterOpening) {
        this.expiringDaysAfterOpening = expiringDaysAfterOpening;
    }

    public Date getActualExpiringDate(){
        if(packaged && opened && openingDate!=null && expiringDaysAfterOpening>0)
            return DateUtils.getDateByAddingDays(openingDate, expiringDaysAfterOpening);
        return expiryDate;
    }

    public boolean isPackaged() {
        return packaged;
    }

    public void setPackaged(boolean packaged) {
        this.packaged = packaged;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SingleProduct){
            SingleProduct singleProductObj = (SingleProduct)obj;

            System.out.println(singleProductObj.getName() + " : " + name);
            System.out.println(singleProductObj.getStorageCondition() + " : " + storageCondition);
            System.out.println(singleProductObj.getOpenedStorageCondition() + " : " + openedStorageCondition);

            if(     singleProductObj.getId()==id &&
                    singleProductObj.isPackaged()==packaged &&
                    (singleProductObj.getName()==name || singleProductObj.getName().equals(name)) &&
                    (singleProductObj.getBrand()==brand || singleProductObj.getBrand().equals(brand)) &&
                    singleProductObj.getPrice()==price &&
                    singleProductObj.getPricePerKilo()==pricePerKilo &&
                    singleProductObj.getWeight()==weight &&
                    singleProductObj.getCurrentWeight()==currentWeight &&
                    singleProductObj.getPercentageQuantity()==percentageQuantity &&
                    singleProductObj.getPieces()==pieces &&
                    singleProductObj.getCurrentPieces()==currentPieces &&
                    singleProductObj.getExpiringDaysAfterOpening()==expiringDaysAfterOpening &&
                    (singleProductObj.getPurchaseDate()==purchaseDate || singleProductObj.getPurchaseDate().equals(purchaseDate)) &&
                    singleProductObj.getStorageCondition()==storageCondition &&
                    singleProductObj.getPointOfPurchaseId()==pointOfPurchaseId &&
                    singleProductObj.getPackageId()==packageId &&
                    singleProductObj.isConsumed()==consumed &&
                    singleProductObj.isOpened()==opened &&
                    (singleProductObj.getOpeningDate()==openingDate || singleProductObj.getOpeningDate().equals(openingDate)) &&
                    (singleProductObj.getExpiryDate()==expiryDate || singleProductObj.getExpiryDate().equals(expiryDate)) &&
                    singleProductObj.getOpenedStorageCondition()==openedStorageCondition)
                return true;
        }
        return false;
    }
}
