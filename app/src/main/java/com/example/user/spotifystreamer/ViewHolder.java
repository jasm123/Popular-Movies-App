package com.example.user.spotifystreamer;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * ViewHolder for Details fragment class.
 */
class ViewHolder {
    public final ListView review_list ;
    public final ListView trailer_list ;
    public final ImageView imageView ;
    public final TextView plotView;
    public final TextView titleView;
    public final RatingBar rating;
    public final TextView dateView;


    public ViewHolder(View rootView){
        review_list=(ListView)rootView.findViewById(R.id.reviews_list);
        trailer_list=(ListView) rootView.findViewById(R.id.trailer_list);
        imageView = (ImageView) rootView.findViewById(R.id.thumbnail);
        titleView=(TextView) rootView.findViewById(R.id.title_textview);
        plotView=(TextView) rootView.findViewById(R.id.plot_textview);
        rating=(RatingBar) rootView.findViewById(R.id.ratingbar);
        dateView=(TextView) rootView.findViewById(R.id.r_date_textview);

    }
}
