package com.lc23.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;

import com.lc23.android.popularmovies.data.MovieContract;
import com.lc23.android.popularmovies.utility.Utility;

public final class MovieAdapter extends CursorAdapter {

    public static final String MOVIE_DB_URL_FORMAT = "https://image.tmdb.org/t/p/w%d";

    private static final int[] sizes = new int[] { 92, 154, 185, 342, 500, 780 };

    private int imageSize;
    private int columnWidth;

    public MovieAdapter(final Context context) {
        super(context, null, 0);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        if (imageSize == 0) {

            int screenDivisor = context.getResources().getInteger(R.integer.screenDivisor);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            GridView gridView = ((GridView) parent);
            int cols = gridView.getNumColumns();
            int spacing = context.getResources().getDimensionPixelSize(R.dimen.gridview_spacing);

            columnWidth = (metrics.widthPixels / (cols * screenDivisor)) - (spacing * (cols + 1));

            for (int size : sizes) {
                if (size > columnWidth) {
                    imageSize = size;
                    break;
                }
            }

        }
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

        final String posterPath = String.format(MOVIE_DB_URL_FORMAT, imageSize) + cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_THUMBNAIL));
        final ImageView posterView = (ImageView) view.findViewById(R.id.gridview_poster_image);
        Utility.getFromPicasso(context, posterPath, posterView, columnWidth, (int) (columnWidth * 1.5f));
    }
}
