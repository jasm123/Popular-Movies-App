package com.example.user.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

/**
 * A placeholder fragment which contains the Grid View of movies.
 */
public class MoviesFragment extends Fragment {

    String BASE_URL = "http://api.themoviedb.org/3/discover/movie"; //base url to retrieve json data.
    ArrayList<Movie> movies ;
    private static final String TAG= "jinal";
    ImageAdapter mMovieAdapter;


    /*The Callback interface which all activities hosting this fragment will implement
     *to get notified about the list items been selected.
     */
    public interface Callback{
        //When an item has been selected
        public void onItemSelected(Movie movie);
    }

    public MoviesFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onPreferenceChanged(String sort_by){
        Log.v(TAG, "PREFERENCE CHANGED");
        movies.clear();
        if(sort_by.equals(getString(R.string.favorites))){
            fetchFavorites();
        }
        else{
            if(isNetworkAvailable()) {
                FetchMovieTask movieTask = new FetchMovieTask();
                movieTask.execute(sort_by);
            }
            else
                Toast.makeText(getContext(), "No Connection!\nCheck your Internet Connection",
                        Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach");
    }

    public void onStart()
    {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movies);
        super.onSaveInstanceState(outState);
        Log.v(TAG, "onSaveInstanceState");

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(getContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.movies_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onResume(){
        Log.v(TAG,"onResume");
        //updateMovie();
        super.onResume();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            movies=new ArrayList<Movie>();
            mMovieAdapter = new ImageAdapter(getActivity(), movies);
            updateMovie();
        } else {
           movies = savedInstanceState.getParcelableArrayList("movies");
            mMovieAdapter = new ImageAdapter(getActivity(), movies);

        }
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView movies_gridview = (GridView) rootView.findViewById(R.id.movies_gridview);
        movies_gridview.setAdapter(mMovieAdapter);
        movies_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback)getActivity()).onItemSelected(movies.get(position));

            }
        });

        return rootView;
    }

    private void updateMovie() {
        if (isNetworkAvailable()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_by = prefs.getString(getString(R.string.pref_general_key), getString(R.string.popularity));
            movies.clear();
            if(sort_by.equals(getString(R.string.favorites))){
                fetchFavorites();
            }
            else{
                FetchMovieTask movieTask = new FetchMovieTask();
                movieTask.execute(sort_by);
            }
        } else {
            Toast.makeText(getContext(), "No Connection!\nCheck your Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    protected void fetchFavorites() {
        MovieDbHelper db = new MovieDbHelper(getContext());
        ArrayList<Movie> movieDetails = db.getAllMovies();
        int n=movieDetails.size();
        if (n > 0) {
            movies = movieDetails;
            String[] poster_paths=new String[n];
            for(int i=0;i<n;i++)
                poster_paths[i]=movies.get(i).poster_path;
            mMovieAdapter.replace(poster_paths);
        }
        else {
            Toast.makeText(getActivity(), "No Favourites Added!", Toast.LENGTH_LONG).show();
        }
    }


    class FetchMovieTask extends AsyncTask<String, Void, String[]> {
        String json_str = null;
        final String TAG = this.getClass().getSimpleName();
        String[] poster_path;

        public String[] doInBackground(String... param) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                String sort_by_category = param[0];
                final String SORT_BY = "sort_by";
                final String API_KEY = "api_key";
                Uri uri = Uri.parse(BASE_URL).buildUpon().
                        appendQueryParameter(SORT_BY, sort_by_category + ".desc").
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
                poster_path = getMovieData(json_str);
            } catch (JSONException e) {
                Log.e(TAG, "JSON Error", e);
            }
            return poster_path;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                mMovieAdapter.replace(strings);
            }
        }

        private String[] getMovieData(String str) throws JSONException {
            final String MOVIEDB_RESULT = "results";
            final String MOVIEDB_TITLE = "title";
            final String MOVIEDB_POSTER_PATH = "poster_path";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String USER_RATING = "vote_average";
            final String image_path = "backdrop_path";
            final String IMAGE_URL = "http://image.tmdb.org/t/p/w342/";
            final String ID = "id";
            JSONObject jsonObject = new JSONObject(str);
            JSONArray movieArray = jsonObject.getJSONArray(MOVIEDB_RESULT);
            String[] resultList = new String[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                movies.add(new Movie());
                Movie present_movie = movies.get(i);
                JSONObject movie_obj = movieArray.getJSONObject(i);
                resultList[i] = IMAGE_URL + movie_obj.getString(MOVIEDB_POSTER_PATH);
                present_movie.poster_path = resultList[i];
                present_movie.plot = (movie_obj.getString(OVERVIEW));
                if (!movie_obj.getString(image_path).endsWith(".jpg")) {
                    present_movie.thumbnail = (resultList[i]);
                } else
                    present_movie.thumbnail = (IMAGE_URL + movie_obj.getString(image_path));
                present_movie.release_date = movie_obj.getString(RELEASE_DATE);
                present_movie.user_rating = movie_obj.getString(USER_RATING);
                present_movie.title = movie_obj.getString(MOVIEDB_TITLE);
                present_movie.movie_id = movie_obj.getString(ID);
            }
            return resultList;
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<String> urls = new ArrayList<>();

        //constructor
        public ImageAdapter(Context context, ArrayList<Movie> movie_list) {
            this.context = context;
            if (!movie_list.isEmpty()) {
                for (int i = 0; i < movie_list.size(); i++)
                    urls.add(movie_list.get(i).poster_path);
            }
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public String getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView moviePoster;
            if (convertView == null) {
                View rootView = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
                moviePoster = (ImageView) rootView;
            } else
                moviePoster = (ImageView) convertView;
            moviePoster.setAdjustViewBounds(true);
            Picasso.with(context).load(getItem(position)).into(moviePoster);
            return moviePoster;
        }

        public void replace(String[] paths) {
            if (!urls.isEmpty())
                this.urls.clear();
            Collections.addAll(urls, paths);
            this.notifyDataSetChanged();
        }
    }
}