package com.example.robertotarullo.myfridge.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.robertotarullo.myfridge.Bean.Pack;

import java.util.List;

@Dao
public interface PackDao {

    // Get all
    @Query("SELECT * FROM pack")
    List<Pack> getAll();

    // Get single
    @Query("SELECT * FROM pack where id = :id")
    Pack get(long id);

    // Insert single
    @Insert
    long insertPack(Pack p);

    // Delete single
    @Delete
    void delete(Pack p);

    @Query("DELETE FROM pack WHERE id = :id")
    void deleteById(long id);

    @Query("UPDATE pack SET name = :name WHERE id = :id")
    int updateName(long id, String name);

    @Update
    int update(Pack p);
}