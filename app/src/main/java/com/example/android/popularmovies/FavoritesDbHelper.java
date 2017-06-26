package com.example.android.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.FavoritesContract.FavoritesEntry;

/**
 * Created by adamzarn on 6/26/17.
 */

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITES_TABLE =

                "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +

                        FavoritesEntry._ID                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        FavoritesEntry.COLUMN_ID              + " REAL NOT NULL, "                     +
                        FavoritesEntry.COLUMN_TITLE           + " REAL NOT NULL, "                     +
                        FavoritesEntry.COLUMN_RELEASE_DATE    + " REAL NOT NULL, "                     +
                        FavoritesEntry.COLUMN_VOTE_AVERAGE    + " REAL NOT NULL, "                     +
                        FavoritesEntry.COLUMN_PLOT_SYNOPSIS   + " REAL NOT NULL, "                     +
                        FavoritesEntry.COLUMN_POSTER          + " REAL NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
