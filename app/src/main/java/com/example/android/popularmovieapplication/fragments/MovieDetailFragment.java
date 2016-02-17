package com.example.android.popularmovieapplication.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovieapplication.R;
import com.example.android.popularmovieapplication.api.MovieResponse.Movie;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment {

    public final static String ARG_MOVIE = "movie";

    private ImageView mPosterImageView;
    private TextView mRatingTextView;
    private TextView mDateTextView;
    private TextView mOverviewTextView;
    private Movie mMovie;

    public static MovieDetailFragment getInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getArguments().getParcelable(ARG_MOVIE);
        if (mMovie == null) {
            throw new NullPointerException("Movie object should be put into fragment argument.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        mPosterImageView = (ImageView) v.findViewById(R.id.poster_image_view);
        mRatingTextView = (TextView) v.findViewById(R.id.rating_text_view);
        mDateTextView = (TextView) v.findViewById(R.id.date_text_view);
        mOverviewTextView = (TextView) v.findViewById(R.id.overview_text_view);

        // tint compound drawables
        Drawable ratingIcon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_stars_white_24dp);
        ratingIcon = DrawableCompat.wrap(ratingIcon);
        DrawableCompat.setTint(ratingIcon, getResources().getColor(R.color.rating_tint));
        mRatingTextView.setCompoundDrawablesWithIntrinsicBounds(ratingIcon, null, null, null);

        Drawable dateIcon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_event_white_24dp);
        dateIcon = DrawableCompat.wrap(dateIcon);
        DrawableCompat.setTint(dateIcon, getResources().getColor(R.color.date_tint));
        mDateTextView.setCompoundDrawablesWithIntrinsicBounds(dateIcon, null, null, null);

        // setup data
        Glide.with(getActivity())
                .load(mMovie.getPosterUrl())
                .centerCrop()
                .placeholder(R.drawable.movie_placeholder)
                .crossFade()
                .into(mPosterImageView);
        mRatingTextView.setText(Float.toString(mMovie.rating));
        mDateTextView.setText(mMovie.releaseDate);
        mOverviewTextView.setText(mMovie.overview);

        return v;
    }

}
