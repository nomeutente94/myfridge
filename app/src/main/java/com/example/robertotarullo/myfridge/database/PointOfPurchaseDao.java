package com.example.robertotarullo.myfridge.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.robertotarullo.myfridge.bean.PointOfPurchase;

import java.util.List;

@Dao
public interface PointOfPurchaseDao {

    // Get all
    @Query("SELECT * FROM pointofpurchase")
    List<PointOfPurchase> getPointsOfPurchase();

    // Get single
    @Query("SELECT * FROM pointofpurchase where id = :id")
    PointOfPurchase getPointOfPurchase(long id);

    // Get count
    @Query("SELECT COUNT(*) from pointofpurchase")
    int getPointsOfPurchaseCount();
    // Insert all

    @Insert
    List<Long> insertAll(PointOfPurchase... pops);

    // Insert single
    @Insert
    long insertPointOfPurchase(PointOfPurchase pop);

    // Delete single
    @Delete
    void deletePointOfPurchase(PointOfPurchase pop);

    @Query("DELETE FROM pointofpurchase WHERE id = :id")
    void deletePointOfPurchaseById(long id);

}