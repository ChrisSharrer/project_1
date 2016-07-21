package com.lc23.android.popularmovies;

import android.net.Uri;

public class Movie {

    private final String name;
    private final Uri image;

    public Movie(String name, Uri image) {
        this.name = name;
        this.image = image;
    }

    public Uri getImage() {
        return image;
    }

    public String getName() {
        return name;
    }
}
