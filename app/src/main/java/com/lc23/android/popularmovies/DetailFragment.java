package com.lc23.android.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lc23.android.popularmovies.data.MovieContract;

import java.util.Date;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static class ViewHolder {
        final TextView movieNameView;
        final ImageView posterView;
        final TextView releaseDateView;
        final TextView userRatingView;
        final TextView plotView;

        ViewHolder(View view) {
            movieNameView = (TextView) view.findViewById(R.id.detail_movie_name);
            posterView = (ImageView) view.findViewById(R.id.detail_poster);
            releaseDateView = (TextView) view.findViewById(R.id.detail_release_date);
            userRatingView = (TextView) view.findViewById(R.id.detail_user_rating);
            plotView = (TextView) view.findViewById(R.id.detail_plot);

        }
    }

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";

    private static final int DETAIL_LOADER = 0;

    private Uri uri;

    private ViewHolder viewHolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set uri from arguments
        Bundle args = getArguments();
        if (args != null)
            uri = args.getParcelable(DETAIL_URI);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        viewHolder = new ViewHolder(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (uri != null)
            return new CursorLoader(
                    getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    null
            );

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst())
            return;

        // Movie name
        String movieName = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE));
        viewHolder.movieNameView.setText(movieName);

        // Poster image
        final String posterPath = "https://image.tmdb.org/t/p/w780" + cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_THUMBNAIL));
        Utility.getFromPicasso(getActivity(), posterPath, viewHolder.posterView, 0, 0);

        // Release date
        long releaseMillis = cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
        Date releaseDate = new Date(releaseMillis);
        viewHolder.releaseDateView.setText(getString(R.string.format_release_date, releaseDate));

        // User rating
        double userRating = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING));
        viewHolder.userRatingView.setText(getString(R.string.format_user_rating, userRating));

        // Plot synopsis
        String plot = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT));
        viewHolder.plotView.setText(plot);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do
    }

}
