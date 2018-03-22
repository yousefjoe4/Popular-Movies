package com.example.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.popularmovies.data.MoviesContract.MovieEntry;


public class MovieDbHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "movies";
    private final static int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_DATABASE =
                        "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +

                        MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        MovieEntry.COLUMN_MOVIE_ID  + " INTEGER UNIQUE, " +

                        MovieEntry.COLUMN_PHOTO_URL + " TEXT, " +

                        MovieEntry.COLUMN_NAME + " TEXT NOT NULL, " +

                        MovieEntry.COLUMN_OVERVIEW + " TEXT, " +

                        MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +

                        MovieEntry.COLUMN_RATING + " INTEGER, " +

                        MovieEntry.COLUMN_TRAILERS + " TEXT, " +

                        MovieEntry.COLUMN_REVIEWS + " TEXT );";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
