package com.shoppalteam.shoppal.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Ezerous on 7/5/2015.
 */
public class ProductTable {
    // Database table
    public static final String TABLE_PRODUCTS = "products";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String QUANTITY = "quantity";
    public static final String NOTES = "notes";
    public static final String LAST_BUY = "last_buy";
    public static final String BUY_PERIOD = "buy_period";
    public static final String TIMES_BOUGHT = "times_bought";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_PRODUCTS
            + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NAME + " TEXT NOT NULL, "
            + PRICE + " REAL NOT NULL,"
            + QUANTITY + " INTEGER NOT NULL,"
            + NOTES + " TEXT,"
            + LAST_BUY + " DATETIME,"
            + BUY_PERIOD + " REAL," //in hours
            + TIMES_BOUGHT +" INTEGER"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(ProductTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(database);
    }
}
