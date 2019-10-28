package com.example.robertotarullo.myfridge.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.robertotarullo.myfridge.bean.Filter;

import java.util.List;

@Dao
public interface FilterDao {

    // Get all
    @Query("SELECT * FROM filter")
    List<Filter> getFilters();

    // Get single
    @Query("SELECT * FROM filter where id = :id")
    Filter getFilter(long id);

    // Get count
    @Query("SELECT COUNT(*) from filter")
    int getFiltersCount();

    // Insert all
    @Insert
    List<Long> insertAll(Filter... filters);

    // Insert single
    @Insert
    long insert(Filter filter);

    // Delete single
    @Delete
    void delete(Filter filter);

    @Query("DELETE FROM filter WHERE id = :id")
    void deleteFilterById(long id);

}