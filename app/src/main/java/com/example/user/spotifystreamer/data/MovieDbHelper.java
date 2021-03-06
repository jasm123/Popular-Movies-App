package com.example.user.spotifystreamer.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.user.spotifystreamer.Movie;
import com.example.user.spotifystreamer.data.MovieContract.MovieEntry;

import java.util.ArrayList;

/**
 * The SQLite helper class which helps us create and upgrade tables to
 * be stored in our movies.db database.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE= "CREATE TABLE if not exists " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " integer PRIMARY KEY," +
                MovieEntry.COLUMN_MID + "," +
                MovieEntry.COLUMN_TITLE + "," +
                MovieEntry.COLUMN_POSTER + "," +
                MovieEntry.COLUMN_DATE + "," +
                MovieEntry.COLUMN_OVERVIEW + "," +
                MovieEntry.COLUMN_RATING + "," +
                MovieEntry.COLUMN_BACKDROP +""+
                " );";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+MovieEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean hasObject(String m_id){
        SQLiteDatabase db=this.getWritableDatabase();
        final String selectString= "SELECT * FROM "+MovieEntry.TABLE_NAME+" WHERE "+
                MovieEntry.COLUMN_MID+"=?";
        Cursor cursor = db.rawQuery(selectString,new String[] {m_id});
        boolean hasObject=false;
        if(cursor.moveToFirst())
            hasObject = true;
        cursor.close();
        db.close();
        return hasObject;
    }
    public ArrayList<Movie> getAllMovies(){
        ArrayList<Movie> movieList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(MovieEntry.TABLE_NAME,null,
                null,null,null,null,null,null);

        if (cursor.moveToFirst()) {
            do {
                Movie movie = new Movie();
                movie.poster_path=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER));
                movie.movie_id=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MID));
                movie.title=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
                movie.thumbnail=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP));
                movie.plot=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
                movie.user_rating=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RATING));
                movie.release_date=cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_DATE));
                movieList.add(movie);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return movieList;
    }

}
