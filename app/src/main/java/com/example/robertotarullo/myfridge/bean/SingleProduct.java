package com.example.robertotarullo.myfridge.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.robertotarullo.myfridge.utils.DateUtils;

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

    // Variaibili fisse
    public static final int DEFAULT_PIECES = 1;
    public static final float DEFAULT_PERCENTAGEQUANTITY = 100;

    // Identificatore univoco del prodotto
    // 0 se inserito, >1 se inserito nel database
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

    // Indica la percentuale generale attuale rimanente del prodotto (default 100)
    private float percentageQuantity;

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
    private Date consumptionDate;

    // Indica la modalità di conservazione nello stato in cui lo si è comprato
    // Può assumere valori tra 0, 1, 2, rispettivamente per Dispensa, Frigorifero, Congelatore
    private int storageCondition;

    // Indica l'id del punto di acquisto
    private long pointOfPurchaseId;

    // Indica se il contenuto del prodotto è stato esaurito o meno
    private boolean consumed;

    // Data di scadenza indicata sulla confezione del prodotto
    // Per prodotto fresco si può calcolare: (= openingDate/purchaseDate + expiringDaysAfterOpening)
    // Dovrebbe essere null se expiringDaysAfterOpening>0 (e viceversa)
    private Date expiryDate;

    private Date insertDate; // Data di inserimento nel db

    // ATTRIBUTI ESCLUSIVI DI UN PRODOTTO NON CONFEZIONATO
    // IL VALORE DI QUESTE VARIABILI VIENE DEDOTTO SE IL PRODOTTO È CONFEZIONATO                    // Se prodotto confezionato si assumono sempre i seguenti valori:
    private Date packagingDate; // Indica la data in cui il prodotto fresco è stato confezionato    (= null)

    // ATTRIBUTI PROPRI DI UN PRODOTTO CONFEZIONATO
    // IL VALORE DI QUESTE VARIABILI VIENE DEDOTTO SE IL PRODOTTO E' FRESCO                         // Se prodotto fresco (NON confezionato) si assumono sempre i seguenti valori:
    private boolean opened; // Indica se il prodotto è stato aperto                                 (= true)
    private Date openingDate; // Data di apertura del prodotto                                      (= packagingDate, se null = purchaseDate)
    private int openedStorageCondition; // Modalità di conservazione a seguito dell'apertura        (= storageCondition)

    public SingleProduct(){
        this.pieces = DEFAULT_PIECES;
        this.percentageQuantity = DEFAULT_PERCENTAGEQUANTITY;
    }

    public SingleProduct(SingleProduct other){
        this.id = other.id;
        this.packaged = other.packaged;
        this.name = other.name;
        this.brand = other.brand;
        this.price = other.price;
        this.pricePerKilo = other.pricePerKilo;
        this.weight = other.weight;
        this.currentWeight = other.currentWeight;
        this.percentageQuantity = other.percentageQuantity;
        this.pieces = other.pieces;
        this.currentPieces = other.currentPieces;
        this.expiringDaysAfterOpening = other.expiringDaysAfterOpening;
        this.purchaseDate = other.purchaseDate;
        this.consumptionDate = other.consumptionDate;
        this.storageCondition = other.storageCondition;
        this.pointOfPurchaseId = other.pointOfPurchaseId;
        this.consumed = other.consumed;
        this.expiryDate = other.expiryDate;
        this.packagingDate = other.packagingDate;
        this.opened = other.opened;
        this.openingDate = other.openingDate;
        this.openedStorageCondition = other.openedStorageCondition;
        this.insertDate = other.insertDate;
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
        return brand;
    }

    @Override
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

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getCurrentWeight() {
        if(!isOpened())
            return getWeight();
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

    @Override
    public int getStorageCondition() {
        return storageCondition;
    }

    @Override
    public void setStorageCondition(int storageCondition) {
        this.storageCondition = storageCondition;
    }

    public long getPointOfPurchaseId() {
        return pointOfPurchaseId;
    }

    public void setPointOfPurchaseId(long pointOfPurchaseId) {
        this.pointOfPurchaseId = pointOfPurchaseId;
    }

    @Override
    public int getActualStorageCondition(){
        if(isOpened())
            return getOpenedStorageCondition();
        return getStorageCondition();
    }

    @Override
    public int getPieces() {
        return pieces;
    }

    @Override
    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getCurrentPieces() {
        if(!isOpened())
            return getPieces();
        return currentPieces;
    }

    public void setCurrentPieces(int currentPieces) {
        this.currentPieces = currentPieces;
    }

    public float getPercentageQuantity() {
        if(!isOpened())
            return DEFAULT_PERCENTAGEQUANTITY;
        return percentageQuantity;
    }

    public void setPercentageQuantity(float percentageQuantity) {
        this.percentageQuantity = percentageQuantity;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
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

    @Override
    public boolean isOpened() {
        if(!isPackaged())
            return true;
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public Date getOpeningDate() {
        if(!isPackaged()){
            if(getPackagingDate()!=null)
                return getPackagingDate();
            else return getPurchaseDate();
        }
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }

    @Override
    public int getOpenedStorageCondition() {
        if(!isPackaged())
            return getStorageCondition();
        return openedStorageCondition;
    }

    @Override
    public void setOpenedStorageCondition(int openedStorageCondition) {
        this.openedStorageCondition = openedStorageCondition;
    }

    public int getExpiringDaysAfterOpening() {
        return expiringDaysAfterOpening;
    }

    public void setExpiringDaysAfterOpening(int expiringDaysAfterOpening) {
        this.expiringDaysAfterOpening = expiringDaysAfterOpening;
    }

    @Override
    public boolean isPackaged() {
        return packaged;
    }

    public void setPackaged(boolean packaged) {
        this.packaged = packaged;
    }

    public Date getPackagingDate() {
        return packagingDate;
    }

    public void setPackagingDate(Date packagingDate) {
        this.packagingDate = packagingDate;
    }

    public Date getConsumptionDate() {
        return consumptionDate;
    }

    public void setConsumptionDate(Date consumptionDate) {
        this.consumptionDate = consumptionDate;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public void loseState(){
        setPurchaseDate(null);
        setPointOfPurchaseId(0); // TODO attributo proprio? Eliminare?
        if(getExpiryDate()!=null && !getExpiryDate().equals(DateUtils.getNoExpiryDate()))
            setExpiryDate(null);
        setPackagingDate(null);
        setOpeningDate(null);

        loseConsumptionState();
    }

    public void loseConsumptionState(){
        setCurrentWeight(0);
        setPercentageQuantity(DEFAULT_PERCENTAGEQUANTITY);
        setCurrentPieces(getPieces());
        setConsumptionDate(null);
        setConsumed(false);
        if(isPackaged()) {
            setOpened(false);
            setOpeningDate(null);
        }
    }

    // ritorna true se raggruppabile in un pack
    // TODO permettere di configurare il criterio di raggruppamento
    public boolean packEquals(SingleProduct singleProduct){
        if(singleProduct != null){
            return     singleProduct.isPackaged() == isPackaged()
                    && singleProduct.getActualStorageCondition() == getActualStorageCondition()
                    && Objects.equals(singleProduct.getName(), getName())
                    && Objects.equals(singleProduct.getBrand(), getBrand())
                    && singleProduct.getPrice() == getPrice()
                    && singleProduct.getPricePerKilo() == getPricePerKilo()
                    && singleProduct.getWeight() == getWeight()
                    && singleProduct.getPieces() == getPieces()
                    && singleProduct.getStorageCondition() == getStorageCondition()
                    && singleProduct.getOpenedStorageCondition() == getOpenedStorageCondition()
                    && Objects.equals(singleProduct.getExpiryDate(), getExpiryDate())
                    && singleProduct.getExpiringDaysAfterOpening() == getExpiringDaysAfterOpening()
                    && Objects.equals(singleProduct.getPackagingDate(), getPackagingDate())
                    && Objects.equals(DateUtils.getActualExpiryDate(singleProduct), DateUtils.getActualExpiryDate(this));
        }
        return false;
    }

    // ritorna true se raggruppabile in modalità PICK
    public boolean pickEquals(SingleProduct singleProductObj){
        if(singleProductObj!=null){
            return  singleProductObj.isPackaged() == isPackaged() &&
                    Objects.equals(singleProductObj.getName(), getName()) &&
                    Objects.equals(singleProductObj.getBrand(), getBrand()) &&
                    singleProductObj.getPrice() == getPrice() &&
                    singleProductObj.getPricePerKilo() == getPricePerKilo() &&
                    singleProductObj.getWeight() == getWeight() &&
                    singleProductObj.getPieces() == getPieces() && // due prodotti freschi uguali possono avere pezzi variabili
                    singleProductObj.getExpiringDaysAfterOpening() == getExpiringDaysAfterOpening() &&
                    singleProductObj.getStorageCondition() == getStorageCondition() &&
                    //singleProductObj.getPointOfPurchaseId() == getPointOfPurchaseId
                    singleProductObj.getOpenedStorageCondition() == getOpenedStorageCondition();
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SingleProduct){
            SingleProduct singleProductObj = (SingleProduct)obj;
            return  singleProductObj.getId() == getId() &&
                    singleProductObj.isPackaged() == isPackaged() &&
                    Objects.equals(singleProductObj.getName(), getName()) &&
                    Objects.equals(singleProductObj.getBrand(), getBrand()) &&
                    singleProductObj.getPrice() == getPrice() &&
                    singleProductObj.getPricePerKilo() == getPricePerKilo() &&
                    singleProductObj.getWeight() == getWeight() &&
                    singleProductObj.getCurrentWeight() == getCurrentWeight() &&
                    singleProductObj.getPercentageQuantity() == getPercentageQuantity() &&
                    singleProductObj.getPieces() == getPieces() &&
                    singleProductObj.getCurrentPieces() == getCurrentPieces() &&
                    singleProductObj.getExpiringDaysAfterOpening() == getExpiringDaysAfterOpening() &&
                    Objects.equals(singleProductObj.getPurchaseDate(), getPurchaseDate()) &&
                    Objects.equals(singleProductObj.getConsumptionDate(), getConsumptionDate()) &&
                    singleProductObj.getStorageCondition() == getStorageCondition() &&
                    singleProductObj.getPointOfPurchaseId() == getPointOfPurchaseId() &&
                    singleProductObj.isConsumed() == isConsumed() &&
                    Objects.equals(singleProductObj.getExpiryDate(), getExpiryDate()) &&
                    Objects.equals(singleProductObj.getPackagingDate(), getPackagingDate()) &&
                    singleProductObj.isOpened() == isOpened() &&
                    Objects.equals(singleProductObj.getOpeningDate(), getOpeningDate()) &&
                    Objects.equals(singleProductObj.getInsertDate(), getInsertDate()) &&
                    singleProductObj.getOpenedStorageCondition() == getOpenedStorageCondition();
        }
        return false;
    }

    @NonNull
    @Override
    // TODO mostrare 0 come unset e -1 come 0
    public String toString() {
        String singleProductAsString = "[";
        singleProductAsString += "id: " + getId() + ", ";
        singleProductAsString += "packaged: " + isPackaged() + ", ";
        singleProductAsString += "name: \"" + getName() + "\", "; // TODO mostrare virgolette solo se name != null
        singleProductAsString += "brand: \"" + getBrand() + "\", "; // TODO mostrare virgolette solo se brand != null
        singleProductAsString += "price: " + getPrice() + ", ";
        singleProductAsString += "pricePerKilo: " + getPricePerKilo() + ", ";
        singleProductAsString += "weight: " + getWeight() + ", ";
        singleProductAsString += "currentWeight: " + getCurrentWeight() + ", ";
        singleProductAsString += "percentageQuantity: " + getPercentageQuantity() + ", ";
        singleProductAsString += "pieces: " + getPieces() + ", ";
        singleProductAsString += "currentPieces: " + getCurrentPieces() + ", ";
        singleProductAsString += "expiringDaysAfterOpening: " + getExpiringDaysAfterOpening() + ", ";
        singleProductAsString += "purchaseDate: " + getPurchaseDate() + ", ";
        singleProductAsString += "consumptionDate: " + getConsumptionDate() + ", ";
        singleProductAsString += "storageCondition: " + getStorageCondition() + ", ";
        singleProductAsString += "pointOfPurchaseId: " + getPointOfPurchaseId() + ", ";
        singleProductAsString += "consumed: " + isConsumed() + ", ";
        singleProductAsString += "expiryDate: " + getExpiryDate() + ", ";
        singleProductAsString += "packagingDate: " + getPackagingDate() + ", ";
        singleProductAsString += "opened: " + isOpened() + ", ";
        singleProductAsString += "openingDate: " + getOpeningDate() + ", ";
        singleProductAsString += "insertDate: " + getInsertDate() + ", ";
        singleProductAsString += "openedStorageCondition: " + getOpenedStorageCondition() + "]";
        return singleProductAsString;
    }
}
