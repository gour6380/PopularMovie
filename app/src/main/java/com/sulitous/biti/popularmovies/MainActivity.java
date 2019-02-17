package com.sulitous.biti.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;

import com.sulitous.biti.popularmovies.data.MovieContract;

import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateTiles();
        if(findViewById(R.id.movie_detail_container) != null){

            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment(), Constants.DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private  void updateTiles(){
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final long bestBefore = settings.getLong(Constants.VALID_TILL, 0L);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (new Date().after(new Date(bestBefore))) {
                    if (Utility.hasActiveInternetConnection(getApplicationContext())) {
                        getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
                        new ApiTask(getApplicationContext()).execute(String.valueOf(Constants.PREF_HIGH_RATED));
                        new ApiTask(getApplicationContext()).execute(String.valueOf(Constants.PREF_MOST_POPULAR));
                        settings.edit().putLong(Constants.VALID_TILL, new Date().getTime() + 86400000L).apply();
                    }
                }
            }
        });

    }

    @Override
    public void onItemSelected(Uri movieUri) {
        DetailActivityFragment daf = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(Constants.DETAILFRAGMENT_TAG);
        if(daf != null){
            Bundle args = new Bundle();
            args.putParcelable(Constants.DETAILFRAG_KEY, movieUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, Constants.DETAILFRAGMENT_TAG)
                    .commit();

        }else{
            Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
            detailIntent.setData(movieUri);
            startActivity(detailIntent);
        }
    }

}
