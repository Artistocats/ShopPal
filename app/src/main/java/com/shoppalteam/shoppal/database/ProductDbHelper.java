package com.shoppalteam.shoppal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ezerous on 2/5/2015.
 */
public class ProductDbHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "Products.db";
    private static final int DATABASE_VERSION = 9;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        ProductTable.onCreate(db);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ProductTable.onUpgrade(db, oldVersion, newVersion);
    }


}
