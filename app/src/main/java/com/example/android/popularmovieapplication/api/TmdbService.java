package com.example.android.popularmovieapplication.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by ChadYeo on 2/15/16.
 */
public interface TmdbService {
    // Example: /discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
    @GET("/discover/movie")
    void getMovieList(@Query("sort_by") String sortBy, Callback<MovieResponse> callback);
}
