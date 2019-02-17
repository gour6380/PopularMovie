package com.sulitous.biti.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.sulitous.biti.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Movie implements Parcelable{
    private String poster_path;
    private Boolean adult;
    private String overview;
    private String release_date;
    private int[] genre_ids;
    private String id;
    String original_title;
    private String backdrop_path;
    private Double vote_average;
    private Boolean favorite = false;

    Movie(JSONObject json) throws JSONException {
        this.poster_path = json.getString("poster_path");
        this.adult = json.getBoolean("adult");
        this.overview = json.getString("overview");
        this.release_date =json.getString("release_date");
        this.genre_ids = formatGenre(json.getJSONArray("genre_ids"));
        this.id = json.getString("id");
        this.original_title = json.getString("original_title");
        this.backdrop_path = json.getString("backdrop_path");
        this.vote_average = json.getDouble("vote_average");
    }

    Movie(Cursor cursor){
        int posterIndex = cursor.getColumnIndex(MovieEntry.COLUMN_POSTER);
        this.poster_path = cursor.getString(posterIndex);

        int overviewIndex = cursor.getColumnIndex(MovieEntry.COLUMN_SYNOPSIS);
        this.overview = cursor.getString(overviewIndex);

        int dateIndex = cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE);
        this.release_date = cursor.getString(dateIndex);

        int titleIndex = cursor.getColumnIndex(MovieEntry.COLUMN_TITLE);
        this.original_title = cursor.getString(titleIndex);

        int ratingIndex = cursor.getColumnIndex(MovieEntry.COLUMN_USER_RATING);
        this.vote_average = cursor.getDouble(ratingIndex);

        int favoriteIndex = cursor.getColumnIndex(MovieEntry.COLUMN_FAVORITE);
        this.favorite = cursor.getInt(favoriteIndex) > 0;
    }

    private Movie(Parcel in) {
        poster_path = in.readString();
        adult = in.readByte() != 0;
        overview = in.readString();
        release_date = in.readString();
        genre_ids = in.createIntArray();
        id = in.readString();
        original_title = in.readString();
        backdrop_path = in.readString();
        vote_average = in.readDouble();
    }

    private int[] formatGenre(JSONArray genreArray){
        if(genreArray == null){
            return null;
        }
        else{
            int[] numbers = new int[genreArray.length()];
            for(int i=0; i < genreArray.length();i++){
                numbers[i] = genreArray.optInt(i);
            }
            return numbers;
        }
    }

    private Uri getPoster_path(String size){
        return Utility.getPoster_path(size, this.poster_path);

    }

    private Uri getBackdrop_path(String size){
        String path = Constants.IMG_BASE_URL + size + this.backdrop_path;
        return Uri.parse(path);
    }

    Uri getPoster_path(){
        return getPoster_path(Constants.DEFAULT_POSTER_WIDTH);
    }

    public String getYear(){
        return Utility.getYear(this.release_date);
    }

    ContentValues packToContentValues(){
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieEntry.COLUMN_TITLE, original_title);
        movieValues.put(MovieEntry.COLUMN_SYNOPSIS, overview);
        movieValues.put(MovieEntry.COLUMN_USER_RATING, vote_average);
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, release_date);
        movieValues.put(MovieEntry.COLUMN_FAVORITE, false);
        movieValues.put(MovieEntry.COLUMN_POSTER, poster_path);
        movieValues.put(MovieEntry._ID, id);
        return movieValues;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(poster_path);
        dest.writeString(overview);
        dest.writeString(release_date);
        dest.writeString(id);
        dest.writeString(original_title);
        dest.writeDouble(vote_average);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>(){

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}