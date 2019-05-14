package com.example.robertotarullo.myfridge.Bean;

import java.util.Date;

public interface Product {
    public String getName();

    public void setName(String name);

    public String getBrand();

    public void setBrand(String brand);

    public float getPrice();

    public void setPrice(float price);

    public void setPricePerKilo(float price);

    public float getPricePerKilo();

    public Date getExpiryDate();

    public void setExpiryDate(Date expiryDate);

    public float getWeight();

    public void setWeight(float weight);

    public float getCurrentWeight();

    public void setCurrentWeight(float currentWeight);

    public Date getPurchaseDate();

    public void setPurchaseDate(Date purchaseDate);

    public long getPointOfPurchaseId();

    public void setPointOfPurchaseId(long pointOfPurchaseId);

    public int getPercentageQuantity();

    public void setPercentageQuantity(int percentageQuantity);

    public boolean isConsumed();

    public void setConsumed(boolean consumed);

    public long getPackageId();

    public void setPackageId(long packageId);

    public int getPieces();

    public void setPieces(int pieces);

    public boolean isPackaged();

    public void setPackaged(boolean packaged);
}
