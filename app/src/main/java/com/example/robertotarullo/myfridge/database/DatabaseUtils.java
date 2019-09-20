package com.example.robertotarullo.myfridge.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseUtils {
    public static final String DATABASE_NAME = "products_db";

    public static ProductDatabase getDatabase(Context context){
        return Room.databaseBuilder(context, ProductDatabase.class, DatabaseUtils.DATABASE_NAME).build();
    }
}

// TODO Inserire funzioni
