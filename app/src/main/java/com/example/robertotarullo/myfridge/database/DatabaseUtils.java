package com.example.robertotarullo.myfridge.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseUtils {
    public static final String DATABASE_NAME = "products_db";

    public static ProductDatabase getDatabase(Context context){
        return Room.databaseBuilder(context, ProductDatabase.class, DatabaseUtils.DATABASE_NAME).addMigrations(MIGRATION_1_2).build();
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE TempTable " +
                            "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "packaged INTEGER NOT NULL, " +
                            "name TEXT, " +
                            "brand TEXT, " +
                            "price REAL NOT NULL, " +
                            "pricePerKilo REAL NOT NULL, " +
                            "weight REAL NOT NULL, " +
                            "currentWeight REAL NOT NULL, " +
                            "percentageQuantity REAL NOT NULL, " +
                            "pieces INTEGER NOT NULL, " +
                            "currentPieces INTEGER NOT NULL, " +
                            "expiringDaysAfterOpening INTEGER NOT NULL, " +
                            "purchaseDate INTEGER, " +
                            "consumptionDate INTEGER, " +
                            "storageCondition INTEGER NOT NULL, " +
                            "pointOfPurchaseId INTEGER NOT NULL, " +
                            "consumed INTEGER NOT NULL, " +
                            "expiryDate INTEGER, " +
                            "packagingDate INTEGER, " +
                            "opened INTEGER NOT NULL, " +
                            "openingDate INTEGER, " +
                            "openedStorageCondition INTEGER NOT NULL);");
            database.execSQL("INSERT INTO TempTable SELECT id, packaged, name, brand, price, pricePerKilo, weight, currentWeight, CAST(percentageQuantity AS REAL), pieces, currentPieces, expiringDaysAfterOpening, purchaseDate, consumptionDate, storageCondition, pointOfPurchaseId, consumed, expiryDate, packagingDate, opened, openingDate, openedStorageCondition FROM SingleProduct;");
            database.execSQL("DROP TABLE SingleProduct;");
            database.execSQL("ALTER TABLE TempTable RENAME TO SingleProduct;");
        }
    };
}

// TODO Inserire funzioni
