package com.jzap.setlist.setlistui.Search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.Playlist.PlaylistActivity;
import com.jzap.setlist.setlistui.R;
import com.jzap.setlist.setlistui.SetListFm.Requestor.ArtistsRequestor;
import com.jzap.setlist.setlistui.SetListFm.SetListFmArtistsDB;
import com.jzap.setlist.setlistui.YouTubeAPIAdapter;

import java.util.LinkedHashMap;
import java.util.List;

import static com.jzap.setlist.setlistui.MessageTypes.ARTISTS_SEARCH_RESULTS;
import static com.jzap.setlist.setlistui.MessageTypes.VIDEO_IDS_RESPONSE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Config.TAG_HEADER + "MainActivity";

    private SearchView mSearchView;
    private RecyclerView mSearchResultsView;
    private RecyclerView.LayoutManager mSearchResultsLayoutManager;
    private RecyclerView.Adapter mSearchResultsAdapter;

    private String mQuery = "";
    LinkedHashMap<String, String> mArtistSuggestions;

    private ProgressBar mSearchProgress;
    private TextView mSearchFailed;

    YouTubeAPIAdapter mYouTubeAPIAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mSearchProgress = (ProgressBar) findViewById(R.id.searchProgress);

        mSearchFailed = (TextView) findViewById(R.id.searchFailed);
        mSearchFailed.setVisibility(View.INVISIBLE);

        setupSearchView();

        /* This is the way to play a playlist on the YouTube app
        Intent intent = createPlayPlaylistIntent(this, "PLx0sYbCqOb8TBPRdmBHs5Iftvv9TPboYG");
        startActivity(intent);
        */

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            handleActionView(intent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            handleActionSearch(intent);
        } else if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            // I was rejected for copyright issues, at least in part by this
            //handleMain();
        }
    }

    private void handleMain() {

        Log.i(TAG, "Handle main");

        // It would be nice to get these artists dynamically, based on popularity and or upcoming concerts (in user's area?)
        mArtistSuggestions = new LinkedHashMap<>();
        mArtistSuggestions.put("Radiohead", "");
        mArtistSuggestions.put("Glen Hansard", "");
        mArtistSuggestions.put("John Mayer", "");
        mArtistSuggestions.put("Rolling Stones", "");
        mArtistSuggestions.put("Pearl Jam", "");
        mArtistSuggestions.put("Tenacious D", "");
        mArtistSuggestions.put("Muse", "");
        mArtistSuggestions.put("Black Sabbath", "");
        mArtistSuggestions.put("Pearl Jam", "");
        mArtistSuggestions.put("Guns N' Roses", "");
        mArtistSuggestions.put("Bon Jovi", "");
        mArtistSuggestions.put("Aerosmith", "");
        mArtistSuggestions.put("Foo Fighters", "");
        mArtistSuggestions.put("Red Hot Chili Peppers", "");
        mArtistSuggestions.put("U2", "");
        mArtistSuggestions.put("Pearl Jam", "");
        mArtistSuggestions.put("Damien Rice", "");
        mArtistSuggestions.put("Ray Lamontagne", "");
        mArtistSuggestions.put("Lake Street Dive", "");
        mArtistSuggestions.put("Daughter", "");
        mArtistSuggestions.put("Blink-182", "");
        mArtistSuggestions.put("Metallica", "");
        mArtistSuggestions.put("Ariana Grande", "");
        mArtistSuggestions.put("Adam Levine", "");
        mArtistSuggestions.put("Rod Stewart", "");
        mArtistSuggestions.put("Brad Pasley", "");
        mArtistSuggestions.put("Rolling Stones", "");
        mArtistSuggestions.put("Foo Fighters", "");
        mArtistSuggestions.put("Red Hot Chili Peppers", "");
        mArtistSuggestions.put("U2", "");
        mArtistSuggestions.put("Pearl Jam", "");
        mArtistSuggestions.put("Tenacious D", "");
        mArtistSuggestions.put("Muse", "");
        mArtistSuggestions.put("Twiddle", "");
        mArtistSuggestions.put("Pearl Jam", "");
        mArtistSuggestions.put("Guns N' Roses", "");
        mArtistSuggestions.put("Bon Jovi", "");
        mArtistSuggestions.put("Aerosmith", "");
        mArtistSuggestions.put("Paul McCartney", "");

        mYouTubeAPIAdapter = new YouTubeAPIAdapter(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case VIDEO_IDS_RESPONSE:
                        Log.i(TAG, "YouTube Response");
                        mSearchProgress.setVisibility(View.INVISIBLE);
                        displayPopularArtists();
                        break;
                }
            }
        });

        mSearchProgress.setVisibility(View.VISIBLE);
        mYouTubeAPIAdapter.requestFirstVideoIds(mArtistSuggestions);
    }

    private void handleActionSearch(Intent intent) {
        mSearchProgress.setVisibility(View.VISIBLE);
        mQuery = intent.getStringExtra(SearchManager.QUERY);
        mSearchView.setQuery(mQuery, false);
        executeSearch(mQuery);
    }

    private void handleActionView(Intent intent) {
        Uri uri = intent.getData();
        Cursor cursor = managedQuery(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();

            // TODO: Change this from the example dictionary, to our artist database
            int wIndex = cursor.getColumnIndexOrThrow(SetListFmArtistsDB.KEY_WORD);
            int dIndex = cursor.getColumnIndexOrThrow(SetListFmArtistsDB.KEY_DEFINITION);

            String artistName = cursor.getString(wIndex);

            Log.d(TAG, "Cursor index = " + cursor.getString(wIndex));
            Log.d(TAG, "Cursor definition = " + cursor.getString(dIndex));

            startPlaylistActivity(artistName);
        }
    }

    private void startPlaylistActivity(String artistName) {
        Intent wordIntent = new Intent(getApplicationContext(), PlaylistActivity.class);
        Uri data = Uri.withAppendedPath(SuggestionProvider.CONTENT_URI, artistName);
        wordIntent.setData(data);
        startActivity(wordIntent);
    }

    private void setupSearchView() {
        mSearchView = (SearchView) findViewById(R.id.search);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setIconifiedByDefault(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    private void executeSearch(String query) {
        new ArtistsRequestor(query, this, new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case ARTISTS_SEARCH_RESULTS:
                        mSearchProgress.setVisibility(View.INVISIBLE);
                        List<String> results = (List<String>) inputMessage.obj;
                        if (results == null || results.isEmpty()) {
                            mSearchFailed.setText("Sorry, no artist matches the search \"" + mQuery + "\"");
                            mSearchFailed.setVisibility(View.VISIBLE);
                        } else {
                            startPlaylistActivity(results.get(0));
                        }
                        break;
                }
            }
        }).request();
    }

    private void displayPopularArtists() {

        // TODO: Pre-load the thumbnails before spinning up the RecyclerView
        mSearchProgress.setVisibility(View.INVISIBLE);

        if (mArtistSuggestions.isEmpty()) {
            mSearchFailed.setText("Sorry, no artists match the search \"" + mQuery + "\"");
            mSearchFailed.setVisibility(View.VISIBLE);
            return;
        }

        mSearchResultsView = (RecyclerView) findViewById(R.id.searchResults);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mSearchResultsView.setHasFixedSize(true);

        mSearchResultsLayoutManager = new LinearLayoutManager(this);
        mSearchResultsView.setLayoutManager(mSearchResultsLayoutManager);
        mSearchResultsAdapter = new SearchResultsRVAdapter(this, mArtistSuggestions);
        mSearchResultsView.setAdapter(mSearchResultsAdapter);
    }

}
