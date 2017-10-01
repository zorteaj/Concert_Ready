package com.jzap.setlist.setlistui;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.jzap.setlist.setlistui.Playlist.Playlist;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by JZ_W541 on 9/19/2017.
 */

public class YouTubeAPIAdapter {

    private static final String TAG = Config.TAG_HEADER + "YouTubeAPIAdapter";

    private HttpTransport mTransport;
    private JsonFactory mJsonFactory;
    private YouTube mYouTube;
    private Handler mHandler;
    private VideoIdRequestThreadPoolExecutor mThreadPoolExec;
    private Playlist mPlaylist;
    private LinkedHashMap<String, String> mAristsTopSongs;

    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

    public class VideoIDRunnable implements Runnable {
        private Playlist.Song mSong;
        private boolean mFirstSong;

        public VideoIDRunnable(Playlist.Song song, boolean firstSong) {
            mSong = song;
            mFirstSong = firstSong;
        }

        @Override
        public void run() {
            getVideoId(mSong);
        }

        public void getVideoId(Playlist.Song song) {
            YouTube.Search.List list = buildList(mPlaylist.getArtist(), song.name);
            if(list == null) {
                Log.e(TAG, "YouTube search list is null, search will not execute");
                return;
            }
            try {
                SearchListResponse listResponse = list.execute();
                List<SearchResult> searchResults = listResponse.getItems();
                if (searchResults != null && searchResults.size() > 0) {
                    SearchResult s = searchResults.get(0);
                    mSong.videoId = s.getId().getVideoId(); // TODO: This should maybe (should it really?) be synchronized, but it's taking forever!
                    if(mFirstSong) { // TODO: Don't send if it's an empty string though, then handle elsewhere
                        mHandler.obtainMessage(MessageTypes.FIRST_SONG_RESPONSE, mSong).sendToTarget();
                    }
                } else {
                    Log.i(TAG, "Search results for " + mPlaylist.getArtist() + ", " + song.name + " is null or empty");
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }

        @Nullable
        private YouTube.Search.List buildList(String artist, String song)
        {
            YouTube.Search.List list = null;
            try {
                list = mYouTube.search().list("id");
                list.setFields("items/id/videoId");
                list.setQ(artist + " " + song);
                list.setType("video");
                list.setKey(Config.YOUTUBE_API_KEY);
                list.setMaxResults(new Long(1));
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
            return list;
        }
    }

    public class TopVideoIDRunnable implements Runnable {
        private String mArtist;

        public TopVideoIDRunnable(String artist) {
            mArtist = artist;
        }

        @Override
        public void run() {
            getVideoId();
        }

        public void getVideoId() {
            YouTube.Search.List list = buildList();
            if(list == null) {
                Log.e(TAG, "YouTube search list is null, search will not execute");
                return;
            }
            try {
                SearchListResponse listResponse = list.execute();
                List<SearchResult> searchResults = listResponse.getItems();
                if (searchResults != null && searchResults.size() > 0) {
                    SearchResult s = searchResults.get(0);
                    mAristsTopSongs.put(mArtist, s.getId().getVideoId());
                } else {
                    Log.i(TAG, "Search results for " + mArtist + " is null or empty");
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }

        @Nullable
        private YouTube.Search.List buildList()
        {
            YouTube.Search.List list = null;
            try {
                list = mYouTube.search().list("id");
                list.setFields("items/id/videoId");
                list.setQ(mArtist);
                list.setType("video");
                list.setKey(Config.YOUTUBE_API_KEY);
                list.setMaxResults(new Long(1));
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
            return list;
        }
    }

    private class VideoIdRequestThreadPoolExecutor extends ThreadPoolExecutor
    {
        public VideoIdRequestThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            // I'm keeping this around in case I need it in the future
        }

    }

    public YouTubeAPIAdapter(Handler handler) {
        mHandler = handler;
        mTransport = AndroidHttp.newCompatibleTransport();
        mJsonFactory = JacksonFactory.getDefaultInstance();
        mYouTube = new YouTube.Builder(mTransport, mJsonFactory, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {}
        }).setApplicationName("IHaveNoIdeaWhatThisIsFor").build();

        Log.i(TAG, "Created YouTube Service");
    }

    public void requestFirstVideoIds(LinkedHashMap<String, String> artistsTopSongs) {
        mAristsTopSongs = artistsTopSongs;
        if(artistsTopSongs.isEmpty()) {
            Log.e(TAG, "Artists Top Songs Map is empty");
            return; // TODO: Handle better
        }

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        mThreadPoolExec = new VideoIdRequestThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 10, TimeUnit.SECONDS, workQueue);

        for(LinkedHashMap.Entry<String, String> entry: mAristsTopSongs.entrySet()) {
            mThreadPoolExec.execute(new TopVideoIDRunnable(entry.getKey()));
        }

        shutdown();
    }

    // only return playlist for which we have search results, so that the playlist does not
    // show playlist that won't have a corresponding video
    public void requestVideoIds(Playlist playlist) {
        mPlaylist = playlist;

        if(mPlaylist.getSongs().isEmpty()) {
            Log.e(TAG, "Playlist is empty");
            return; // TODO: Handle better
        }

        // first song is a special case - we want to get this up and running to reduce perceived latency
        // new Thread(new VideoIDRunnable(playlist.getSortedMap().entrySet().iterator().next().getKey(), true)).start();

        new Thread(new VideoIDRunnable(mPlaylist.getSong(0), true)).start();

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        mThreadPoolExec = new VideoIdRequestThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 10, TimeUnit.SECONDS, workQueue);

        for (Playlist.Song s : mPlaylist.getSongs()) {
            mThreadPoolExec.execute(new VideoIDRunnable(s, false));
        }

        shutdown();
    }

    public void shutdown() {
        new Thread() {
            @Override
            public void run() {
                mThreadPoolExec.shutdown();
                try {
                    mThreadPoolExec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
                mHandler.obtainMessage(MessageTypes.VIDEO_IDS_RESPONSE).sendToTarget();
            }
        }.start();
    }

}