package com.shoppalteam.shoppal.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.shoppalteam.shoppal.database.ProductDbHelper;
import com.shoppalteam.shoppal.database.ProductTable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Ezerous on 7/5/2015.
 */
public class ProductContentProvider extends ContentProvider
{
    //Database
    private ProductDbHelper database;

    //For UriMatcher
    private static final int PRODUCTS = 1;
    private static final int PRODUCTS_ID = 2;

    private static final String AUTHORITY = "com.shoppalteam.shoppal.contentprovider";
    private static final String BASE_PATH = "products";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/products";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/product";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, PRODUCTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PRODUCTS_ID);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        database = new ProductDbHelper(getContext());
        return false;           //?
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(ProductTable.TABLE_PRODUCTS);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case PRODUCTS:
                break;
            case PRODUCTS_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(ProductTable.ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType)
        {
            case PRODUCTS:
                id = sqlDB.insert(ProductTable.TABLE_PRODUCTS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case PRODUCTS:
                rowsDeleted = sqlDB.delete(ProductTable.TABLE_PRODUCTS, selection,
                        selectionArgs);
                break;
            case PRODUCTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ProductTable.TABLE_PRODUCTS,
                            ProductTable.ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ProductTable.TABLE_PRODUCTS,
                            ProductTable.ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case PRODUCTS:
                rowsUpdated = sqlDB.update(ProductTable.TABLE_PRODUCTS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PRODUCTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ProductTable.TABLE_PRODUCTS,
                            values,
                            ProductTable.ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ProductTable.TABLE_PRODUCTS,
                            values,
                            ProductTable.ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { ProductTable.NAME,
                ProductTable.PRICE, ProductTable.QUANTITY,
                ProductTable.ID, ProductTable.NOTES,
                ProductTable.LAST_BUY, ProductTable.BUY_PERIOD,
                ProductTable.TIMES_BOUGHT};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
