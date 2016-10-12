package com.lc23.android.popularmovies.utility;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class LinkListAdapter extends CursorAdapter {
    private final List<String> links = new ArrayList<>();

    private int layout, textViewId, imageViewId, drawableIconId;
    private Drawable drawableIcon;
    private String textColumn, linkColumn;

    public LinkListAdapter(Context context, int layout, int imageViewId, Drawable drawableIcon, int textViewId, String textColumn, String linkColumn) {
        super(context, null, 0);

        setLayout(layout);
        setTextView(textViewId, textColumn);
        setImageView(imageViewId, drawableIcon);
        setLinkColumn(linkColumn);
    }

    public LinkListAdapter(Context context) {
        super(context, null, 0);
    }

    public LinkListAdapter setLayout(int layout) {
        this.layout = layout;
        return this;
    }

    public LinkListAdapter setTextView(int textViewId, String textColumn) {
        this.textViewId = textViewId;
        this.textColumn = textColumn;
        return this;
    }

    public LinkListAdapter setImageView(int imageViewId, Drawable drawableIcon) {
        this.imageViewId=imageViewId;
        this.drawableIcon=drawableIcon;
        return this;
    }

    public LinkListAdapter setLinkColumn(String linkColumn) {
        this.linkColumn = linkColumn;
        return this;
    }

    public String getLink(int position) {
        if (position >= links.size()) return null;
        return links.get(position);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (layout <= 0)
            throw new IllegalStateException("Layout has not been set");
        return LayoutInflater.from(context).inflate(layout, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if (imageViewId > 0 && drawableIcon != null) {
            final ImageView iconView = (ImageView) view.findViewById(imageViewId);
            iconView.setImageDrawable(drawableIcon);
        }

        if (textViewId > 0 && textColumn != null) {
            final String text = cursor.getString(cursor.getColumnIndex(textColumn));
            final TextView textView = (TextView) view.findViewById(textViewId);
            textView.setText(text);
        }

        if (linkColumn != null) {
            final String link = cursor.getString(cursor.getColumnIndex(linkColumn));
            links.add(link);
        }
    }
}
