package com.example.android.popularmovieapplication.fragments;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovieapplication.R;
import com.example.android.popularmovieapplication.api.MovieResponse.Movie;
import com.example.android.popularmovieapplication.api.RetrofitAdapter;
import com.example.android.popularmovieapplication.api.ReviewsResponse;
import com.example.android.popularmovieapplication.api.TmdbService;
import com.example.android.popularmovieapplication.api.TrailersResponse;
import com.example.android.popularmovieapplication.async.MovieDetailsStoreAsyncTask;
import com.example.android.popularmovieapplication.async.ReviewListLoader;
import com.example.android.popularmovieapplication.async.TrailerListLoader;
import com.example.android.popularmovieapplication.provider.MovieProvider;

import java.util.ArrayList;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList>{

    public final static String MOVIE = "movie";

    public final static String FAVORITE = "favorite";

    private final static String ARG_TRAILERS = "trailers";

    private final static String ARG_REVIEWS = "reviews";

    private final static int TRAILER_LOADER_ID = 1;

    private final static int REVIEW_LOADER_ID = 2;

    private final static String[] RANDOM_COLORS = {"#EF9A9A", "#F48FB1", "#B39DDB", "#9FA8DA",
            "#90CAF9", "#81D4FA", "#80DEEA", "#80CBC4", "#A5D6A7", "#C5E1A5", "#E6EE9C", "#FFE082",
            "#FFCC80", "#FFAB91", "#BCAAA4", "#B0BEC5"};

    @InjectView(R.id.poster_image_view)
    ImageView mPosterImageView;

    @InjectView(R.id.rating_text_view)
    TextView mRatingTextView;

    @InjectView(R.id.date_text_view)
    TextView mDateTextView;

    @InjectView(R.id.overview_text_view)
    TextView mOverviewTextView;

    @InjectView(R.id.empty_overview_text_view)
    TextView mEmptyOverviewTextView;

    @InjectView(R.id.trailers_parent)
    LinearLayout mTrailersParent;

    @InjectView(R.id.empty_trailers_text_view)
    TextView mEmptyTrailersTextView;

    @InjectView(R.id.reviews_parent)
    LinearLayout mReviewsParent;

    @InjectView(R.id.empty_reviews_text_view)
    TextView mEmptyReviewsTextView;

    private MenuItem mShareMenuItem;

    private Movie mMovie;

    private ArrayList<TrailersResponse.Trailer> mTrailers;
    private ArrayList<ReviewsResponse.Review> mReviews;

    private DetailsActionListener mActionListener;

    private TmdbService mTmdbService;

    public static MovieDetailFragment getInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DetailsActionListener) {
            mActionListener = (DetailsActionListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getArguments().getParcelable(MOVIE);
        if (mMovie == null) {
            throw new NullPointerException("Movie object should be put into fragment argument.");
        }
        RestAdapter restAdapter = RetrofitAdapter.getRestAdapter();
        mTmdbService = restAdapter.create(TmdbService.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_details, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
        mShareMenuItem.setVisible(isVisible() && mTrailers != null && mTrailers.size() > 0);
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
        mEmptyOverviewTextView = (TextView) v.findViewById(R.id.empty_overview_text_view);
        mTrailersParent = (LinearLayout) v.findViewById(R.id.trailers_parent);
        mReviewsParent = (LinearLayout) v.findViewById(R.id.reviews_parent);

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
        if (mMovie.overview != null) {
            mOverviewTextView.setText(mMovie.overview);
            mOverviewTextView.setVisibility(View.VISIBLE);
            mEmptyOverviewTextView.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_TRAILERS, mTrailers);
        outState.putParcelableArrayList(ARG_REVIEWS, mReviews);
        super.onSaveInstanceState(outState);
    }

    /** Opens first trailer link in browser/youtube */
    private void openTrailer(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /** Loads data first from local db, then updates from server */
    private void loadData() {
        //first, load trailers and reviews from db, then update from server
        getActivity().getSupportLoaderManager().restartLoader(TRAILER_LOADER_ID, null, this);
        getActivity().getSupportLoaderManager().restartLoader(REVIEW_LOADER_ID, null, this);
        refreshData();
    }

    /** Loads data from server, stores response to local db */
    private void refreshData() {
        mTmdbService.getTrailers(mMovie.id, new Callback<TrailersResponse>() {
            @Override
            public void success(TrailersResponse trailersResponse, Response response) {
                mTrailers = trailersResponse.results;
                populateTrailers();
                storeTrailers();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });

        mTmdbService.getReviews(mMovie.id, new Callback<ReviewsResponse>() {
            @Override
            public void success(ReviewsResponse reviewsResponse, Response response) {
                mReviews = reviewsResponse.results;
                populateReviews();
                storeReviews();
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    /**
     * UI setup
     */
    private void populateTrailers() {
        if (!isAdded()) {
            return;
        }
        if (mTrailers != null && mTrailers.size() > 0) {
            mTrailersParent.removeAllViews();
            Context context = getActivity();
            int paddingSmall = Math.round(getResources().getDimension(R.dimen.margin_small));
            int paddingTiny = Math.round(getResources().getDimension(R.dimen.margin_tiny));
            for (final TrailersResponse.Trailer item : mTrailers) {
                TextView view = new TextView(context);
                view.setText(item.name);
                view.setCompoundDrawablePadding(paddingSmall);
                setCompoundDrawable(view, R.drawable.ic_video);
                view.setPadding(0, paddingTiny, 0, paddingTiny);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openTrailer(item.getYoutubeLink());
                    }
                });
                mTrailersParent.addView(view);
            }
            mEmptyTrailersTextView.setVisibility(View.GONE);
            mTrailersParent.setVisibility(View.VISIBLE);
            if (mShareMenuItem != null) {
                mShareMenuItem.setVisible(true);
            }
        } else {
            mEmptyTrailersTextView.setVisibility(View.VISIBLE);
            mTrailersParent.setVisibility(View.GONE);
        }
    }
    @SuppressWarnings("deprecation")
    private void populateReviews() {
        if (!isAdded()) {
            return;
        }
        if (mReviews != null && mReviews.size() > 0) {
            mReviewsParent.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            Random random = new Random();
            for (ReviewsResponse.Review item : mReviews) {
                View view = inflater.inflate(R.layout.review_item, mReviewsParent, false);
                ReviewViewHolder viewHolder = new ReviewViewHolder(view);
                viewHolder.nameTextView.setText(item.author);
                viewHolder.contentTextView.setText(item.content);
                int tint = Color.parseColor(RANDOM_COLORS[random.nextInt(RANDOM_COLORS.length)]);
                Drawable drawable = ContextCompat
                        .getDrawable(getActivity(), R.drawable.circle_background);
                drawable.setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.avatarImageView.setBackground(drawable);
                } else {
                    viewHolder.avatarImageView.setBackgroundDrawable(drawable);
                }
                mReviewsParent.addView(view);
            }
            mEmptyReviewsTextView.setVisibility(View.GONE);
            mReviewsParent.setVisibility(View.VISIBLE);
        } else {
            mEmptyReviewsTextView.setVisibility(View.VISIBLE);
            mReviewsParent.setVisibility(View.GONE);
        }
    }

    private void setCompoundDrawable(TextView view, int resId) {
        Drawable ratingIcon = ContextCompat.getDrawable(getActivity(), resId);
        ratingIcon = DrawableCompat.wrap(ratingIcon);
        DrawableCompat.setTint(ratingIcon, getResources().getColor(R.color.icon_tint));
        view.setCompoundDrawablesWithIntrinsicBounds(ratingIcon, null, null, null);
    }

    /**
     * Data storage
     */
    private void storeTrailers() {
        if (mTrailers.size() == 0) {
            return;
        }
        ContentValues[] values = new ContentValues[mTrailers.size()];
        for (int i = 0; i < mTrailers.size(); i++) {
            TrailersResponse.Trailer item = mTrailers.get(i);
            ContentValues value = new ContentValues();
            value.put(MovieProvider.TrailerContract.MOVIE_ID, mMovie.id);
            value.put(MovieProvider.TrailerContract.KEY, item.key);
            value.put(MovieProvider.TrailerContract.NAME, item.name);
            values[i] = value;
        }
        new MovieDetailsStoreAsyncTask(getActivity(),
                MovieProvider.TrailerContract.CONTENT_URI, mMovie.id)
                .execute(values);
    }

    private void storeReviews() {
        if (mReviews.size() == 0) {
            return;
        }
        ContentValues[] values = new ContentValues[mReviews.size()];
        for (int i = 0; i < mReviews.size(); i++) {
            ReviewsResponse.Review item = mReviews.get(i);
            ContentValues value = new ContentValues();
            value.put(MovieProvider.ReviewContract.MOVIE_ID, mMovie.id);
            value.put(MovieProvider.ReviewContract.AUTHOR, item.author);
            value.put(MovieProvider.ReviewContract.CONTENT, item.content);
            values[i] = value;
        }
        new MovieDetailsStoreAsyncTask(getActivity(),
                MovieProvider.ReviewContract.CONTENT_URI, mMovie.id)
                .execute(values);
    }

    /**
     * Loader callbacks
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == TRAILER_LOADER_ID) {
            return new TrailerListLoader(getActivity(), mMovie.id);
        } else {
            return new ReviewListLoader(getActivity(), mMovie.id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, ArrayList data) {
        if (loader.getId() == TRAILER_LOADER_ID) {
            mTrailers = data;
            populateTrailers();
        } else {
            mReviews = data;
            populateReviews();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    public interface DetailsActionListener {

        void onFavoriteAction(long movieId);
    }

    class ReviewViewHolder {

        @InjectView(R.id.avatar_view)
        ImageView avatarImageView;

        @InjectView(R.id.name_text_view)
        TextView nameTextView;

        @InjectView(R.id.content_text_view)
        TextView contentTextView;

        public ReviewViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
