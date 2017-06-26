package com.example.android.popularmovies;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by adamzarn on 6/26/17.
 */

public class FavoritesProvider extends ContentProvider {

    public static final int CODE_FAVORITES = 100;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private FavoritesDbHelper favoritesDbHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoritesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FavoritesContract.PATH_FAVORITES, CODE_FAVORITES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        favoritesDbHelper = new FavoritesDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = favoritesDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case CODE_FAVORITES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (uriMatcher.match(uri)) {

            case CODE_FAVORITES: {

                cursor = favoritesDbHelper.getReadableDatabase().query(
                        FavoritesContract.FavoritesEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int numRowsDeleted;

        if (null == selection) selection = "1";

        switch (uriMatcher.match(uri)) {

            case CODE_FAVORITES:
                numRowsDeleted = favoritesDbHelper.getWritableDatabase().delete(
                        FavoritesContract.FavoritesEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not implementing getType in PopularMovies.");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = favoritesDbHelper.getWritableDatabase();

        Uri returnUri;

        switch (uriMatcher.match(uri)) {

            case CODE_FAVORITES:
                db.beginTransaction();
                int rowsInserted = 0;

                long id = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(FavoritesContract.FavoritesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;

    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("Not implementing update in PopularMovies");
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        favoritesDbHelper.close();
        super.shutdown();
    }
}
