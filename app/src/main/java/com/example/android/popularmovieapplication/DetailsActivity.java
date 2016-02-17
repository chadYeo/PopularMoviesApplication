package com.example.android.popularmovieapplication;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.android.popularmovieapplication.api.MovieResponse;
import com.example.android.popularmovieapplication.fragments.MovieDetailFragment;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        MovieResponse.Movie movie;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movie = extras.getParcelable(MovieDetailFragment.ARG_MOVIE);
        } else {
            throw new NullPointerException("No movie found in intent extras");
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(movie.title);
        }

        MovieDetailFragment fragment = MovieDetailFragment.getInstance(movie);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
