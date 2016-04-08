package com.example.user.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.user.spotifystreamer.MoviesFragment.Callback;

public class MainActivity extends AppCompatActivity implements Callback {
    public static boolean mtwoPane;
    private static final String DETAILFRAGMENT_TAG="DFTAG";
    public static String mSort_by;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.details_container) !=null){
            Log.v("main","twopane");
            mtwoPane=true;
            //Add detail fragment dynamically in case of 2 pane layout
            if(savedInstanceState==null){
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.details_container, new DetailActivityFragment(),DETAILFRAGMENT_TAG).
                        commit();
            }
        }
        else
            mtwoPane=false;

    }

    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String sort_by = prefs.getString(getString(R.string.pref_general_key), getString(R.string.popularity));
        if(mSort_by!=null && !sort_by.equals(mSort_by)){
            MoviesFragment mf=(MoviesFragment)getSupportFragmentManager().
                    findFragmentById(R.id.movies_fragment);
            if(mf!=null)
                mf.onPreferenceChanged(sort_by);
        }
        mSort_by=sort_by;
    }


    @Override
    public void onItemSelected(Movie movie) {
        if(mtwoPane){
            //In two pane mode, show the detail view in this activity by
            //adding or replacing the detail fragment using fragment transaction
            Bundle args=new Bundle();
            args.putParcelable(DetailActivityFragment.MOVIE_DETAIL,movie);
            DetailActivityFragment fragment=new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_container,fragment,DETAILFRAGMENT_TAG)
                    .commit();
        }else {
            Intent intent = new Intent(this,DetailActivity.class);
            intent.putExtra("MOVIE", movie);
            startActivity(intent);
        }


    }
}
