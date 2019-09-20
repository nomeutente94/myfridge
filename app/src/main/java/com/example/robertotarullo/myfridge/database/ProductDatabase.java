package com.example.robertotarullo.myfridge.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.bean.SingleProduct;

@Database(entities = {SingleProduct.class, PointOfPurchase.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class ProductDatabase extends RoomDatabase {
    public abstract SingleProductDao productDao();
    public abstract PointOfPurchaseDao pointOfPurchaseDao();
}