package com.example.popularmovies;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.popularmovies.Data.MoviesContract;

import java.util.ArrayList;
import java.util.List;

import static com.example.popularmovies.Data.MoviesContract.MovieEntry;

public class MainActivity extends AppCompatActivity {
    private CustomRecyclerView recyclerViewAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView internetErrorMessage;
    private TextView favouritesErrorMessage;
    private String SPINNER_POPULAR_ITEM;
    private String SPINNER_TOP_RATED_ITEM;
    private String SPINNER_FAVOURITES_ITEM;
    private final int MOVIES_LOADER_ID = 1000;
    private final int FAVOURITE_MOVIES_LOADER_ID = 2000;
    private final String BUNDLE_KEY = "sortBY";
    private final String ON_SAVED_INSTANCE_KEY = "onSavedKey";
    Spinner spinner;
    String savedInstanceString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        internetErrorMessage = findViewById(R.id.tv_internet_error_message);
        favouritesErrorMessage = findViewById(R.id.tv_favourites_error_message);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        SPINNER_POPULAR_ITEM = getString(R.string.sort_by_popular);
        SPINNER_TOP_RATED_ITEM = getString(R.string.sort_by_top_rated);
        SPINNER_FAVOURITES_ITEM = getString(R.string.sort_by_favourites);

        savedInstanceString = null;

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ON_SAVED_INSTANCE_KEY)) {
                savedInstanceString = savedInstanceState.getString(ON_SAVED_INSTANCE_KEY);
            }
        }

        if (savedInstanceString == null || savedInstanceString.equals(SPINNER_POPULAR_ITEM)) {
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY, JSONUtils.ORDER_BY_POPULAR);
            getLoaderManager().initLoader(MOVIES_LOADER_ID, bundle, moviesList).forceLoad();
        } else if (savedInstanceString.equals(SPINNER_TOP_RATED_ITEM)) {

            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_KEY, JSONUtils.ORDER_BY_TOP_RATED);
            getLoaderManager().initLoader(MOVIES_LOADER_ID, bundle, moviesList).forceLoad();
        } else if (savedInstanceString.equals(SPINNER_FAVOURITES_ITEM)) {
            getLoaderManager().initLoader(FAVOURITE_MOVIES_LOADER_ID, null, favouriteMovies);
        }


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateNoOfColumns(this));
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerViewAdapter = new CustomRecyclerView(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    LoaderManager.LoaderCallbacks<List<Movie>> moviesList = new LoaderManager.LoaderCallbacks<List<Movie>>() {
        @Override
        public Loader<List<Movie>> onCreateLoader(int i, final Bundle bundle) {

            return new AsyncTaskLoader<List<Movie>>(MainActivity.this) {
                @Override
                protected void onStartLoading() {

                    if (!(isNetworkActive())) {
                        showInternetErrorMessage();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public List<Movie> loadInBackground() {
                    String enteredSortBy = bundle.getString(BUNDLE_KEY);
                    if (enteredSortBy != null) {
                        return JSONUtils.fetchMovies(enteredSortBy);
                    }
                    return null;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Movie>> loader, final List<Movie> moviesList) {
            showMoviesViews();
            CustomRecyclerView.ItemOnClick itemOnClick = new CustomRecyclerView.ItemOnClick() {
                @Override
                public void onClick(int i) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, moviesList.get(i));
                    startActivity(intent);
                }
            };
            recyclerViewAdapter.changeData(moviesList, itemOnClick);
        }

        @Override
        public void onLoaderReset(Loader<List<Movie>> loader) {

        }
    };

    LoaderManager.LoaderCallbacks<Cursor> favouriteMovies = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_PHOTO_URL,
                    MovieEntry.COLUMN_NAME,
                    MovieEntry.COLUMN_OVERVIEW,
                    MovieEntry.COLUMN_RELEASE_DATE,
                    MovieEntry.COLUMN_RATING,
                    MovieEntry.COLUMN_TRAILERS,
                    MovieEntry.COLUMN_REVIEWS};

            return new CursorLoader(MainActivity.this,
                    MoviesContract.MovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            showMoviesViews();

            final List<Movie> moviesListResult = convertCursorToMoviesList(cursor);
            if (moviesListResult == null || moviesListResult.size() == 0 || cursor == null) {
                showFavouritesErrorMessage();
                return;
            }
            CustomRecyclerView.ItemOnClick itemOnClick = new CustomRecyclerView.ItemOnClick() {
                @Override
                public void onClick(int i) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, moviesListResult.get(i));
                    startActivity(intent);
                }
            };
            recyclerViewAdapter.changeData(moviesListResult, itemOnClick);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };


    // Helper method to calculate the best columns to be displayed on a device
    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        return noOfColumns;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem(R.id.spinner);
        spinner = (Spinner) menuItem.getActionView();
        setupSpinner(spinner);

        return true;
    }

    public void setupSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_by,
                R.layout.spinner_item);

        spinner.setAdapter(arrayAdapter);
        if (savedInstanceString != null) {
            spinner.setSelection(arrayAdapter.getPosition(savedInstanceString));
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = adapterView.getItemAtPosition(position).toString();

                if (SPINNER_POPULAR_ITEM.equals(selectedItem)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_KEY, JSONUtils.ORDER_BY_POPULAR);
                    getLoaderManager().restartLoader(MOVIES_LOADER_ID, bundle, moviesList).forceLoad();

                } else if (SPINNER_TOP_RATED_ITEM.equals(selectedItem)) {

                    Bundle bundle = new Bundle();
                    bundle.putString(BUNDLE_KEY, JSONUtils.ORDER_BY_TOP_RATED);
                    getLoaderManager().restartLoader(MOVIES_LOADER_ID, bundle, moviesList).forceLoad();

                } else if (SPINNER_FAVOURITES_ITEM.equals(selectedItem)) {

                    getLoaderManager().initLoader(FAVOURITE_MOVIES_LOADER_ID, null, favouriteMovies);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public boolean isNetworkActive() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo connectivityState = connectivityManager.getActiveNetworkInfo();

        return (null != connectivityState && (connectivityState.isConnected()));
    }

    public List<Movie> convertCursorToMoviesList(Cursor cursor) {
        List<Movie> moviesListResult = new ArrayList<>();

        if (!(cursor.moveToFirst())) {
            return null;
        }
        do {
            long movieId = cursor.getLong(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));

            String photoUrl = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_PHOTO_URL));
            Uri imageUri = Uri.parse(photoUrl);

            String movieName = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_NAME));

            String movieOverview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));

            String movieReleaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));

            int movieRating = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_RATING));


            moviesListResult.add(new Movie(movieId, movieName, movieReleaseDate, movieOverview, movieRating, imageUri));
        } while (cursor.moveToNext());

        return moviesListResult;
    }

    public void showInternetErrorMessage() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        favouritesErrorMessage.setVisibility(View.GONE);
        internetErrorMessage.setVisibility(View.VISIBLE);
    }

    public void showFavouritesErrorMessage() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        internetErrorMessage.setVisibility(View.GONE);
        favouritesErrorMessage.setVisibility(View.VISIBLE);
    }

    public void showMoviesViews() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        internetErrorMessage.setVisibility(View.GONE);
        favouritesErrorMessage.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (spinner != null) {
            String selectedItem = spinner.getSelectedItem().toString();
            outState.putString(ON_SAVED_INSTANCE_KEY, selectedItem);
        }

    }
}
