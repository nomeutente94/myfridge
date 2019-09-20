package com.example.robertotarullo.myfridge.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.robertotarullo.myfridge.bean.SingleProduct;

import java.util.List;

@Dao
public interface SingleProductDao {

    // Get all
    @Query("SELECT * FROM SingleProduct")
    List<SingleProduct> getAll();

    // Get single
    @Query("SELECT * FROM SingleProduct where id = :id")
    SingleProduct get(long id);

    // Get count
    @Query("SELECT COUNT(*) from SingleProduct")
    int getCount();

    // Insert all
    @Insert
    List<Long> insertAll(SingleProduct... fps);

    // Insert list
    @Insert
    List<Long> insertAll(List<SingleProduct> fps);

    // Insert single
    @Insert
    long insert(SingleProduct fp);

    // Delete single
    @Delete
    int delete(SingleProduct fp);

    @Query("DELETE FROM SingleProduct WHERE id = :id")
    int deleteById(long id);

    @Update
    int update(SingleProduct p);
}