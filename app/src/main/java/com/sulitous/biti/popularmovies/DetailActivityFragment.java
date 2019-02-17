package com.sulitous.biti.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linearlistview.LinearListView;
import com.squareup.picasso.Picasso;
import com.sulitous.biti.popularmovies.data.MovieContract.MovieEntry;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG =  DetailActivityFragment.class.getSimpleName();
    private Uri mUri;
    private String title = "Movie";
    private ShareActionProvider mShareActionProvider;

    @BindView(R.id.imageView_poster_detail)
    ImageView imageView_poster_detail;
    @BindView(R.id.textView_release_year)
    TextView textView_release_year;
    @BindView(R.id.textView_vote_average) TextView textView_vote_average;
    @BindView(R.id.textView_overview) TextView textView_overview;
    @BindView(R.id.runtime_textView) TextView textView_runtime;
    @BindView(R.id.listView_trailers)
    LinearListView listView_trailers;
    @BindView(R.id.linearlist) LinearListView linearListView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.textView_empty_trailer_list) TextView textView_empty_trailer_list;
    @BindView(R.id.textView_empty_list_item) TextView textView_empty_list_item;

    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS =  {
            MovieEntry.TABLE_NAME +"."+ MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_POSTER,
            MovieEntry.COLUMN_USER_RATING,
            MovieEntry.COLUMN_SYNOPSIS,
            MovieEntry.COLUMN_FAVORITE
    };
    private FloatingActionButton myFab;

    public static final int COL_MOVIE_TITLE = 1;
    public static final int COL_MOVIE_DATE = 2;
    public static final int COL_MOVIE_POSTER = 3;
    public static final int COL_MOVIE_RATING = 4;
    public static final int COL_MOVIE_SYNOPSIS = 5;
    public static final int COL_MOVIE_FAV = 6;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myFab = (FloatingActionButton) getActivity().findViewById(R.id.detail_fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                if (myFab.isSelected()) {
                    cv.put(MovieEntry.COLUMN_FAVORITE, false);
                    Log.i(TAG, " fab clicked");
                } else {
                    cv.put(MovieEntry.COLUMN_FAVORITE, true);
                }
                if (cv.size() > 0) {
                    getLoaderManager().restartLoader(DETAIL_LOADER, null, DetailActivityFragment.this);
                }

            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        if(arguments != null){
            mUri = arguments.getParcelable(Constants.DETAILFRAG_KEY);
            assert mUri != null;
            Log.i(TAG, mUri.toString());
        }
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent createShareVideoIntent(String key) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String link;
        try {
            if(key != null){
                link =  "Checkout " + title + " movie trailer @ " + "http://www.youtube.com/watch?v="+ key +" ";
            } else{
                link =  "Checkout " + title + " movie ";
            }
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    link + Constants.MOVIE_SHARE_HASHTAG);
            return shareIntent;
        }catch (NullPointerException e){
            Log.e(TAG, "Unable to read title from activity");
        }

        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mUri != null){
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            long movieId = ContentUris.parseId(mUri);
            title = data.getString(COL_MOVIE_TITLE);
            new ApiTask(getActivity(), textView_runtime).execute(String.valueOf(movieId));

            new ApiTask(getActivity(), listView_trailers, Constants.FETCH_VIDEOS, new ApiTask.AsyncResponse() {
                @Override
                public void processFinish(String result) {
                    listView_trailers.setEmptyView(textView_empty_trailer_list);
                    if(mShareActionProvider != null){
                        Intent shareIntent =  createShareVideoIntent(result);
                        mShareActionProvider.setShareIntent(shareIntent);
                    }
                }
            }).execute(String.valueOf(movieId));

            new ApiTask(getActivity(), linearListView, Constants.FETCH_REVIEWS, new ApiTask.AsyncResponse() {
                @Override
                public void processFinish(String str) {
                    progressBar.setVisibility(View.GONE);
                    linearListView.setEmptyView(textView_empty_list_item);
                }
            }).execute(String.valueOf(movieId));


            if(getActivity().findViewById(R.id.gridView_tiles) == null){
                getActivity().setTitle(title);
            }

            String year = data.getString(COL_MOVIE_DATE);

            textView_release_year.setText(title + " \n\n" + Utility.getYear(year));

            String overview = data.getString(COL_MOVIE_SYNOPSIS);
            textView_overview.setText(overview);

            String rating = data.getString(COL_MOVIE_RATING);
            textView_vote_average.setText(rating + R.string.ten);

            String poster = data.getString(COL_MOVIE_POSTER);

            imageView_poster_detail.setContentDescription(title);
            Picasso.get()
                    .load(Utility.getPoster_path(Constants.DEFAULT_POSTER_WIDTH, poster))
                    .into(imageView_poster_detail);

            boolean fav = data.getInt(COL_MOVIE_FAV) > 0;
            if(fav){
                myFab.setSelected(true);
                myFab.setImageResource(R.drawable.ic_favorite_black_24dp);
            }else{
                myFab.setSelected(false);
                myFab.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

