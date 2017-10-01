package com.jzap.setlist.setlistui.SetListFm.Requestor;

import android.content.Context;
import android.os.Handler;

import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.MessageTypes;

import java.util.List;

/**
 * Created by JZ_W541 on 9/29/2017.
 */

public class ArtistsRequestor extends SetListAPIRequestor {

    public class ArtistCallback implements Callback {
        @Override
        public void call(int what, Object obj) {
            onAritstsResponse((List<String>) obj);
        }
    }

    private static final String TAG = Config.TAG_HEADER + "ArtistRqstr";
    private List<String> mArtists;
    private String mArtist;

    public ArtistsRequestor(String artist, Context context, Handler handler) {
        super(context, handler);
        mArtist = artist;
    }

    @Override
    protected Runnable getRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                requestArtits();
            }
        };
    }

    private void requestArtits() {
        mSetListFmAPIAdapter.requestArtists(new ArtistCallback(), mArtist);
    }

    private void onAritstsResponse(List<String> response) {
        mArtists = response;
        deliverResponse();
    }

    @Override
    protected Object getResponse() {
        return mArtists;
    }

    @Override
    protected int getResponseType() {
        return MessageTypes.ARTISTS_SEARCH_RESULTS;
    }
}
