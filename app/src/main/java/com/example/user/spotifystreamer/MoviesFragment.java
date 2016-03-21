package com.example.user.spotifystreamer;

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
 * A placeholder fragment which contains the gridview of movies.
 */
public class MoviesFragment extends Fragment {
    String BASE_URL = "http://api.themoviedb.org/3/discover/movie"; //base url to retrieve json data.
    String[] poster_path;
    ArrayList<String> thumbnail = new ArrayList<>();
    ArrayList<String> plot = new ArrayList<>();
    ArrayList<String> user_rating = new ArrayList<>();
    ArrayList<String> release_date = new ArrayList<>();
    ArrayList<String> title = new ArrayList<>();
    ImageAdapter mMovieAdapter;

    public MoviesFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onStart() {
        super.onStart();
        updateMovie();
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

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new ImageAdapter(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView movies_gridview = (GridView) rootView.findViewById(R.id.movies_gridview);
        movies_gridview.setAdapter(mMovieAdapter);
        movies_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("TITLE", title.get(position));//send title
                intent.putExtra("PLOT", plot.get(position));//send overview
                intent.putExtra("RATING", user_rating.get(position));//send user-rating
                intent.putExtra("IMAGE", thumbnail.get(position));
                intent.putExtra("release-date", release_date.get(position));//send release date
                startActivity(intent);
            }
        });
        return rootView;
    }

    public void updateMovie() {
        if (isNetworkAvailable()) {
            FetchMovieTask movieTask = new FetchMovieTask();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort_by = prefs.getString(getString(R.string.pref_general_key), getString(R.string.popularity));
            title.clear();
            thumbnail.clear();
            plot.clear();
            release_date.clear();
            user_rating.clear();
            movieTask.execute(sort_by);
        } else {
            Toast.makeText(getContext(), "No Connection!\nCheck your Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    class FetchMovieTask extends AsyncTask<String, Void, String[]> {
        String json_str = null;
        String TAG = this.getClass().getSimpleName();

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
                StringBuffer buffer = new StringBuffer();
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
            JSONObject jsonObject = new JSONObject(str);
            JSONArray movieArray = jsonObject.getJSONArray(MOVIEDB_RESULT);
            String[] resultList = new String[movieArray.length()];
            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movie_obj = movieArray.getJSONObject(i);
                resultList[i] = IMAGE_URL + movie_obj.getString(MOVIEDB_POSTER_PATH);
                plot.add(movie_obj.getString(OVERVIEW));
                if (!movie_obj.getString(image_path).endsWith(".jpg")) {
                    thumbnail.add(resultList[i]);
                } else
                    thumbnail.add(IMAGE_URL + movie_obj.getString(image_path));
                release_date.add(movie_obj.getString(RELEASE_DATE));
                user_rating.add(movie_obj.getString(USER_RATING));
                title.add(movie_obj.getString(MOVIEDB_TITLE));
            }
            return resultList;
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<String> urls = new ArrayList<String>();

        //constructor
        public ImageAdapter(Context context) {
            this.context = context;
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

        public void clear() {
            this.urls.clear();
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
            notifyDataSetChanged();
        }
    }
}