package com.jzap.setlist.setlistui.Playlist;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.jzap.setlist.setlistui.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by JZ_W541 on 9/19/2017.
 */

public class Playlist {

    private static final String TAG = Config.TAG_HEADER + "Playlist";

    private String mArtist;
    RecyclerView.Adapter mAdapter;
    private Map<String, Song> mSongsMap = new HashMap<>();
    private List<Map.Entry<String, Song>> mSongsLinkedList;
    private List<Song> mSongs = null;
    private int mPosition = 0;

    public static class Song {

        public static final int STOPPED = 0;
        public static final int PAUSED = 1;
        public static final int PLAYING = 2;

        public Song(String name){
            this.name = name;
        }

        public Song(String name, Integer count) {
            this(name, count, "");
        }

        public Song(String name, Integer count, String videoId) {
            this.name = name;
            this.count = count;
            this.videoId = videoId;
        }

        public Integer count;
        public int position;
        public String videoId = "";
        public String name = "";
        public int state = STOPPED;
    }

    public Playlist(String artist) {
        mArtist = artist;
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    public String getArtist() {
        return mArtist;
    }

    public void insert(String name) {
        Song attribs = mSongsMap.get(name);
        if (attribs == null) {
            mSongsMap.put(name, new Song(name, 1));
        } else {
            mSongsMap.put(name, new Song(name, attribs.count + 1));
        }
    }

    public boolean clean() {
        boolean changed = false;
        Log.i(TAG, "Size before clean = " + mSongs.size());
        for (int i = 0; i < mSongs.size(); i++) {
            if (!valid(mSongs.get(i))) {
                changed = true;
                remove(i--);
            }
        }
        Log.i(TAG, "Size after clean = " + mSongs.size());
        return changed;
    }

    private boolean valid(Song song) {
        if (!song.videoId.isEmpty()) {
            return true;
        }
        return false;
    }

    public void remove(int position) {
        Log.d(TAG, "Removing position " + position);
        mSongsLinkedList.remove(position);
        // if current position is past the removed song position
        // all songs will shift up, so mPosition must be decremented
        if(mPosition > position) {
            mPosition --;
        }
        populateSongsList();
    }

    public boolean setNextPosition() {
        if(mPosition + 1 >= mSongs.size()) {
            Log.e(TAG, "Requesting out of bound index to Playlist");
            return false;
        } else {
            mPosition++;
            return true;
        }
    }

    public boolean setPreviousPosition() {
        if(mPosition -1 < 0) {
            Log.e(TAG, "Requesting out of bound index to Playlist");
            return false;
        } else {
            mPosition--;
            return true;
        }
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public Song getCurrentSong() {
        return getSong(mPosition);
    }

    public Song getSong(int position) {
        if (mSongs == null) {
            generateSongList();
        }

        Song song = null;
        try {
            song = mSongs.get(position);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, e.toString());
        }

        return song;
    }

    public List<Song> getSongs() {
        if (mSongs == null) {
            generateSongList();
        }
        return mSongs;
    }

    private void populateSongsList() {
        mSongs = new ArrayList<>(); // TODO: I wonder if I can optimize remaking this each time I use this method
        int songPosition = 0;
        for (Map.Entry<String, Song> entry : mSongsLinkedList) {
            Song song = entry.getValue();
            song.position = songPosition++;
            mSongs.add(entry.getValue());
        }
    }


    // TODO: This is getting called twice?
    private void  generateSongList() {
        mSongs = new ArrayList<>();
        mSongsLinkedList = new LinkedList<>(mSongsMap.entrySet());

        // Sorting the list based on count
        Collections.sort(mSongsLinkedList, new Comparator<Map.Entry<String, Song>>() {
            public int compare(Map.Entry<String, Song> o1, Map.Entry<String, Song> o2) {
                return o2.getValue().count.compareTo(o1.getValue().count);
            }
        });

        populateSongsList();
    }

    public void onCurrentSongEnded() {
        getCurrentSong().state = Song.STOPPED;
        if(mAdapter != null) {
            mAdapter.notifyItemChanged(mPosition);
        } else {
            Log.d(TAG, "mAdapter is null");
        }
    }

    public void play() {
        getCurrentSong().state = Song.PLAYING;
        if(mAdapter != null) {
            mAdapter.notifyItemChanged(mPosition);
        } else {
            Log.d(TAG, "mAdapter is null");
        }
    }

    public void pause() {
        getCurrentSong().state = Song.PAUSED;
        if(mAdapter != null) {
            mAdapter.notifyItemChanged(mPosition);
        } else {
            Log.d(TAG, "mAdapter is null");
        }
    }


}
