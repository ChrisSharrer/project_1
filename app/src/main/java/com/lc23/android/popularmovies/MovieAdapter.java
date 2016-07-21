package com.lc23.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lc23.android.popularmovies.data.MovieContract;

public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Show the movie poster
        final String posterPath = "https://image.tmdb.org/t/p/w185" + cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_THUMBNAIL));
        final ImageView posterView = (ImageView) view.findViewById(R.id.gridview_poster_image);
        Utility.getFromPicasso(context, posterPath, posterView, 255, 383);
    }
}
