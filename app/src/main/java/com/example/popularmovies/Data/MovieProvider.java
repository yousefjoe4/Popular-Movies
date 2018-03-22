package com.example.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieProvider extends ContentProvider {
    MovieDbHelper movieDbHelper;
    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIE_WITH_ID = 101;

    final static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, CODE_MOVIES);
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES + "/#", CODE_MOVIE_WITH_ID);
    }

    @Override
    public boolean onCreate() {

        movieDbHelper = new MovieDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String selections, String[] selectionsArgs, String orderBy) {
        SQLiteDatabase sqLiteDatabase = movieDbHelper.getReadableDatabase();

        Cursor cursor = null;

        switch (uriMatcher.match(uri)) {

            case CODE_MOVIES:

                cursor = sqLiteDatabase.query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        columns,
                        selections,
                        selectionsArgs,
                        null,
                        null,
                        orderBy);

                break;
            case CODE_MOVIE_WITH_ID:

                selections = MoviesContract.MovieEntry.COLUMN_MOVIE_ID + "=?";

                int id = (int) ContentUris.parseId(uri);
                selectionsArgs = new String[]{String.valueOf(id)};

                cursor = sqLiteDatabase.query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        columns,
                        selections,
                        selectionsArgs,
                        null,
                        null,
                        null);
                break;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {

            case CODE_MOVIES:

                long id = sqLiteDatabase.insert(MoviesContract.MovieEntry.TABLE_NAME, null, contentValues);
                uri = ContentUris.withAppendedId(uri, id);
getContext().getContentResolver().notifyChange(uri,null);
                return uri;

            default:
                return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionsArgs) {
        switch (uriMatcher.match(uri)) {

            case CODE_MOVIE_WITH_ID:

                selection = MoviesContract.MovieEntry.COLUMN_MOVIE_ID + "=?";

                int id = (int) ContentUris.parseId(uri);
                selectionsArgs = new String[]{String.valueOf(id)};

                int deletedRow = movieDbHelper.getWritableDatabase().delete(MoviesContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionsArgs);
                getContext().getContentResolver().notifyChange(uri,null);

                return deletedRow;

            default:
                return 0;

        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
