package com.example.robertotarullo.myfridge.Bean;

// Interfaccia per prodotti e gruppi di prodotti
public interface Product {
    boolean isPackaged();

    String getName();

    void setName(String name);

    String getBrand();

    void setBrand(String brand);

    float getWeight();

    void setWeight(float weight);

    int getPieces();

    void setPieces(int pieces);

    int getStorageCondition();

    void setStorageCondition(int storageCondition);

    int getOpenedStorageCondition();

    void setOpenedStorageCondition(int openedStorageCondition);

    int getPercentageQuantity();

    boolean isConsumed();

    void setConsumed(boolean consumed);
}
