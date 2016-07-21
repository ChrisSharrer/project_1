package com.lc23.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public class Utility {
    public static String getSortOrderSetting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_default));
    }

    private static void picassoHelper(RequestCreator creator, ImageView imageView, int width, int height, Callback callback) {
        if (width > 0 || height > 0)
            creator.resize(width, height);

        creator.into(imageView, callback);
    }

    public static void getFromPicasso(final Context context, final String imagePath, final ImageView imageView, final int width, final int height) {
        // Load image from cache if available
        final RequestCreator fromCache = Picasso.with(context).load(imagePath)
                .networkPolicy(NetworkPolicy.OFFLINE);

        picassoHelper(fromCache, imageView, width, height, new Callback() {
            @Override
            public void onSuccess() {
                // Found in cache, do nothing else
            }

            @Override
            public void onError() {
                // Not in cache, load from web
                RequestCreator fromWeb = Picasso.with(context).load(imagePath);
                picassoHelper(fromWeb, imageView, width, height, null);
            }
        });
    }
}
