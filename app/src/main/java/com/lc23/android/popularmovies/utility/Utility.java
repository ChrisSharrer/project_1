package com.lc23.android.popularmovies.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.lc23.android.popularmovies.FetchMovieTask;
import com.lc23.android.popularmovies.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Helpful static methods
public final class Utility {

    private static Picasso picasso;

    private static final Set<String> loadedTypes = new HashSet<>();

    private static final Lock dataLock = new ReentrantLock();
    private static final Condition dataLoaded = dataLock.newCondition();
    private static boolean loaded;

    private Utility() {
        // Do not construct
    }

    public static void waitForDataToLoad() {
        dataLock.lock();
        try {
            while (!loaded)
                dataLoaded.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Wait interrupted");
        }
        finally {
            dataLock.unlock();
        }
    }

    public static void setDataLoaded() {
        dataLock.lock();
        try {
            loaded = true;
            dataLoaded.signalAll();
        }
        finally {
            dataLock.unlock();
        }
    }


    public static boolean notLoaded(String movieType) {
        return !loadedTypes.contains(movieType);
    }

    public static void addTypeLoaded(String movieType) {
        loadedTypes.add(movieType);
    }

    public static void loadData(Context context) {
        loadData(context, getMovieType(context));
    }

    public static void loadData(Context context, String movieType) {
        FetchMovieTask movieTask = new FetchMovieTask(context);
        movieTask.execute(movieType);
        setLastAccess(context, new Date().getTime());
    }

    public static boolean changeMovieType(final Context context, final String selectedType) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String currentType = prefs.getString(context.getString(R.string.pref_type_key), "NONE");

        if (!currentType.equals(selectedType)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(context.getString(R.string.pref_type_key), selectedType);
            editor.apply();
            return true;
        }
        return false;
    }

    public static String getMovieType(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_type_key), context.getString(R.string.pref_type_default));
    }

    private static void setLastAccess(final Context context, long access) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(context.getString(R.string.pref_last_access_key), access);
        editor.apply();
    }

    public static long getLastAccess(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(context.getString(R.string.pref_last_access_key), 0L);
    }

    private static Picasso getPicasso(Context context) {
        // Setup Picasso to use local cache
        if (picasso == null) {
            picasso = new Picasso.Builder(context)
                    .downloader(new OkHttpDownloader(context, Integer.MAX_VALUE))
                    .build();
        }
        return picasso;
    }

    private static void picassoHelper(final RequestCreator creator, final ImageView imageView, final int width, final int height, final Callback callback) {
        if (width > 0 || height > 0) {
            creator.resize(width, height).centerCrop();
        }

        creator.into(imageView, callback);
    }

    public static void getFromPicasso(final Context context, final String imagePath, final ImageView imageView, final int width, final int height) {
        final Picasso picasso = getPicasso(context);

        // Load image from cache if available
        final RequestCreator fromCache = picasso.load(imagePath)
                .networkPolicy(NetworkPolicy.OFFLINE);

        picassoHelper(fromCache, imageView, width, height, new Callback() {
            @Override
            public void onSuccess() {
                // Found in cache
            }

            @Override
            public void onError() {
                // Not in cache, load from web
                RequestCreator fromWeb = picasso.load(imagePath);
                picassoHelper(fromWeb, imageView, width, height, null);
            }
        });
    }

}
