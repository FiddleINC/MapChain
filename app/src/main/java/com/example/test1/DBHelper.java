package com.example.test1;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Set;


public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Mapchain";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "MapData";
    private static final String COLUMN_HEXHASH = "HexHash";
    private static final String COLUMN_GEOHASH = "Geohash";

    private static String hexHash;
    private static Set<String> Geohash;

    DBHelper(Context context, String hexhash, Set<String> geohash) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        hexHash = hexhash;
        Geohash = geohash;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("  + COLUMN_HEXHASH + " string NOT NULL," + COLUMN_GEOHASH + " string NOT NULL" + ");";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    boolean insertData () {
        ContentValues contentValues = new ContentValues();
        for(String geohash : Geohash) {
            contentValues.put(COLUMN_HEXHASH, hexHash);
            contentValues.put(COLUMN_GEOHASH, geohash);
        }
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db, 0, 0);
        System.out.println("SQL DONE");
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }
}
