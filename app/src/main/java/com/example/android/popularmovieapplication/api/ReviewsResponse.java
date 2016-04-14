package com.example.android.popularmovieapplication.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Reviews response data model
 */
public class ReviewsResponse {

    public ArrayList<Review> results;

    public static class Review implements Parcelable{

        public static final Creator<Review> CREATOR = new Creator<Review>() {
            @Override
            public Review createFromParcel(Parcel in) {
                return new Review(in);
            }

            @Override
            public Review[] newArray(int size) {
                return new Review[size];
            }
        };

        public String author;
        public String content;

        public Review() {
        }

        protected Review(Parcel in) {
            author = in.readString();
            content = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(author);
            out.writeString(content);
        }
    }
}
