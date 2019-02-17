package com.sulitous.biti.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.sulitous.biti.popularmovies.data.MovieContract.MovieEntry;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 0;
    private MoviesCursorAdaptor moviesCursorAdaptor;
    private static final String SELECTED_KEY = "selectedKey";
    private static final String CURRENTVIEW_KEY = "currentView";
    private int mPosition;
    Callback mCallback;
    private int position = -1;
    private GridView gridView;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        moviesCursorAdaptor = new MoviesCursorAdaptor(getActivity(), null, 0, CURSOR_LOADER_ID);
        gridView = (GridView) rootView.findViewById(R.id.gridView_tiles);
        gridView.setColumnWidth(Constants.POSTER_WIDTH);
        gridView.setAdapter(moviesCursorAdaptor);
        gridView.setEmptyView(rootView.findViewById(R.id.imageView_empty_grid));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null && cursor.moveToPosition(position)) {
                    Long idLong = cursor.getLong(cursor.getColumnIndex("_id"));

                    Uri destUri = MovieEntry.buildMovieUri(idLong);
                    mCallback.onItemSelected(destUri);
                }
                mPosition = position;
            }
        });
        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        if(savedInstanceState != null && savedInstanceState.containsKey(CURRENTVIEW_KEY)){
            position = savedInstanceState.getInt(CURRENTVIEW_KEY);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);
        MenuItem item = menu.findItem(R.id.action_favorite);
        if(Utility.getPrefSelected(getActivity()) == Constants.PREF_FAVORITE){
            item.setIcon(R.drawable.ic_favorite_black_24dp);
        }else{
            item.setIcon(R.drawable.ic_favorite_border_black_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            if(Utility.getPrefSelected(getActivity()) == Constants.PREF_FAVORITE){
                item.setIcon(R.drawable.ic_favorite_border_black_24dp);
                Utility.putPrefSelected(getActivity(), 1);
            }else{
                item.setIcon(R.drawable.ic_favorite_black_24dp);
                Utility.putPrefSelected(getActivity(), Constants.PREF_FAVORITE);
            }
            onPreferenceChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        onPreferenceChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != GridView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        position = gridView.getFirstVisiblePosition();
        outState.putInt(CURRENTVIEW_KEY,position);
        super.onSaveInstanceState(outState);
    }

    void onPreferenceChanged(){
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    interface Callback{
        void onItemSelected(Uri movieUri);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        int sortBy = Utility.getPrefSelected(getActivity());
        String sortOrder;
        String selection = null;
        switch (sortBy){
            case Constants.PREF_HIGH_RATED:
                sortOrder = MovieEntry.COLUMN_USER_RATING + " DESC";
                break;
            case Constants.PREF_FAVORITE:
                selection = MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_FAVORITE + " = 1 ";
            case Constants.PREF_MOST_POPULAR:
                sortOrder = MovieEntry.COLUMN_RELEASE_DATE + " DESC";
                break;
            default:
                sortOrder = null;

        }
        return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI, null,selection,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        moviesCursorAdaptor.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION){
            gridView.smoothScrollToPosition(mPosition);
        }
        if (position != -1) {
            gridView.smoothScrollToPosition(position);
        }
        // perform click on first item to show its details by default
        if(getActivity().getSupportFragmentManager().findFragmentByTag(Constants.DETAILFRAGMENT_TAG) != null && data.moveToFirst()){
            new Handler().post(new Runnable() {
                public void run() {
                    gridView.performItemClick(gridView.getAdapter().getView(0, null, null), 0, gridView.getAdapter().getItemId(0));
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        moviesCursorAdaptor.swapCursor(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;
        if(context instanceof Activity){
            a= (Activity) context;
            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try{
                mCallback = (Callback) a;
            }catch (ClassCastException e){
                throw new ClassCastException(a.toString()
                        + " must implement Callback interface");

            }

        }
    }
}
