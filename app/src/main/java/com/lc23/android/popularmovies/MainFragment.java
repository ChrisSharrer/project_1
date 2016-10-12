package com.lc23.android.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.lc23.android.popularmovies.data.MovieContract.MovieEntry;
import com.lc23.android.popularmovies.utility.Utility;

import java.util.Date;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int MOVIE_LOADER = 0;
    private static final String POSITION_KEY = "POSITION";
    private static final String MOVIE_KEY = "MOVIE";

    private MovieAdapter movieAdapter;

    private MovieSelectedListener movieSelectedListener;
    private int savedPosition;
    private int movieId;
    private GridView gridView;

    public interface MovieSelectedListener {
        void onMovieSelected(int moveId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main_fragment, menu);

        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(menu.findItem(R.id.spinner));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.pref_type_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = getResources().getStringArray(R.array.pref_type_values)[position];
                if (Utility.changeMovieType(getContext(), selectedType)) {
                    savedPosition = 0;
                    initialize(selectedType);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner.post(new Runnable() {
            @Override
            public void run() {
                final String movieType = Utility.getMovieType(getContext());
                String[] values = getResources().getStringArray(R.array.pref_type_values);
                for (int i = 0; i < values.length; i++) {
                    if (movieType.equals(values[i])) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
        });

    }
    private void initialize(String movieType) {
        long now = new Date().getTime();
        long lastAccess = Utility.getLastAccess(getContext());

        // Check database every hour unless there is no data loaded
        boolean load = now - lastAccess > 60 * 60 * 1000 || Utility.notLoaded(movieType);
        if (load)
            Utility.loadData(getContext(), movieType);

        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void setMovieSelectedListener(MovieSelectedListener movieSelectedListener) {
        this.movieSelectedListener = movieSelectedListener;
        initialize(Utility.getMovieType(getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity());

        gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                savedPosition = position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null  && cursor.getCount() > 0 && movieSelectedListener != null) {
                    movieId = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_DB_ID));
                    movieSelectedListener.onMovieSelected(movieId);
                }
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(POSITION_KEY))
                savedPosition = savedInstanceState.getInt(POSITION_KEY);
            if (savedInstanceState.containsKey(MOVIE_KEY))
                movieId = savedInstanceState.getInt(MOVIE_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedPosition > 0)
            savedInstanceState.putInt(POSITION_KEY, savedPosition);

        if (movieId > 0)
            savedInstanceState.putInt(MOVIE_KEY, movieId);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String movieType = Utility.getMovieType(getActivity());
        Uri moviesForTypeUri = MovieEntry.buildMovieForTypeUri(movieType);
        return new CursorLoader(getActivity(), moviesForTypeUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        movieAdapter.swapCursor(cursor);
        if (savedPosition > 0 )
            gridView.smoothScrollToPosition(savedPosition);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }

}
