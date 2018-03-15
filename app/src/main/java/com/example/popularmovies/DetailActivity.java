package com.example.popularmovies;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovies.Data.MovieDbHelper;
import com.example.popularmovies.Data.MoviesContract;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<String>> {

    @BindView(R.id.tv_original_title)
    TextView originalTitleTextView;
    @BindView(R.id.detail_image)
    ImageView detailImage;
    @BindView(R.id.tv_release_date)
    TextView releaseDate;
    @BindView(R.id.tv_overview)
    TextView overview;
    @BindView(R.id.tv_vote_average)
    TextView voteAverageTextView;

    @BindView(R.id.ic_play_trailer1)
    ImageView trailer1Icon;
    @BindView(R.id.tv_trailer1)
    TextView trailer1TextView;

    @BindView(R.id.ic_play_trailer2)
    ImageView trailer2Icon;
    @BindView(R.id.tv_trailer2)
    TextView trailer2TextView;
    @BindView(R.id.constraint_layout)
    ConstraintLayout constraintLayout;
    @BindView(R.id.trailers_layout)
    View trailersView;
    @BindView(R.id.review_title)
    View reviewView;
    FloatingActionButton fab;

    private final int LOADER_TRAILERS_ID = 2000;
    private final int LOADER_REVIEWS_ID = 3000;
    private final String BUNDLE_KEY = "type";
    private Movie movie;
    private final String YOUTUBE_URL = "https://www.youtube.com/watch";

    private final String EMPTY_TAG = "empty";
    private final String FILLED_TAG = "filled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);

        originalTitleTextView.setText(movie.originalTitle);
        Picasso.with(this).load(movie.imageUri).into(detailImage);
        releaseDate.setText(movie.releaseDate);
        overview.setText(movie.overview);
        String formattedVoteAverage = String.format("%d/10", Math.round(movie.voteAverage));
        voteAverageTextView.setText(formattedVoteAverage);


        fab = findViewById(R.id.fab_button);
        if(isMovieFavourite()){
            fab.setTag(FILLED_TAG);
            fab.setImageResource(R.drawable.ic_star);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton fabOnClick = (FloatingActionButton) view;

                String fabTag = (String) fabOnClick.getTag();

                if (fabTag.equals(EMPTY_TAG)) {
                    fab.setImageResource(R.drawable.ic_star);
                    fab.setTag(FILLED_TAG);
                    saveMovieToFavourites();
                } else if (fabTag.equals(FILLED_TAG)) {
                    fab.setImageResource(R.drawable.ic_star_empty);
                    fab.setTag(EMPTY_TAG);
                    deleteMoviesFromFavourites();
                }
            }
        });


        if (isNetworkActive()) {
            Bundle trailersBundle = new Bundle();
            trailersBundle.putString(BUNDLE_KEY, JSONUtils.TYPE_TRAILERS);
            getLoaderManager().initLoader(LOADER_TRAILERS_ID, trailersBundle, DetailActivity.this);

            Bundle reviewBundle = new Bundle();
            reviewBundle.putString(BUNDLE_KEY, JSONUtils.TYPE_REVIEWS);
            getLoaderManager().initLoader(LOADER_REVIEWS_ID, reviewBundle, DetailActivity.this);
        } else {
            trailersView.setVisibility(View.GONE);
            reviewView.setVisibility(View.GONE);
        }

    }

    @Override
    public Loader<List<String>> onCreateLoader(int i, final Bundle bundle) {
        return new AsyncTaskLoader<List<String>>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public List<String> loadInBackground() {
                switch (bundle.getString(BUNDLE_KEY)) {
                    case JSONUtils.TYPE_TRAILERS:
                        return JSONUtils.fetchTrailersReviews(String.valueOf(movie.id), JSONUtils.TYPE_TRAILERS);
                    case JSONUtils.TYPE_REVIEWS:
                        return JSONUtils.fetchTrailersReviews(String.valueOf(movie.id), JSONUtils.TYPE_REVIEWS);
                }
                return null;
            }


        };
    }

    @Override
    public void onLoadFinished(Loader<List<String>> loader, List<String> strings) {
        switch (loader.getId()) {
            case LOADER_TRAILERS_ID:
                movie.trailers = strings;

                if (movie.trailers.size() == 2) {
                    trailersView.setVisibility(View.VISIBLE);
                    trailer1Icon.setVisibility(View.VISIBLE);
                    trailer1TextView.setVisibility(View.VISIBLE);
                    trailer2Icon.setVisibility(View.VISIBLE);
                    trailer2TextView.setVisibility(View.VISIBLE);

                    setupTrailer1();
                    setupTrailer2();

                } else if (movie.trailers.size() == 1) {
                    trailersView.setVisibility(View.VISIBLE);
                    trailer1Icon.setVisibility(View.VISIBLE);
                    trailer1TextView.setVisibility(View.VISIBLE);
                    trailer2Icon.setVisibility(View.GONE);
                    trailer2TextView.setVisibility(View.GONE);

                    setupTrailer1();
                } else {
                    trailersView.setVisibility(View.GONE);
                }

                return;

            case LOADER_REVIEWS_ID:
                movie.reviews = strings;
                generateReviews(movie.reviews);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {

    }


    public void setupTrailer1() {
        View.OnClickListener trailer1OnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTrailer(movie.trailers.get(0));
            }
        };
        trailer1TextView.setOnClickListener(trailer1OnClick);
        trailer1Icon.setOnClickListener(trailer1OnClick);
    }

    public void setupTrailer2() {
        View.OnClickListener trailer2OnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTrailer(movie.trailers.get(1));
            }
        };
        trailer2TextView.setOnClickListener(trailer2OnClick);
        trailer2Icon.setOnClickListener(trailer2OnClick);
    }

    public void openTrailer(String key) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(YOUTUBE_URL).buildUpon().appendQueryParameter("v", key).build());
        startActivity(intent);
    }

    public void generateReviews(List<String> reviews) {
        int previousItemId = R.id.review_title;
        int reviewsSize = reviews.size();
        if (reviewsSize == 0) {
            reviewView.setVisibility(View.GONE);
        } else {
            reviewView.setVisibility(View.VISIBLE);
            for (int i = 0; i < reviewsSize; i++) {
                View reviewItem = LayoutInflater.from(this).inflate(R.layout.review_item, null);
                int currentId = View.generateViewId();
                reviewItem.setId(currentId);
                ConstraintSet constraintSet = new ConstraintSet();

                constraintLayout.addView(reviewItem);
                constraintSet.clone(constraintLayout);
                constraintSet.connect(currentId, ConstraintSet.TOP, previousItemId, ConstraintSet.BOTTOM);
                constraintSet.applyTo(constraintLayout);
                previousItemId = currentId;

                TextView reviewItemTextView = reviewItem.findViewById(R.id.tv_review_item);
                String review = reviews.get(i);
                reviewItemTextView.setText(review);
            }
        }
    }

    public boolean isNetworkActive() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo connectivityState = connectivityManager.getActiveNetworkInfo();

        return (null != connectivityState && (connectivityState.isConnected()));
    }
    private void saveMovieToFavourites(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movie.id);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_PHOTO_URL, movie.imageUri.toString());
        contentValues.put(MoviesContract.MovieEntry.COLUMN_NAME, movie.originalTitle);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE ,movie.releaseDate);
        contentValues.put(MoviesContract.MovieEntry.COLUMN_RATING, movie.voteAverage);


        getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI,contentValues);
    }
    private void deleteMoviesFromFavourites(){
        Uri uriWithId =  ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, movie.id);
        getContentResolver().delete(uriWithId, null, null);
    }

    private boolean isMovieFavourite(){
        String[] projection = {MoviesContract.MovieEntry.COLUMN_MOVIE_ID};
        Uri uriWithId = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI,movie.id);
        Cursor cursor = getContentResolver().query(uriWithId,projection,null, null, null);
        if(cursor != null && cursor.moveToFirst()){
           long id =  cursor.getLong(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));

           if(id == movie.id){
               return true;
           }
        }
        return false;
    }

}
