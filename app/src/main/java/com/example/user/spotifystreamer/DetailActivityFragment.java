package com.example.user.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    public DetailActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent=getActivity().getIntent();
        if(intent!=null) {
            if (intent.hasExtra("IMAGE")) {
                String title = intent.getStringExtra("IMAGE");
                ImageView imageView=(ImageView)rootView.findViewById(R.id.thumbnail);
                imageView.setAdjustViewBounds(true);
                Picasso.with(getActivity()).load(title).resize(780,350).into(imageView);
            }
            if (intent.hasExtra("TITLE")) {
                String title = intent.getStringExtra("TITLE");
                ((TextView) rootView.findViewById(R.id.title_textview)).setText(title);
            }
            if (intent.hasExtra("PLOT")) {
                String plot = intent.getStringExtra("PLOT");
                if(!plot.equals(""))
                ((TextView) rootView.findViewById(R.id.plot_textview)).setText("Overview:\n"+plot);
            }
            if (intent.hasExtra("RATING")) {
                String title = intent.getStringExtra("RATING");
                ((TextView) rootView.findViewById(R.id.user_rating)).setText("User Rating:\n"+title);
            }
            if (intent.hasExtra("release-date")) {
                String title = intent.getStringExtra("release-date");
                ((TextView) rootView.findViewById(R.id.r_date_textview)).setText("Release Date:\n" + title);
            }
        }
        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(),SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
