package com.example.android.popularmovieapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.android.popularmovieapplication.api.MovieResponse;
import com.example.android.popularmovieapplication.fragments.MovieDetailFragment;
import com.example.android.popularmovieapplication.fragments.MoviesFragment;

import butterknife.InjectView;
import butterknife.Optional;

public class MainActivity extends AppCompatActivity implements MoviesFragment.ListActionListener,
        MovieDetailFragment.DetailsActionListener {

    private MoviesFragment mMoviesFragment;

    private boolean isDualPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String param = null;
        switch (item.getItemId()) {
            case R.id.sort_order_popularity:
                param = getString(R.string.sort_order_popularity);
                break;
            case R.id.sort_order_rating:
                param = getString(R.string.sort_order_rating);
                break;
            case R.id.sort_order_favorites:
                param = getString(R.string.sort_order_favorites);
                break;
        }
        if (param != null) {
            item.setChecked(true);
            mMoviesFragment.setSortOrder(param);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFavoriteAction(long movieId) {
        mMoviesFragment.favoriteListChanged(movieId);
    }

    @Override
    public void onMovieSelected(MovieResponse.Movie movie, boolean isFavorite) {
        if (!isDualPane) {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(MovieDetailFragment.MOVIE, movie);
            startActivity(intent);
        }
    }

    @Override
    public void onEmptyMovieList() {

    }
}
