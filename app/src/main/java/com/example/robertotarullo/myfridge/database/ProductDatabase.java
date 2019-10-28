package com.example.robertotarullo.myfridge.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.robertotarullo.myfridge.R;
import com.example.robertotarullo.myfridge.bean.Filter;
import com.example.robertotarullo.myfridge.bean.PointOfPurchase;
import com.example.robertotarullo.myfridge.bean.SingleProduct;

import java.util.concurrent.Executors;

@Database(entities = {SingleProduct.class, PointOfPurchase.class, Filter.class}, version = 2)
@TypeConverters({DateConverter.class})
public abstract class ProductDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "products_db";

    public abstract SingleProductDao productDao();
    public abstract PointOfPurchaseDao pointOfPurchaseDao();
    public abstract FilterDao filterDao();

    private static ProductDatabase instance;

    public synchronized static ProductDatabase getInstance(Context context) {
        if (instance == null) {
            instance = buildDatabase(context);
        }
        return instance;
    }

    private static ProductDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(context, ProductDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        // Aggiungi tre filtri di default al primo avvio
                        Executors.newSingleThreadScheduledExecutor().execute(() -> {
                            Filter filter1 = new Filter(context.getString(R.string.storage_default1)); // TODO
                            Filter filter2 = new Filter(context.getString(R.string.storage_default2)); // TODO
                            Filter filter3 = new Filter(context.getString(R.string.storage_default3)); // TODO
                            getInstance(context).filterDao().insertAll(filter1, filter2, filter3);
                        });


                    }
                })
                //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build();
    }

    /*
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

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE filter " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, " +
                    "position INTEGER NOT NULL);");
        }
    };
    */
}