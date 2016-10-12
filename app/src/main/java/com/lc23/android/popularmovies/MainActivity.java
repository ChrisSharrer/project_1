package com.lc23.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lc23.android.popularmovies.data.MovieContract;
import com.lc23.android.popularmovies.utility.Utility;

public final class MainActivity extends AppCompatActivity implements MainFragment.MovieSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT";
    private static final String MOVIE_KEY = "MOVIE";

    private boolean dualView;

    private int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (Utility.notLoaded(Utility.getMovieType(this))) {
            Utility.loadData(this);
            Utility.waitForDataToLoad();
        }
        else
            Utility.setDataLoaded();

        dualView = (findViewById(R.id.movie_detail_container) != null);

        if (dualView) {
            if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_KEY))
                movieId = savedInstanceState.getInt(MOVIE_KEY);
            else movieId = getFirstMovieId();
            onMovieSelected(movieId);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (movieId > 0)
            savedInstanceState.putInt(MOVIE_KEY, movieId);

        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();

        final MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        if (mainFragment != null)
            mainFragment.setMovieSelectedListener(this);
    }

    @Override
    public void onMovieSelected(int movieId) {
        this.movieId = movieId;
        if (dualView) {
            final DetailFragment detailFragment = DetailFragment.newInstance(movieId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        else
            startActivity(new Intent(this, DetailActivity.class).
                    setData(MovieContract.MovieDetailsEntry.buildMovieDetailsForMovieUri(movieId)));
    }

    private int getFirstMovieId() {
        final Uri moviesForTypeUri = MovieContract.MovieEntry.buildMovieForTypeUri(Utility.getMovieType(this));
        final Cursor cursor = getContentResolver().query(moviesForTypeUri, null, null, null, null);
        int movieId = 0;
        final boolean found = cursor.moveToFirst();
        if (found)
            movieId = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_DB_ID));
        cursor.close();
        return movieId;
    }

}
