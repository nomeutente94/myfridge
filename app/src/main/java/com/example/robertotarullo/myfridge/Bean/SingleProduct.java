package com.example.robertotarullo.myfridge.Bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.example.robertotarullo.myfridge.Utils.DateUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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

public class SingleProduct implements Product, Serializable {
    // Identificatore univoco del prodotto
    // Se non specificato è 0, parte da id 1
    @PrimaryKey(autoGenerate = true)
    private long id;

    // Indica se si tratta di un prodotto confezionato
    private boolean packaged;

    // Nome del prodotto
    private String name;

    // Marca del prodotto
    private String brand;

    // Prezzo del prodotto per la quantità totale specificata
    private float price;

    // Prezzo al chilo
    private float pricePerKilo;

    // Peso totale del prodotto al momento dell'acquisto
    private float weight;

    // Peso corrente del prodotto
    private float currentWeight;

    // Indica la percentuale generale attuale rimanente del prodotto, non specificato se 0
    private int percentageQuantity;

    // Indica il numero totale di pezzi, 1 se non ve n'è più di uno, 0 se non specificato
    private int pieces;

    // Indica il numero di pezzi rimanenti su quelli totali
    private int currentPieces;

    // Giorni entro cui consumare il prodotto (da quando aperto nel caso di una confezione, dalla data di acquisto altrimenti)
    // Dovrebbe essere 0 se expiryDate!=null (e viceversa)
    private int expiringDaysAfterOpening;

    // Data in cui il prodotto è stato acquistato
    private Date purchaseDate;

    // Data in cui il prodotto è stato consumato
    // Un prodotto può essere consumato senza aver precisato la data di consumazione
    private Date consumingDate;

    // Indica la modalità di conservazione nello stato in cui lo si è comprato
    // Può assumere valori tra 0, 1, 2, rispettivamente per Dispensa, Frigorifero, Congelatore
    private int storageCondition;

    // Indica l'id del punto di acquisto
    private long pointOfPurchaseId;

    // Indica se il prodotto è stato esaurito o meno, diverso da percentageQuantity=0
    private boolean consumed;

    // Data di scadenza indicata sulla confezione del prodotto
    // Per prodotto fresco si può calcolare: (= openingDate/purchaseDate + expiringDaysAfterOpening)
    // Dovrebbe essere null se expiringDaysAfterOpening>0 (e viceversa)
    private Date expiryDate;

    // Indica la data in cui il prodotto è stato confezionato
    private Date productionDate;

    // ATTRIBUTI PROPRI DI UN PRODOTTO CONFEZIONATO
    // IL VALORE DI QUESTE VARIABILI VIENE DEDOTTO SE IL PRODOTTO E' FRESCO                         // Se prodotto fresco (NON confezionato) si assumono sempre i seguenti valori:
    private boolean opened; // Indica se il prodotto è stato aperto                                 (= true)
    private Date openingDate; // Data di apertura del prodotto                                      (= productionDate, se null = purchaseDate)
    private int openedStorageCondition; // Modalità di conservazione a seguito dell'apertura        (= storageCondition)

    public SingleProduct(){}

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

    // TODO spostare in classe Utils esterna
    public Date calculateExpiryDate() {
        if(getExpiryDate()==null && getOpeningDate()!=null && getExpiringDaysAfterOpening()>0 && !isPackaged()) // se non lo ha ed è un prodotto fresco, prova a calcolarlo
            return DateUtils.getDateByAddingDays(getOpeningDate(), getExpiringDaysAfterOpening());
        else
            return getExpiryDate();
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

    // TODO spostare in classe Utils esterna
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

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public float getPricePerKilo() {
        return pricePerKilo;
    }

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

    // TODO spostare in classe Utils esterna
    public int calculateExpiringDaysAfterOpening(){
        if(getExpiringDaysAfterOpening()==0 && getOpeningDate()!=null && getExpiryDate()!=null && !isPackaged()) // se non lo ha ed è un prodotto fresco, prova a calcolarlo
            return DateUtils.getDaysByDateDiff(getOpeningDate(), getExpiryDate());
        else
            return getExpiringDaysAfterOpening();
    }

    public int getExpiringDaysAfterOpening() {
        return expiringDaysAfterOpening;
    }

    public void setExpiringDaysAfterOpening(int expiringDaysAfterOpening) {
        this.expiringDaysAfterOpening = expiringDaysAfterOpening;
    }

    public boolean isPackaged() {
        return packaged;
    }

    public void setPackaged(boolean packaged) {
        this.packaged = packaged;
    }

    // ritorna true se raggruppabile
    // TODO permettere di configurare il criterio di raggruppamento
    public boolean packEquals(SingleProduct singleProduct){
        if(singleProduct!=null){
            if( singleProduct.isPackaged()==packaged &&                                 // packaged
                Objects.equals(singleProduct.getName(), name) &&                        // name
                Objects.equals(singleProduct.getBrand(), brand) &&                      // brand
                singleProduct.getWeight()==weight &&                                    // weight
                singleProduct.getPieces()==pieces &&                                    // pieces
                singleProduct.getStorageCondition()==storageCondition &&                // storageCondition
                singleProduct.getOpenedStorageCondition()==openedStorageCondition &&    // openedStorageCondition
                Objects.equals(singleProduct.getExpiryDate(), expiryDate) &&            // expiryDate
                singleProduct.getExpiringDaysAfterOpening()==expiringDaysAfterOpening   // expiringDaysAfterOpening
            ){
                return true;
            }
        } else
            return false;

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SingleProduct){
            SingleProduct singleProductObj = (SingleProduct)obj;
            if( singleProductObj.getId()==id &&
                singleProductObj.isPackaged()==packaged &&
                Objects.equals(singleProductObj.getName(), name) &&
                Objects.equals(singleProductObj.getBrand(), brand) &&
                singleProductObj.getPrice()==price &&
                singleProductObj.getPricePerKilo()==pricePerKilo &&
                singleProductObj.getWeight()==weight &&
                singleProductObj.getCurrentWeight()==currentWeight &&
                singleProductObj.getPercentageQuantity()==percentageQuantity &&
                singleProductObj.getPieces()==pieces &&
                singleProductObj.getCurrentPieces()==currentPieces &&
                singleProductObj.getExpiringDaysAfterOpening()==expiringDaysAfterOpening &&
                Objects.equals(singleProductObj.getPurchaseDate(), purchaseDate) &&
                Objects.equals(singleProductObj.getConsumingDate(), consumingDate) &&
                singleProductObj.getStorageCondition()==storageCondition &&
                singleProductObj.getPointOfPurchaseId()==pointOfPurchaseId &&
                singleProductObj.isConsumed()==consumed &&
                Objects.equals(singleProductObj.getExpiryDate(), expiryDate) &&
                Objects.equals(singleProductObj.getProductionDate(), productionDate) &&
                singleProductObj.isOpened()==opened &&
                Objects.equals(singleProductObj.getOpeningDate(), openingDate) &&
                singleProductObj.getOpenedStorageCondition()==openedStorageCondition
            ){
                return true;
            }
        }
        return false;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getConsumingDate() {
        return consumingDate;
    }

    public void setConsumingDate(Date consumingDate) {
        this.consumingDate = consumingDate;
    }
}
