package com.example.android.popularmovieapplication.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.popularmovieapplication.R;
import com.example.android.popularmovieapplication.api.MovieResponse;
import com.example.android.popularmovieapplication.api.RetrofitAdapter;
import com.example.android.popularmovieapplication.api.TmdbService;

import java.util.ArrayList;
import java.util.StringTokenizer;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Object> {

    private final static String ARG_ITEMS = "items";
    private final static String ARG_SORT_ORDER = "sort_order";
    private final static String ARG_VIEW_STATE = "view_state";
    private final static String ARG_SELECTED_ITEM = "selected_item";

    private final static int VIEW_STATE_LOADING = 0;
    private final static int VIEW_STATE_ERROR = 1;
    private final static int VIEW_STATE_EMPTY = 2;
    private final static int VIEW_STATE_RESULTS = 3;

    private final static int LOADER_ID = 1;

    private TextView mErrorTextView;
    private Button mRetryButton;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private String mSortOrder;
    private ListActionListener mActionListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mActionListener = (ListActionListener) activity;
        } catch (ClassCastException e) {
            Log.e(this.getClass().getName(), "Activity must implement " + ListActionListener.class.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean reloadData = (mSortOrder != null && !mSortOrder.equalsIgnoreCase(getSortParam()));
        if (reloadData) {
            loadData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movies, container, false);
        mErrorTextView = (TextView) v.findViewById(R.id.error_text_view);
        mRetryButton = (Button) v.findViewById(R.id.retry_button);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retry();
            }
        });
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 4 : 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        mAdapter = new MovieAdapter(getActivity(), mActionListener);
        mRecyclerView.setAdapter(mAdapter);
        if (savedInstanceState == null) {
            loadData();
        } else {
            mSortOrder = savedInstanceState.getString(ARG_SORT_ORDER);
            if (!mSortOrder.equalsIgnoreCase(getSortParam())) {
                loadData();
            }
            int state = savedInstanceState.getInt(ARG_VIEW_STATE, VIEW_STATE_ERROR);
            switch (state) {
                case VIEW_STATE_ERROR:
                    showErrorViews();
                    break;
                case VIEW_STATE_RESULTS:
                    int selectedPosition = savedInstanceState.getInt(ARG_SELECTED_ITEM, 0);
                    ArrayList<MovieResponse.Movie> items = savedInstanceState.getParcelableArrayList(ARG_ITEMS);
                    mAdapter.mSelectedPosition = selectedPosition;
                    mAdapter.setItems(items);
                    showResultViews();
                    break;
                default:
                    showLoadingViews();
                    break;
            }
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int state = VIEW_STATE_RESULTS;
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_LOADING;
        } else if (mErrorTextView.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_ERROR;
        }
        outState.putInt(ARG_VIEW_STATE, state);
        outState.putParcelableArrayList(ARG_ITEMS, mAdapter.getItems());
        outState.putInt(ARG_SELECTED_ITEM, mAdapter.mSelectedPosition);
        outState.putString(ARG_SORT_ORDER, mSortOrder);
        super.onSaveInstanceState(outState);
    }

    /**
     * Changes sort order, stores selected param to SharedPreferences, reloads fragment data.
     *
     * @param sortOrder sortBy param
     */
    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
        mAdapter.mSelectedPosition = 0;
        populateData();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.prefs_sort_order), mSortOrder);
        editor.apply();
    }

    public void favoriteListChanged(long movieId) {
        if (mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            // TODO: remove/insert by one item
            populateData();
        }
    }

    /** Returns favorites list. */
    private ArrayList<Long> loadFavorites() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String favoritesString = prefs.getString(getString(R.string.prefs_favorites), "");
        ArrayList<Long> list = new ArrayList<>();
        if (favoritesString.length() > 0) {
            StringTokenizer st = new StringTokenizer(favoritesString, ",");
            while (st.hasMoreTokens()) {
                list.add(Long.parseLong(st.nextToken()));
            }
        }
        if (mAdapter != null) {
            mAdapter.setFavorites(list);
        }
        return list;
    }

    /** Loads data from server or from db. */
    private void populateData() {
        // load from db or from server
        if (mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            showLoadingViews();
            getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
            loadFavorites();
            loadData();
        }
    }

    // Loads movie list
    private void loadData() {
        showLoadingViews();
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        mSortOrder = getSortParam();
        service.getMovieList(mSortOrder, new Callback<MovieResponse>() {
            @Override
            public void success(MovieResponse movieResponse, Response response) {
                showResultViews();
                mAdapter.setItems(movieResponse.results);
            }

            @Override
            public void failure(RetrofitError error) { showErrorViews(); }
        });
    }

    // Retry to reload movie list
    private void retry() { loadData(); }

    // Returns default(popularity) or set by user sortBy param.
    private String getSortParam() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue = getString(R.string.sort_order_popularity);
        return prefs.getString("sort_order", defaultValue);
    }

    // Helper method to hide all elements, except progress bar.
    private void showLoadingViews() {
        mProgressBar.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    // Helper method to hide all elements, except error views.
    private void showErrorViews() {
        mErrorTextView.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    // Helper method to hide all elements, except recycler view.
    private void showResultViews() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.GONE);
        mRetryButton.setVisibility(View.GONE);
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }

    // Movies RecyclerView adapter class.
    private static class MovieAdapter extends RecyclerView.Adapter<MovieViewHolder> {

        final private Context mContext;

        private ArrayList<MovieResponse.Movie> mItems;
        private ArrayList<Long> mFavorites;

        final private ListActionListener mActionListener;

        private int mSelectedPosition;

        public MovieAdapter(Context context, ListActionListener listener) {
            mContext = context;
            mActionListener = listener;
            mItems = new ArrayList<>();
        }

        @Override
        public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.movie_item, parent, false);
            return new MovieViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MovieViewHolder holder, final int position) {
            Glide.with(mContext)
                    .load(mItems.get(position).getPosterUrl())
                    .centerCrop()
                    .placeholder(R.drawable.movie_placeholder)
                    .crossFade()
                    .into(holder.mPosterView);
            holder.mPosterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectItem(position);
                }
            });
        }

        @Override
        public int getItemCount() { return mItems.size(); }

        public void setFavorites(ArrayList<Long> favorites) {
            mFavorites = favorites;
        }

        public void setItems(ArrayList<MovieResponse.Movie> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        private void selectItem(int position) {
            int prevPosition = mSelectedPosition;
            mSelectedPosition = position;
            notifyItemChanged(position);
            notifyItemChanged(prevPosition);
            MovieResponse.Movie movie = mItems.get(position);
            boolean isFavorite = (mFavorites != null && mFavorites.contains(movie.id));
            mActionListener.onMovieSelected(movie, isFavorite);
        }

        public ArrayList<MovieResponse.Movie> getItems() { return mItems; }
    }

    // Movie view holder class
    private static class MovieViewHolder extends RecyclerView.ViewHolder {
        final public ImageView mPosterView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            this.mPosterView = (ImageView) itemView.findViewById(R.id.poster_image_view);
        }
    }

    // Movie list action listener
    public interface ListActionListener {
        void onMovieSelected(MovieResponse.Movie movie, boolean isFavorite);

        void onEmptyMovieList();
    }
}
