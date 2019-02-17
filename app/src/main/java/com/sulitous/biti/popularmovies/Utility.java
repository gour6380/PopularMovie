package com.sulitous.biti.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class Utility {

    static String getSortStringPath(String type){
        if(type == "0")
            return "vote_average.desc";
        else{
            return "popularity.desc";
        }
    }

    // method get preference selected
    static int getPrefSelected(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(settings.getString(context.getString(R.string.pref_sort_list_key), "1"));
    }

    static void putPrefSelected(Context context, int num){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(context.getString(R.string.pref_sort_list_key), String.valueOf(num)).apply();
    }

    static String getYear(String date){
        String str[] = date.split("-");
        return str[0];
    }

    static Uri getPoster_path(String size, String poster_path){
        String path = Constants.IMG_BASE_URL + size + poster_path;
        return Uri.parse(path);

    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    static boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException ignored) {
            }
        } else {

        }
        return false;
    }
}
