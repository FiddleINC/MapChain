package com.example.test1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Mapchain";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "MapData";
    private static final String COLUMN_LATITUDE = "Latitude";
    private static final String COLUMN_LONGITUDE= "Longitude";
    private static final String COLUMN_GEOHASH = "Geohash";

    private static double latitude,longitude;
    private static String Geohash;

    DBHelper(Context context, double lat, double longt, String geohash) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        latitude = lat;
        longitude = longt;
        Geohash = geohash;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NAME + " ("  + COLUMN_LATITUDE + " double NOT NULL," +  COLUMN_LONGITUDE + " double NOT NULL," + COLUMN_GEOHASH + " varchar(200) NOT NULL" + ");";
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
        contentValues.put(COLUMN_LATITUDE, latitude);
        contentValues.put(COLUMN_LONGITUDE, longitude);
        contentValues.put(COLUMN_GEOHASH, Geohash);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_NAME, null, contentValues) != -1;
    }
}
