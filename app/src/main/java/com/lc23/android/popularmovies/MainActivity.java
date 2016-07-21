package com.lc23.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {
    private static boolean started;
    private String sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_main, new MainFragment())
                    .commit();

            // Setup Picasso
            if (!started) {
                Picasso.setSingletonInstance(null);
                Picasso.Builder builder = new Picasso.Builder(this);
                builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
                Picasso built = builder.build();
                Picasso.setSingletonInstance(built);
                started = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sort = Utility.getSortOrderSetting(this);
        if (sort != null && !sort.equals(this.sort)) {
            // Load new list of movies
            MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.activity_main);
            if (fragment != null)
                fragment.onSortChanged();

            this.sort = sort;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        startActivity(new Intent(this, DetailActivity.class).setData(dateUri));
    }
}
