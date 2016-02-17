package com.example.android.popularmovieapplication.api;

import com.example.android.popularmovieapplication.BuildConfig;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by ChadYeo on 2/15/16.
 */
public class RetrofitAdapter {
    private final static String BASE_URL = "http://api.themoviedb.org/3/";
    private final static String API_KEY = "/** YOUR API KEY HERE*/";
    private static RestAdapter mRestAdapter;

    public static RestAdapter getRestAdapter() {
        if (mRestAdapter == null) {
            mRestAdapter = new RestAdapter.Builder()
              .setEndpoint(BASE_URL)
                    .setRequestInterceptor(mRequestInterceptor)
                    .build();
            if (BuildConfig.DEBUG) {
                mRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
            }
        }
        return mRestAdapter;
    }

    private static RequestInterceptor mRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addQueryParam("api_key", API_KEY);
        }
    };
}
