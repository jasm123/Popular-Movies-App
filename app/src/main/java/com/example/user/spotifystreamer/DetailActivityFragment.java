package com.example.user.spotifystreamer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.spotifystreamer.data.MovieContract.MovieEntry;
import com.example.user.spotifystreamer.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private final String EXTRAINFO_URL = "http://api.themoviedb.org/3/movie/";//+id+videos?api_key=#
    private String movie_id;
    private ReviewAdapter mReviewAdapter;
    private ArrayList<MovieReview> reviews;
    private ArrayList<MovieTrailer> trailer_keys;
    private CustomAdapter mTrailerAdapter;
    private Movie movie;//the movie we received as an argument, whose details are to be displayed
    public static final String MOVIE_DETAIL = "movieDetails";

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!MainActivity.mtwoPane)
            setHasOptionsMenu(true);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(getContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("Reviews", reviews);
        outState.putParcelableArrayList("Trailers", trailer_keys);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (savedInstanceState == null || !savedInstanceState.containsKey("Reviews")
                || !savedInstanceState.containsKey("Trailers")) {
            reviews = new ArrayList<>();
            trailer_keys = new ArrayList<>();
        } else {
            Log.v("DETAIL ACTIVITY", "bundle received.");
            reviews = savedInstanceState.getParcelableArrayList("Reviews");
            trailer_keys = savedInstanceState.getParcelableArrayList("Trailers");
        }
        if (args != null) {
            movie = args.getParcelable(MOVIE_DETAIL);
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            mReviewAdapter = new ReviewAdapter(getActivity(), reviews);
            ViewHolder viewHolder = new ViewHolder(rootView);
            rootView.setTag(viewHolder);
            viewHolder = (ViewHolder) rootView.getTag();
            final FloatingActionButton fab=(FloatingActionButton)getActivity().findViewById(R.id.fab);
            viewHolder.review_list.setAdapter(mReviewAdapter);
            mTrailerAdapter = new CustomAdapter(getActivity(), trailer_keys);
            viewHolder.trailer_list.setAdapter(mTrailerAdapter);
            movie_id = movie.movie_id;
            if (savedInstanceState == null) {
                getReview();
                getTrailer();
            }
            viewHolder.imageView.setAdjustViewBounds(true);
            Picasso.with(getActivity()).load(movie.thumbnail).resize(780, 350).into(viewHolder.imageView);
            viewHolder.titleView.setText(movie.title);
            if (!(movie.plot).equals(""))
                viewHolder.plotView.setText(movie.plot);
            viewHolder.rating.setRating((Float.parseFloat(movie.user_rating)) / 2);
            viewHolder.dateView.setText(movie.release_date);
            viewHolder.trailer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent trailer_intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=" + trailer_keys.get(position)));
                    startActivity(trailer_intent);
                }
            });

            ScrollView parentScroll = (ScrollView) rootView;
            parentScroll.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    getActivity().findViewById(R.id.trailer_list).getParent()
                            .requestDisallowInterceptTouchEvent(false);
                    return false;
                }
            });
            viewHolder.review_list.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of
                    // child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            viewHolder.trailer_list.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of
                    // child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            MovieDbHelper dbHelper = new MovieDbHelper(getContext());
            if (dbHelper.hasObject(movie_id)) {
                fab.setImageResource(R.drawable.favorite2);
            }
            else{
                fab.setImageResource(R.drawable.favorite1);
            }
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MovieDbHelper dbHelper = new MovieDbHelper(getContext());
                    if (!dbHelper.hasObject(movie_id)) {
                        saveToDatabase();
                        Toast.makeText(getContext(), "Added to Favorite!", Toast.LENGTH_SHORT).show();
                        fab.setImageResource(R.drawable.favorite2);
                    } else {
                        removeDatabase();
                        Toast.makeText(getContext(), "Removed from Favorite!", Toast.LENGTH_SHORT).show();
                        fab.setImageResource(R.drawable.favorite1);
                    }
                }
            });
            return rootView;
        }
        else
            return null;
    }
    private void saveToDatabase(){
        DatabaseTask movieTask=new DatabaseTask();
        movieTask.execute("save");
    }
    private void removeDatabase(){
        DatabaseTask movieTask=new DatabaseTask();
        movieTask.execute("remove");
        if(MainActivity.mtwoPane && MainActivity.mSort_by.equals("favorites")){
            MoviesFragment mf = (MoviesFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.movies_fragment);
            mf.fetchFavorites();
        }
    }
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id==R.id.action_settings){
            startActivity(new Intent(getActivity(),SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getTrailer(){
        if(isNetworkAvailable()){
            FetchTrailerTask movieTask=new FetchTrailerTask();
            movieTask.execute();
        }else{
            Toast.makeText(getContext(),"No Connection!\nCheck your Internet Connection",Toast.LENGTH_LONG).show();
        }
    }

    private void getReview(){
        if(isNetworkAvailable()){
            FetchReviewTask movieTask=new FetchReviewTask();
            movieTask.execute();
        }else{
            Toast.makeText(getContext(),"No Connection!\nCheck your Internet Connection",Toast.LENGTH_LONG).show();
        }
    }

    class DatabaseTask extends AsyncTask<String, Void, Boolean> {
        final MovieDbHelper db = new MovieDbHelper(getContext());
        final SQLiteDatabase database = db.getWritableDatabase();

        @Override
        protected Boolean doInBackground(String... params) {
            if (params[0].equals("save")) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieEntry.COLUMN_MID, movie_id);
                contentValues.put(MovieEntry.COLUMN_TITLE, movie.title);
                contentValues.put(MovieEntry.COLUMN_POSTER, movie.poster_path);
                contentValues.put(MovieEntry.COLUMN_DATE, movie.release_date);
                contentValues.put(MovieEntry.COLUMN_OVERVIEW, movie.plot);
                contentValues.put(MovieEntry.COLUMN_RATING, movie.user_rating);
                contentValues.put(MovieEntry.COLUMN_BACKDROP, movie.thumbnail);
                long row_id = database.insert(MovieEntry.TABLE_NAME, null, contentValues);
                db.close();
                return row_id != -1;
            } else if (params[0].equals("remove")) {
                database.delete(MovieEntry.TABLE_NAME, MovieEntry.COLUMN_MID + "=?", new String[]{movie_id});
                db.close();
                return null;
            }
            return null;
        }
    }

    class FetchReviewTask extends AsyncTask<Void, Void, MovieReview[]> {
        String json_str = null;
        final String TAG = this.getClass().getSimpleName();

        @Override
        protected MovieReview[] doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            MovieReview[] objects = null;
            try {
                final String REVIEWS = "reviews";
                final String API_KEY = "api_key";
                Uri uri = Uri.parse(EXTRAINFO_URL + movie_id + "/" + REVIEWS).buildUpon().
                        appendQueryParameter(API_KEY, getActivity().getString(R.string.api_key))
                        .build();
                URL url = new URL(uri.toString());
                Log.v(TAG, url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                json_str = buffer.toString();
                Log.v(TAG,json_str);
            } catch (IOException e) {
                Log.e(TAG, "Error blah ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                objects = getReviewData(json_str);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error", e);
            }
            return objects;

        }

        private MovieReview[] getReviewData(String str) throws JSONException {
            final String MOVIEDB_RESULT = "results";
            final String MOVIEDB_CONTENT = "content";
            final String MOVIEDB_AUTHOR = "author";
            final String MOVIEDB_URL = "url";
            JSONObject jsonObject = new JSONObject(str);
            JSONArray movieArray = jsonObject.getJSONArray(MOVIEDB_RESULT);
            MovieReview[] result = new MovieReview[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie_obj = movieArray.getJSONObject(i);
                MovieReview rev = new MovieReview();
                rev.author = movie_obj.getString(MOVIEDB_AUTHOR);
                rev.review = movie_obj.getString(MOVIEDB_CONTENT);
                rev.url = movie_obj.getString(MOVIEDB_URL);
                result[i] = rev;
                reviews.add(rev);
            }
            return result;
        }

        protected void onPostExecute(MovieReview[] list) {
            mReviewAdapter.clear();
            for (MovieReview rev : list)
                mReviewAdapter.add(rev);
            mReviewAdapter.notifyDataSetChanged();

        }
    }

    class FetchTrailerTask extends AsyncTask<Void, Void, MovieTrailer[]> {
        String json_str = null;
        final String TAG = this.getClass().getSimpleName();
        MovieTrailer[] objects = null;

        public MovieTrailer[] doInBackground(Void... param) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                final String VIDEOS = "videos";
                final String API_KEY = "api_key";
                Uri uri = Uri.parse(EXTRAINFO_URL + movie_id + "/" + VIDEOS).buildUpon().
                        appendQueryParameter(API_KEY, getActivity().getString(R.string.api_key))
                        .build();
                URL url = new URL(uri.toString());
                Log.v(TAG, url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                json_str = buffer.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error blah ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                objects = getTrailerData(json_str);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error", e);
            }
            return objects;
        }

        private MovieTrailer[] getTrailerData(String str) throws JSONException {
            final String MOVIEDB_RESULT = "results";
            final String MOVIEDB_KEY = "key";
            JSONObject jsonObject=new JSONObject(str);
            JSONArray movieArray = jsonObject.getJSONArray(MOVIEDB_RESULT);
            MovieTrailer[] resultList = new MovieTrailer[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie_obj = movieArray.getJSONObject(i);
                MovieTrailer trailer = new MovieTrailer();
                trailer.key = movie_obj.getString(MOVIEDB_KEY);
                resultList[i] = trailer;
                trailer_keys.add(trailer);
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(MovieTrailer[] strings) {
            if (strings != null) {
                mTrailerAdapter.clear();
                for (MovieTrailer trailer : strings)
                    mTrailerAdapter.add(trailer);
                mTrailerAdapter.notifyDataSetChanged();
            }
        }

    }

    public class ReviewAdapter extends BaseAdapter {
        private final Context mContext;
        private List<MovieReview> review;
        private final int item_layout_id;

        public ReviewAdapter(Context context, List<MovieReview> obj) {
            mContext = context;
            item_layout_id = R.layout.list_item_review;
            review = obj;
        }

        @Override
        public int getCount() {
            return review.size();
        }

        public void add(MovieReview obj) {
            this.review.add(obj);
        }

        @Override
        public MovieReview getItem(int position) {
            return review.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void clear() {
            review.clear();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final MovieReview rev = getItem(position);

            View rootView;
            if (convertView == null) {
                rootView = LayoutInflater.from(getContext()).inflate(item_layout_id, parent, false);
            } else
                rootView = convertView;
            TextView author = (TextView) rootView.findViewById(R.id.author);
            author.setText(rev.author + ":");
            TextView content = (TextView) rootView.findViewById(R.id.review_text);
            content.setText(rev.review);
            if (content.getLineCount() > 5) {
                TextView read_more = (TextView) rootView.findViewById(R.id.read_more);
                read_more.setText(R.string.read_more);
                read_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(rev.url));
                        startActivity(intent);
                    }
                });
            }
            return rootView;
        }
    }

    public class CustomAdapter extends BaseAdapter {
        private final Context mContext;
        private final int item_layout_id;
        private final int textView_id;
        private List<MovieTrailer> keys;

        public CustomAdapter(Context context, List<MovieTrailer> obj) {
            this.mContext = context;
            this.item_layout_id = R.layout.list_item_trailer;
            this.textView_id = R.id.trailer_no;
            keys = obj;
        }

        @Override
        public int getCount() {
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView;
            if (convertView == null) {
                rootView = LayoutInflater.from(getContext()).inflate(item_layout_id, parent, false);
            } else
                rootView = convertView;
            TextView trailer = (TextView) rootView.findViewById(this.textView_id);
            trailer.setText("Trailer " + (position + 1));
            return rootView;
        }

        public void clear() {
            keys.clear();
        }

        public void add(MovieTrailer item) {
            keys.add(item);
        }

    }

}
