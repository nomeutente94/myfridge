package com.example.robertotarullo.myfridge.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.robertotarullo.myfridge.Bean.SingleProduct;

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

    // Insert single
    @Insert
    long insert(SingleProduct fp);

    // Delete single
    @Delete
    int delete(SingleProduct fp);

    @Query("DELETE FROM SingleProduct WHERE id = :id")
    int deleteById(long id);

    @Query("UPDATE SingleProduct SET packageId = :packageId WHERE id = :id")
    int updatePackageId(long id, long packageId);

    @Query("UPDATE SingleProduct SET consumed = :consumed WHERE id = :id")
    int updateConsumption(long id, boolean consumed);

    @Query("UPDATE SingleProduct SET consumed = :consumed WHERE packageId = :packageId")
    int updatePackConsumption(long packageId, boolean consumed);

    @Update
    int update(SingleProduct p);
}