package com.jzap.setlist.setlistui.SetListFm.Requestor;

import android.content.Context;
import android.os.Handler;

import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.MessageTypes;
import com.jzap.setlist.setlistui.Playlist.Playlist;


/**
 * Created by JZ_W541 on 9/28/2017.
 */

public abstract class PlaylistCreator extends SetListAPIRequestor {

    public class PageCallback implements Callback {
        public void call(int what, Object obj) {
            onPageResponse(obj);
        }
    }

    private static final String TAG = Config.TAG_HEADER + "PlaylistCtr";

    protected Playlist mPlaylist;
    protected String mArtist;

    public PlaylistCreator(String artist, Context context, Handler handler) {
        super(context, handler);
        mArtist = artist;
    }

    @Override
    protected Object getResponse() {
        return mPlaylist;
    }

    @Override
    protected int getResponseType() {
        return MessageTypes.PLAYLIST_CREATED;
    }

/*
    protected void requestPage() {
        mSetListFmAPIAdapter.requestPage(new PageCallback(), mArtist);
    }*/

    // Handles response from SetListFMAPIAdapter request
    protected abstract void onPageResponse(Object obj);

 /*   protected void deliverPlaylist() {
        mRequestorHandler.obtainMessage(MessageTypes.PLAYLIST_CREATED, mPlaylist).sendToTarget();
    }*/
}
