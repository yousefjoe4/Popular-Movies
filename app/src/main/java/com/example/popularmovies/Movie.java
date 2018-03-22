package com.example.popularmovies;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;


public class Movie implements Parcelable {
    long id;
    String originalTitle;
    String releaseDate;
    String overview;
    int voteAverage;
    Uri imageUri;
    List<String> trailers;
    List<String> reviews;

    public Movie(long id, String originalTitle, String releaseDate, String overview, int voteAverage, Uri imageUri) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.imageUri = imageUri;
    }


    private Movie(Parcel in) {
        id = in.readLong();
        originalTitle = in.readString();
        releaseDate = in.readString();
        overview = in.readString();
        voteAverage = in.readInt();
        imageUri = Uri.parse(in.readString());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(originalTitle);
        parcel.writeString(releaseDate);
        parcel.writeString(overview);
        parcel.writeInt(voteAverage);
        parcel.writeString(imageUri.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final static Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };
}
