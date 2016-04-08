package com.example.user.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * MovieTrailer class has all the information about the movie trailers.
 * It has a single member but it is made so that it can be parceled.
 */
public class MovieTrailer implements Parcelable {
    public String key;
    public MovieTrailer(){
    }
    private MovieTrailer(Parcel in) {
        key = in.readString();
    }

    public static final Creator<MovieTrailer> CREATOR = new Creator<MovieTrailer>() {
        @Override
        public MovieTrailer createFromParcel(Parcel in) {
            return new MovieTrailer(in);
        }

        @Override
        public MovieTrailer[] newArray(int size) {
            return new MovieTrailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
    }
}
