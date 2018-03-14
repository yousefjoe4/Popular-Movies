package com.example.popularmovies.Data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yousef on 11/3/2018.
 */

public class MoviesContract {

    public final static String CONTENT_AUTHORITY = "com.example.popularmovies";

    public final static String BASE_CONTENT_URI = "content://" + CONTENT_AUTHORITY;

    public final static String PATH_MOVIES = "movies";


    public static class MovieEntry implements BaseColumns {

        public final static Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI).buildUpon().appendPath(PATH_MOVIES).build();

        public final static String TABLE_NAME = "movies";

        public final static String COLUMN_MOVIE_ID = "id";

        public final static String COLUMN_PHOTO_URL = "photo_url";

        public final static String COLUMN_NAME = "name";

        public final static String COLUMN_OVERVIEW = "overview";

        public final static String COLUMN_RELEASE_DATE = "release_date";

        public final static String COLUMN_RATING = "rating";

        public final static String COLUMN_TRAILERS = "trailers";

        public final static String COLUMN_REVIEWS = "reviews";
    }
}
