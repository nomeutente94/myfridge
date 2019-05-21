package com.example.robertotarullo.myfridge.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.example.robertotarullo.myfridge.Bean.Pack;
import com.example.robertotarullo.myfridge.Bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.Bean.SingleProduct;

@Database(entities = {SingleProduct.class, PointOfPurchase.class, Pack.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class ProductDatabase extends RoomDatabase {
    public abstract SingleProductDao productDao();
    public abstract PointOfPurchaseDao pointOfPurchaseDao();
}