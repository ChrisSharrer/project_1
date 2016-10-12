package com.lc23.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.lc23.android.popularmovies.data.MovieContract;
import com.lc23.android.popularmovies.data.MovieContract.MovieTypeEntry;
import com.lc23.android.popularmovies.utility.LinkListAdapter;
import com.lc23.android.popularmovies.utility.Utility;

import java.util.Date;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static class ViewHolder {
        final TextView movieNameView;
        final ImageView posterView;
        final TextView releaseDateView;
        final TextView runtimeView;
        final TextView userRatingView;
        final TextView plotView;
        final View touch;
        final ViewSwitcher favorite;
        final TextView favoriteCaption;
        final TextView trailerHeading;
        final TextView reviewHeading;

        ViewHolder(View view) {
            movieNameView = (TextView) view.findViewById(R.id.detail_movie_name);
            posterView = (ImageView) view.findViewById(R.id.detail_poster);
            releaseDateView = (TextView) view.findViewById(R.id.detail_release_date);
            runtimeView = (TextView) view.findViewById(R.id.detail_runtime);
            userRatingView = (TextView) view.findViewById(R.id.detail_user_rating);
            plotView = (TextView) view.findViewById(R.id.detail_plot);
            touch = view.findViewById(R.id.detail_touch);
            favorite = (ViewSwitcher) view.findViewById(R.id.detail_favorite);
            favoriteCaption = (TextView) view.findViewById(R.id.detail_favorite_caption);
            trailerHeading = (TextView) view.findViewById(R.id.detail_heading_trailers);
            reviewHeading = (TextView) view.findViewById(R.id.detail_heading_reviews);
        }
    }

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAIL_URI = "URI";

    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    private static final String SHARE_HASHTAG = "#PopularMoviesApp";

    private Uri uri;

    private ViewHolder viewHolder;

    private ListView trailerListView;
    private LinkListAdapter trailerAdapter;
    private ListView reviewListView;
    private LinkListAdapter reviewAdapter;

    private int trailerCount, reviewCount;

    private Menu menu;

    private String movieName;
    private String shareText;
    private ShareActionProvider shareActionProvider;

    public static DetailFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_URI, uri);
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(args);
        return detailFragment;
    }

    public static DetailFragment newInstance(int movieId) {
        return DetailFragment.newInstance(MovieContract.MovieDetailsEntry.buildMovieDetailsForMovieUri(movieId));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set uri from arguments
        final Bundle args = getArguments();
        if (args != null)
            uri = args.getParcelable(DETAIL_URI);

        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        viewHolder = new ViewHolder(rootView);

        final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showLink(((LinkListAdapter) adapterView.getAdapter()).getLink(position));
            }
        };

        trailerAdapter = new LinkListAdapter(getActivity())
                .setLayout(R.layout.list_item_trailer)
                .setImageView(R.id.trailer_icon_image_view, ResourcesCompat.getDrawable(getResources(), R.drawable.play_triangle, null))
                .setTextView(R.id.trailer_text_view, MovieContract.MovieLinksEntry.COLUMN_LINK_TEXT)
                .setLinkColumn(MovieContract.MovieLinksEntry.COLUMN_URI);

        trailerListView = (ListView) rootView.findViewById(R.id.listview_trailers);
        trailerListView.setAdapter(trailerAdapter);
        trailerListView.setOnItemClickListener(listener);

        // Set share text after layout is displayed
        trailerListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                trailerListView.removeOnLayoutChangeListener(this);
                createShare();
            }
        });

        reviewAdapter = new LinkListAdapter(getActivity())
                .setLayout(R.layout.list_item_review)
                .setImageView(0, null)
                .setTextView(R.id.review_text_view, MovieContract.MovieLinksEntry.COLUMN_LINK_TEXT)
                .setLinkColumn(MovieContract.MovieLinksEntry.COLUMN_URI);

        reviewListView = (ListView) rootView.findViewById(R.id.listview_reviews);
        reviewListView.setAdapter(reviewAdapter);
        reviewListView.setOnItemClickListener(listener);

        // Fixes bug where detail screen starts scrolled down
        trailerListView.setFocusable(false);
        reviewListView.setFocusable(false);

        return rootView;
    }

    private void showLink(String url) {
        final Uri linkUri = Uri.parse(url)
                .buildUpon()
                .build();

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(linkUri);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
        else
            Log.d(LOG_TAG, "Couldn't call " + linkUri.toString() + ", no handler available");
    }

    public static void setListViewHeight(ListView listView) {
        final ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int totalHeight = 0;

            final int itemCount = listAdapter.getCount();

            // Add height of items
            for (int i = 0; i < itemCount; i++) {
                View item = listAdapter.getView(i, null, listView);
                item.measure(0, 0);
                totalHeight += item.getMeasuredHeight();
            }

            // Add height of dividers
            totalHeight += listView.getDividerHeight() * (itemCount - 1);

            // Set list height.
            final ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
        }
    }

    private Intent createShareIntent() {
        return new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, String.format("%s %s", shareText, SHARE_HASHTAG))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        item.setVisible(false);
        getActivity().invalidateOptionsMenu();

        // If onLoadFinished happens before this, we can go ahead and set the share intent now
        createShare();

        if (trailerCount > 0) {
            item.setVisible(true);
            getActivity().invalidateOptionsMenu();
        }
    }

    private void createShare() {
        if (trailerAdapter == null)
            return;

        String link = trailerAdapter.getLink(0);
        if (movieName != null && link != null) {
            shareText = String.format("'%s' trailer: %s", movieName, link);
            if (shareActionProvider != null)
                shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (uri == null)
            return null;

        final int movieId = MovieContract.MovieDetailsEntry.getMovieIdFromUri(uri);

        switch (id) {
            case DETAIL_LOADER:
                return new CursorLoader(
                        getActivity(),
                        uri,
                        null,
                        null,
                        null,
                        null
                );
            case TRAILER_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MovieContract.MovieLinksEntry.buildMovieLinksUri(movieId, "trailer"),
                        null,
                        null,
                        null,
                        null
                );
            case REVIEW_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MovieContract.MovieLinksEntry.buildMovieLinksUri(movieId, "review"),
                        null,
                        null,
                        null,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {
            case TRAILER_LOADER:
                trailerAdapter.swapCursor(cursor);
                trailerAdapter.notifyDataSetChanged();
                trailerCount = cursor.getCount();
                setListViewHeight(trailerListView);
                break;
            case REVIEW_LOADER:
                reviewAdapter.swapCursor(cursor);
                reviewCount = cursor.getCount();
                setListViewHeight(reviewListView);
                break;
            case DETAIL_LOADER:
                if (!cursor.moveToFirst())
                    return;

                // Movie name
                movieName = cursor.getString(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_TITLE));
                viewHolder.movieNameView.setText(movieName);

                // Poster image
                final String posterPath = "https://image.tmdb.org/t/p/w780" + cursor.getString(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_POSTER_THUMBNAIL));
                Utility.getFromPicasso(getActivity(), posterPath, viewHolder.posterView, 0, 0);

                // Release date
                final long releaseMillis = cursor.getLong(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_RELEASE_DATE));
                if (releaseMillis != FetchMovieTask.RELEASE_DATE_NOT_FOUND) {
                    Date releaseDate = new Date(releaseMillis);
                    viewHolder.releaseDateView.setText(getString(R.string.format_release_date, releaseDate));
                }

                final int runtime = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_RUNTIME));
                if (runtime > 0) {
                    viewHolder.runtimeView.setText(getString(R.string.format_runtime, runtime));
                }

                // User rating
                final double userRating = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_USER_RATING));
                viewHolder.userRatingView.setText(getString(R.string.format_user_rating, userRating));

                // Plot synopsis
                final String plot = cursor.getString(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_PLOT));
                viewHolder.plotView.setText(plot);

                // Favorite button
                final ViewSwitcher favorite = viewHolder.favorite;
                final int movieId = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieDetailsEntry.COLUMN_MOVIE_DB_ID));
                final boolean isFavorite = isFavorite(movieId);
                setFavorite(isFavorite, false);
                favorite.setAnimateFirstView(true);
                viewHolder.touch.setOnClickListener(new FavoriteOnClickListener(isFavorite, movieId));

                break;
        }

        // Trailer header
        if (trailerCount > 0) {
            viewHolder.trailerHeading.setVisibility(View.VISIBLE);
            if (menu != null ) {
                MenuItem item = menu.findItem(R.id.action_share);
                if (!item.isVisible()) {
                    createShare();
                    item.setVisible(true);
                    getActivity().invalidateOptionsMenu();
                }
            }
        }

        // Review header
        if (reviewCount > 0) {
            viewHolder.reviewHeading.setVisibility(View.VISIBLE);
        }
    }

    private void setFavorite(boolean favorite, boolean animate) {
        ViewSwitcher button = viewHolder.favorite;
        TextView caption = viewHolder.favoriteCaption;

        button.setVisibility(View.VISIBLE);

        View startOff = button.findViewById(R.id.star_off);
        View starOn = button.findViewById(R.id.star_on);
        if (favorite) {
            if (button.getCurrentView() != starOn)
                button.showNext();
            if (animate)
                caption.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
            caption.setVisibility(View.VISIBLE);
        }
        else {
            if (button.getCurrentView() != startOff)
                button.showPrevious();
            if (animate)
                caption.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
            caption.setVisibility(View.INVISIBLE);
        }
    }

    class FavoriteOnClickListener implements View.OnClickListener {
        private boolean favorite;
        private final int movieId;

        FavoriteOnClickListener(boolean favorite, int movieId) {
            this.favorite = favorite;
            this.movieId = movieId;
        }

        @Override
        public void onClick(View v) {
            favorite = !favorite;
            setFavorite(favorite, true);
            setFavoriteInDb(movieId, favorite);
        }
    }

    private boolean isFavorite(int movieId) {
        String favoriteType = getString(R.string.pref_type_favorites);
        Cursor cursor = getActivity().getContentResolver().query(MovieTypeEntry.buildMovieTypeUri(favoriteType, movieId), null, null, null, null);
        boolean found = cursor.moveToFirst();
        cursor.close();
        return found;
    }

    private void setFavoriteInDb(int movieId, boolean favorite) {
        String favoriteType = getString(R.string.pref_type_favorites);
        if (favorite) {
            ContentValues values = new ContentValues();
            values.put(MovieTypeEntry.COLUMN_MOVIE_DB_ID, movieId);
            values.put(MovieTypeEntry.COLUMN_TYPE, favoriteType);
            getActivity().getContentResolver().insert(MovieTypeEntry.CONTENT_URI, values);
        }
        else getActivity().getContentResolver().delete(MovieTypeEntry.CONTENT_URI,
                MovieTypeEntry.COLUMN_MOVIE_DB_ID + " = ? AND " + MovieTypeEntry.COLUMN_TYPE + " = ?",
                new String[] { Integer.toString(movieId), favoriteType });

        // Update favorite gridview if it is displayed
        if (Utility.getMovieType(getContext()).equals(favoriteType))
            getContext().getContentResolver().notifyChange(MovieContract.MovieEntry.buildMovieForTypeUri(favoriteType), null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRAILER_LOADER:
                trailerAdapter.swapCursor(null);
                break;
            case REVIEW_LOADER:
                reviewAdapter.swapCursor(null);
                break;
        }
    }
}
