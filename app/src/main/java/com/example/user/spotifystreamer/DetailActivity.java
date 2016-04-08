package com.example.user.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class DetailActivity extends AppCompatActivity {
    private final static String TAG="DetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState==null){
            Bundle args=new Bundle();
            if(getIntent().getParcelableExtra("MOVIE")==null)
                Log.v(TAG,"intent is null");
            else
                Log.v(TAG, "intent is not null");
            args.putParcelable(DetailActivityFragment.MOVIE_DETAIL,
                    (getIntent().getParcelableExtra("MOVIE")));
            DetailActivityFragment fragment=new DetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_container,fragment)
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().
                    add(R.id.details_container, new DetailActivityFragment()).commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
