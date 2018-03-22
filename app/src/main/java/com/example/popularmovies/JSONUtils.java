package com.example.popularmovies;


import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class JSONUtils {

    private static final String BASE_URL = "http://api.themoviedb.org/3/movie";
    private static final String IMAGE_URL_BASE = "http://image.tmdb.org/t/p/w185";

    public static final String ORDER_BY_POPULAR = "popular";
    public static final String ORDER_BY_TOP_RATED = "top_rated";
    public static final String TYPE_TRAILERS = "videos";
    public static final String TYPE_REVIEWS = "reviews";


    private static final String API_KEY = "api_key";
    private static final String API_KEY_VALUE = BuildConfig.API_KEY;

    private static final String JSON_KEY_RESULTS = "results";
    private static final String JSON_KEY_ITEM_ID = "id";
    private static final String JSON_KEY_ITEM_ORIGINAL_TITLE = "original_title";
    private static final String JSON_KEY_ITEM_VOTE_AVERAGE = "vote_average";
    private static final String JSON_KEY_ITEM_POSTER_PATH = "poster_path";
    private static final String JSON_KEY_ITEM_OVERVIEW = "overview";
    private static final String JSON_KEY_ITEM_RELEASE_DATE = "release_date";
    private static final String JSON_KEY_ITEM_RESULTS = "results";
    public static final String JSON_KEY_ITEM_TYPE = "type";
    public static final String JSON_KEY_ITEM_KEY = "key";
    public static final String JSON_VALUE_ITEM_TRAILER = "Trailer";
    public static final String JSON_VALUE_ITEM_CONTENT = "content";


    public static List<Movie> fetchMovies(String orderBy) {

        URL url = getMoviesUrl(orderBy);

        String jsonData = getData(url);

        return parseMoviesJSON(jsonData);
    }

    public static List<String> fetchTrailersReviews(String id, String type) {
        URL url = getTrailersReviewsUrl(id, type);
        String jsonData = getData(url);
        switch (type) {
            case TYPE_TRAILERS:
                return parseTrailersJSON(jsonData);
            case TYPE_REVIEWS:
                return parseReviewsJSON(jsonData);
        }
        return null;
    }


    public static URL getMoviesUrl(String orderBy) {
        URL url = null;
        try {
            String uriString = Uri.parse(BASE_URL).
                    buildUpon().
                    appendPath(orderBy).
                    appendQueryParameter(API_KEY, API_KEY_VALUE).
                    build().toString();
            url = new URL(uriString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL getTrailersReviewsUrl(String id, String type) {
        URL url = null;
        try {
            String uriString = Uri.parse(BASE_URL).
                    buildUpon().
                    appendPath(id).
                    appendPath(type).
                    appendQueryParameter(API_KEY, API_KEY_VALUE).
                    build().toString();
            url = new URL(uriString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    public static String getData(URL url) {
        HttpURLConnection httpURLConnection = null;
        String data = null;
        try {

            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext()) {
                data = scanner.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
        return data;
    }

    public static List<Movie> parseMoviesJSON(String jsonData) {
        List<Movie> moviesList = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            if (!(jsonObject.has(JSON_KEY_RESULTS))) {
                return null;
            }

            JSONArray jsonArray = jsonObject.optJSONArray(JSON_KEY_RESULTS);

            moviesList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                int id = jsonArray.getJSONObject(i).optInt(JSON_KEY_ITEM_ID);
                String originalTitle = jsonArray.getJSONObject(i).optString(JSON_KEY_ITEM_ORIGINAL_TITLE);
                String releaseDate = jsonArray.getJSONObject(i).optString(JSON_KEY_ITEM_RELEASE_DATE);
                String overview = jsonArray.getJSONObject(i).optString(JSON_KEY_ITEM_OVERVIEW);
                int voteAverage = jsonArray.getJSONObject(i).optInt(JSON_KEY_ITEM_VOTE_AVERAGE);

                String imagePosterString = jsonArray.getJSONObject(i).optString(JSON_KEY_ITEM_POSTER_PATH);
                Uri imageUri = Uri.parse(IMAGE_URL_BASE).
                        buildUpon().
                        appendEncodedPath(imagePosterString).
                        build();


                moviesList.add(new Movie(id, originalTitle, releaseDate, overview, voteAverage, imageUri));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return moviesList;
    }


    public static List<String> parseTrailersJSON(String jsonData) {
        List<String> trailers = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has(JSON_KEY_ITEM_RESULTS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_ITEM_RESULTS);
                for (int i = 0; jsonArray.length() > i && trailers.size() < 2; i++) {

                    JSONObject item = jsonArray.getJSONObject(i);
                    if (item.has(JSON_KEY_ITEM_KEY) && item.has(JSON_KEY_ITEM_TYPE)) {
                        String videoType = item.optString(JSON_KEY_ITEM_TYPE);

                        if (videoType.equals(JSON_VALUE_ITEM_TRAILER)) {
                            String videoKey = item.optString(JSON_KEY_ITEM_KEY);
                            trailers.add(videoKey);
                        }
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return trailers;
    }

    public static List<String> parseReviewsJSON(String jsonData) {
        List<String> reviews = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (jsonObject.has(JSON_KEY_ITEM_RESULTS)) {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_ITEM_RESULTS);

                for (int i = 0; jsonArray.length() > i && i < 3; i++) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    String review = item.optString(JSON_VALUE_ITEM_CONTENT);
                    reviews.add(review);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }
}
