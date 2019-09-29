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
    private Date consumptionDate;

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

    // ATTRIBUTI PROPRI DI UN PRODOTTO FRESCO
    // IL VALORE DI QUESTE VARIABILI VIENE DEDOTTO SE IL PRODOTTO E' CONFEZIONATO                   // Se prodotto confezionato si assumono sempre i seguenti valori:
    private Date packagingDate; // Indica la data in cui il prodotto fresco è stato confezionato    (= null)

    // ATTRIBUTI PROPRI DI UN PRODOTTO CONFEZIONATO
    // IL VALORE DI QUESTE VARIABILI VIENE DEDOTTO SE IL PRODOTTO E' FRESCO                         // Se prodotto fresco (NON confezionato) si assumono sempre i seguenti valori:
    private boolean opened; // Indica se il prodotto è stato aperto                                 (= true)
    private Date openingDate; // Data di apertura del prodotto                                      (= packagingDate, se null = purchaseDate)
    private int openedStorageCondition; // Modalità di conservazione a seguito dell'apertura        (= storageCondition)

    public SingleProduct(){}

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
        if(opened)
            return openedStorageCondition;
        return storageCondition;
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

    @Override
    public int getOpenedStorageCondition() {
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

    public void loseState(){
        setPurchaseDate(null);
        setPointOfPurchaseId(0); // TODO attributo proprio? Eliminare?
        setExpiryDate(null);
        setPackagingDate(null);
        setOpeningDate(null);

        loseConsumptionState();
    }

    public void loseConsumptionState(){
        setCurrentWeight(0);
        setPercentageQuantity(100);
        setCurrentPieces(getPieces());
        setConsumptionDate(null);
        setConsumed(false);
        if(packaged) {
            setOpened(false);
            setOpeningDate(null);
        }
    }

    // ritorna true se raggruppabile in un pack
    // TODO permettere di configurare il criterio di raggruppamento
    public boolean packEquals(SingleProduct singleProduct){
        if(singleProduct!=null){
            return     singleProduct.isPackaged() == packaged                                                               // packaged
                    && singleProduct.getActualStorageCondition() == this.getActualStorageCondition()                        // actualStorageCondition
                    && Objects.equals(singleProduct.getName(), name)                                                        // name
                    && Objects.equals(singleProduct.getBrand(), brand)                                                      // brand
                    && singleProduct.getWeight() == weight                                                                  // weight
                    && singleProduct.getPieces() == pieces                                                                  // pieces
                    && singleProduct.getStorageCondition() == storageCondition                                              // storageCondition
                    && singleProduct.getOpenedStorageCondition() == openedStorageCondition                                  // openedStorageCondition
                    && Objects.equals(singleProduct.getExpiryDate(), expiryDate)                                            // expiryDate
                    && singleProduct.getExpiringDaysAfterOpening() == expiringDaysAfterOpening                              // expiringDaysAfterOpening
                    && Objects.equals(singleProduct.getPackagingDate(), packagingDate)                                      // packagingDate
                    && Objects.equals(DateUtils.getActualExpiryDate(singleProduct), DateUtils.getActualExpiryDate(this));// actualExpiryDate
        }
        return false;
    }

    // ritorna true se raggruppabile in modalità PICK
    public boolean pickEquals(SingleProduct singleProductObj){
        if(singleProductObj!=null){
            boolean piecesCondition = singleProductObj.getPieces() == pieces;
            if(!packaged)
                piecesCondition = true;

            return  singleProductObj.isPackaged() == packaged &&
                    Objects.equals(singleProductObj.getName(), name) &&
                    Objects.equals(singleProductObj.getBrand(), brand) &&
                    singleProductObj.getPrice() == price &&
                    singleProductObj.getPricePerKilo() == pricePerKilo &&
                    singleProductObj.getWeight() == weight &&
                    piecesCondition && // due prodotti freschi uguali possono avere pezzi variabili
                    singleProductObj.getExpiringDaysAfterOpening() == expiringDaysAfterOpening &&
                    singleProductObj.getStorageCondition() == storageCondition &&
                    //singleProductObj.getPointOfPurchaseId() == pointOfPurchaseId
                    singleProductObj.getOpenedStorageCondition() == openedStorageCondition;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SingleProduct){
            SingleProduct singleProductObj = (SingleProduct)obj;
            return  singleProductObj.getId() == id &&
                    singleProductObj.isPackaged() == packaged &&
                    Objects.equals(singleProductObj.getName(), name) &&
                    Objects.equals(singleProductObj.getBrand(), brand) &&
                    singleProductObj.getPrice() == price &&
                    singleProductObj.getPricePerKilo() == pricePerKilo &&
                    singleProductObj.getWeight() == weight &&
                    singleProductObj.getCurrentWeight() == currentWeight &&
                    singleProductObj.getPercentageQuantity() == percentageQuantity &&
                    singleProductObj.getPieces() == pieces &&
                    singleProductObj.getCurrentPieces() == currentPieces &&
                    singleProductObj.getExpiringDaysAfterOpening() == expiringDaysAfterOpening &&
                    Objects.equals(singleProductObj.getPurchaseDate(), purchaseDate) &&
                    Objects.equals(singleProductObj.getConsumptionDate(), consumptionDate) &&
                    singleProductObj.getStorageCondition() == storageCondition &&
                    singleProductObj.getPointOfPurchaseId() == pointOfPurchaseId &&
                    singleProductObj.isConsumed() == consumed &&
                    Objects.equals(singleProductObj.getExpiryDate(), expiryDate) &&
                    Objects.equals(singleProductObj.getPackagingDate(), packagingDate) &&
                    singleProductObj.isOpened() == opened &&
                    Objects.equals(singleProductObj.getOpeningDate(), openingDate) &&
                    singleProductObj.getOpenedStorageCondition() == openedStorageCondition;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        String singleProductAsString = "[";
        singleProductAsString += "id: " + id + ", ";
        singleProductAsString += "packaged: " + packaged + ", ";
        singleProductAsString += "name: \"" + name + "\", "; // TODO mostrare virgolette solo se name != null
        singleProductAsString += "brand: \"" + brand + "\", "; // TODO mostrare virgolette solo se brand != null
        singleProductAsString += "price: " + price + ", ";
        singleProductAsString += "pricePerKilo: " + pricePerKilo + ", ";
        singleProductAsString += "weight: " + weight + ", ";
        singleProductAsString += "currentWeight: " + currentWeight + ", ";
        singleProductAsString += "percentageQuantity: " + percentageQuantity + ", ";
        singleProductAsString += "pieces: " + pieces + ", ";
        singleProductAsString += "currentPieces: " + currentPieces + ", ";
        singleProductAsString += "expiringDaysAfterOpening: " + expiringDaysAfterOpening + ", ";
        singleProductAsString += "purchaseDate: " + purchaseDate + ", ";
        singleProductAsString += "consumptionDate: " + consumptionDate + ", ";
        singleProductAsString += "storageCondition: " + storageCondition + ", ";
        singleProductAsString += "pointOfPurchaseId: " + pointOfPurchaseId + ", ";
        singleProductAsString += "consumed: " + consumed + ", ";
        singleProductAsString += "expiryDate: " + expiryDate + ", ";
        singleProductAsString += "packagingDate: " + packagingDate + ", ";
        singleProductAsString += "opened: " + opened + ", ";
        singleProductAsString += "openingDate: " + openingDate + ", ";
        singleProductAsString += "openedStorageCondition: " + openedStorageCondition + "]";
        return singleProductAsString;
    }
}
