package com.example.user.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * MovieReview class stores information about the reviews of each movie(author,review,url ,etc.).
 */
public class MovieReview implements Parcelable {
    public String author;
    public String review;
    public String url;
    public MovieReview(){
    }
    private MovieReview(Parcel in){
        author=in.readString();
        review=in.readString();
        url=in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(review);
        dest.writeString(url);
    }
    public static final Parcelable.Creator<MovieReview> CREATOR=new Parcelable.Creator<MovieReview>(){
        public MovieReview createFromParcel(Parcel parcel){
            return new MovieReview(parcel);
        }
        public MovieReview[] newArray(int i){
            return new MovieReview[i];
        }
    };
}
