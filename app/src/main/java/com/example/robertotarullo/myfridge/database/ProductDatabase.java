package com.example.robertotarullo.myfridge.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.bean.SingleProduct;

@Database(entities = {SingleProduct.class, PointOfPurchase.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class ProductDatabase extends RoomDatabase {
    public abstract SingleProductDao productDao();
    public abstract PointOfPurchaseDao pointOfPurchaseDao();
}