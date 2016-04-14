package com.example.android.popularmovieapplication.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Trailers response data model
 */
public class TrailersResponse {

    public ArrayList<Trailer> results;

    public static class Trailer implements Parcelable{

        public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
            @Override
            public Trailer createFromParcel(Parcel in) {
                return new Trailer(in);
            }

            @Override
            public Trailer[] newArray(int size) {
                return new Trailer[size];
            }
        };

        public String key;

        public String name;

        public Trailer() {
        }

        protected Trailer(Parcel in) {
            key = in.readString();
            name = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(key);
            out.writeString(name);
        }

        //Helper method to build YouTube link
        public String getYoutubeLink() {
            return "http://www.youtube.com/watch?v=" + this.key;
        }
    }
}
