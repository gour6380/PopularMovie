package com.sulitous.biti.popularmovies;

final class Constants {

    public Constants(){}

    static final String IMG_BASE_URL = "http://image.tmdb.org/t/p/";
    static final String API_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
    static final String META_BASE_URL = "http://api.themoviedb.org/3/movie/";
    static final int POSTER_WIDTH = 500;
    private static final int BACKDROP_WIDTH = 780;
    static final String DEFAULT_POSTER_WIDTH = "w" + POSTER_WIDTH;
    static final String DEFAULT_BACKDROP_WIDTH = "w" + BACKDROP_WIDTH;

    static final String VALID_TILL = "validTill";

    static final int FETCH_METAINFO = 1;
    static final int FETCH_VIDEOS = 2;
    static final int FETCH_REVIEWS = 3;

    static final String MOVIE_SHARE_HASHTAG = "#PopularMovies";

    static final int PREF_HIGH_RATED = 0;
    static final int PREF_MOST_POPULAR = 1;
    static final int PREF_FAVORITE = 2;

    static final String DETAILFRAG_KEY = "mUri";
    static final String DETAILFRAGMENT_TAG = "DFTAG";
}
