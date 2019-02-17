package com.sulitous.biti.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.linearlistview.LinearListView;
import com.sulitous.biti.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

class ApiTask extends AsyncTask<String, Void, String> {

    private Context context;
    private TextView textView_runtime;
    private Boolean isMetaRequired;
    private int type;
    private LinearListView linearListView;

    interface AsyncResponse{
        void processFinish(String result);
    }

    private AsyncResponse delegate = null;

    ApiTask(Context context, TextView runtime){
        this.context = context;
        this.textView_runtime = runtime;
        this.isMetaRequired = true;
        this.type = Constants.FETCH_METAINFO;
    }

    //constructor to fetch movies list
    ApiTask(Context context){
        this.context = context;
        this.textView_runtime = null;
        this.isMetaRequired = false;
    }

    private ApiTask(Context context, LinearListView linearListView, int type) {
        this.context = context;
        this.textView_runtime = null;
        this.isMetaRequired = true;
        this.type = type;
        this.linearListView = linearListView;
    }

    ApiTask(Context context, LinearListView linearListView, int type, AsyncResponse delegate) {
        this(context, linearListView, type);
        this.delegate = delegate;
    }

    private Uri buildUri(String param){
        final String APIKEY = context.getResources().getString(R.string.TMDbAPIKEY);
        if(isMetaRequired){
            switch (type) {
                case Constants.FETCH_METAINFO:
                    return Uri.parse(Constants.META_BASE_URL + param).buildUpon()
                            .appendQueryParameter("api_key", APIKEY)
                            .build();
                case Constants.FETCH_VIDEOS:
                    return Uri.parse(Constants.META_BASE_URL + param).buildUpon()
                            .appendPath("videos")
                            .appendQueryParameter("api_key", APIKEY)
                            .build();
                case Constants.FETCH_REVIEWS:
                    return Uri.parse(Constants.META_BASE_URL + param).buildUpon()
                            .appendPath("reviews")
                            .appendQueryParameter("api_key", APIKEY)
                            .build();
                default:
                    return null;
            }
        }else{
            return Uri.parse(Constants.API_BASE_URL).buildUpon()
                    .appendQueryParameter("sort_by", Utility.getSortStringPath(param))
                    .appendQueryParameter("api_key", APIKEY)
                    .build();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        if(params.length == 0){
            return null;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonstr = null;
        try {

            Uri uri = buildUri(params[0]);

            //check if no uri is returned
            if(uri == null){
                return null;
            }

            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }

            if(buffer.length() == 0){
                return null;
            }
            movieJsonstr = buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(isMetaRequired){
            return movieJsonstr;
        }else{
            try {
                storeMoviesFromJson(movieJsonstr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if(s != null) {
            if (isMetaRequired) {
                switch (type) {
                    case Constants.FETCH_METAINFO:
                        try {
                            JSONObject metaJson = new JSONObject(s);
                            String runtime = metaJson.getString("runtime");

                            if (textView_runtime != null) {
                                textView_runtime.setText(runtime + "min");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constants.FETCH_VIDEOS:
                        try {
                            JSONObject metaJson = new JSONObject(s);
                            JSONArray results = metaJson.getJSONArray("results");
                            ArrayList<HashMap<String , String>> trailers = new ArrayList<>();
                            for (int i = 0; i < results.length(); i++) {
                                HashMap<String, String> trailer = new HashMap<>();
                                JSONObject obj =  results.getJSONObject(i);
                                String key = obj.getString("key");
                                String name = obj.getString("name");
                                trailer.put("name", name);
                                trailer.put("key", key);
                                trailers.add(trailer);
                            }
                            if(trailers.size() > 0){
                                delegate.processFinish(trailers.get(0).get("key"));
                            } else{
                                delegate.processFinish(null);
                            }

                            SimpleAdapter listAdapter = new SimpleAdapter(context, trailers, R.layout.list_item_trailer,
                                    new String[]{"name", "key"},
                                    new int[]{R.id.textView_listitem_trailer_title, R.id.textView_trailer_key} );
                            linearListView.setAdapter(listAdapter);
                            linearListView.setOnItemClickListener(new LinearListView.OnItemClickListener() {
                                @Override
                                public void onItemClick(LinearListView parent, View view, int position, long id) {
                                    String selected = ((TextView) view.findViewById(R.id.textView_trailer_key)).getText().toString();
                                    Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+selected));
                                    context.startActivity(youtubeIntent);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constants.FETCH_REVIEWS:
                        try {
                            JSONObject metaJson = new JSONObject(s);
                            JSONArray results = metaJson.getJSONArray("results");
                            ArrayList<HashMap<String , String>> reviews = new ArrayList<>();
                            delegate.processFinish(null);
                            for (int i = 0; i < results.length(); i++) {
                                HashMap<String, String> review = new HashMap<>();
                                JSONObject obj =  results.getJSONObject(i);
                                String author = obj.getString("author");
                                String content = obj.getString("content");
                                review.put("author", author);
                                review.put("content", content);
                                reviews.add(review);
                            }
                            SimpleAdapter adapter = new SimpleAdapter(context,reviews, R.layout.list_item_review, new String[]{"author", "content"}, new int[]{R.id.textView_author, R.id.textView_review});
                            linearListView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }
        }
    }

    private void storeMoviesFromJson(String popularJsonStr) throws JSONException {
        if(popularJsonStr == null){
            return ;
        }
        final String RESULTS = "results";
        JSONObject receivedJson = new JSONObject(popularJsonStr);
        JSONArray receivedMoviesArray = receivedJson.getJSONArray(RESULTS);

        ArrayList<Movie> movies = new ArrayList<>();

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<>(receivedMoviesArray.length());

        for(int i = 0; i < receivedMoviesArray.length(); i++ ){
            JSONObject movieJson = receivedMoviesArray.getJSONObject(i);
//            movies.add(i, new Movie(movieJson));
            cVVector.add(new Movie(movieJson).packToContentValues());
        }
        int inserted = 0;
        //add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = context.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
        }

    }
}
