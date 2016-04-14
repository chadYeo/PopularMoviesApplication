package com.example.android.popularmovieapplication.async;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.popularmovieapplication.api.TrailersResponse;
import com.example.android.popularmovieapplication.provider.MovieProvider;

import java.util.ArrayList;

/**
 * Movie trailers async task loader.

 */
public class TrailerListLoader extends AsyncTaskLoader<ArrayList<TrailersResponse.Trailer>> {

    private final long mMovieId;

    private ArrayList<TrailersResponse.Trailer> mTrailers;

    public TrailerListLoader(Context context, long movieId) {
        super(context);
        mMovieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mTrailers != null) {
            deliverResult(mTrailers);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<TrailersResponse.Trailer> loadInBackground() {
        String selection = MovieProvider.COL_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(mMovieId)};
        Cursor cursor = getContext().getContentResolver()
                .query(MovieProvider.TrailerContract.CONTENT_URI, null, selection, selectionArgs, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        } else {
            mTrailers = new ArrayList<>();
            int key = cursor.getColumnIndex(MovieProvider.TrailerContract.KEY);
            int name = cursor.getColumnIndex(MovieProvider.TrailerContract.NAME);
            while (cursor.moveToNext()) {
                TrailersResponse.Trailer trailer = new TrailersResponse.Trailer();
                trailer.key = cursor.getString(key);
                trailer.name = cursor.getString(name);
                mTrailers.add(trailer);
            }
            cursor.close();
            return mTrailers;
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
