package com.jzap.setlist.setlistui.SetListFm.Requestor;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.Playlist.Playlist;
import com.jzap.setlist.setlistui.SetListFm.Parser.SetListParser;

/**
 * Created by JZ_W541 on 9/28/2017.
 */

public class TourPlaylistCreator extends PlaylistCreator {

    public class TourNameCallback implements Callback {
        public void call(int what, Object obj) {
            onTourNameResponse((SetListPageResponse.TourNameResponse) obj);
        }
    }

    public class TourPageCallback implements Callback {
        @Override
        public void call(int what, Object obj) {
            onTourPageResponse((SetListPageResponse) obj);
        }
    }

    public static class SetListPageResponse {
        public int itemsPerPage;
        public int total;
        public int pageNum;
        public Playlist playlist;
        public String tourName;

        public SetListPageResponse() {
        }

        public SetListPageResponse(int itemsPerPage, int numPages, int pageNum, Playlist playlist, String tourName) {
            this.itemsPerPage = itemsPerPage;
            this.total = numPages;
            this.pageNum = pageNum;
            this.playlist = playlist;
            this.tourName = tourName;
        }

        public static class TourNameResponse {
            public String tourName;
            public String xml;

            public TourNameResponse() {
            }

            public TourNameResponse(String tourName) {
                this(tourName, null);
            }

            public TourNameResponse(String tourName, String xml) {
                this.tourName = tourName;
                this.xml = xml;
            }
        }

    }

    private static final String TAG = Config.TAG_HEADER + "TourPlaylistCtr";

    public TourPlaylistCreator(String artist, Context context, Handler handler) {
        super(artist, context, handler);
    }

    @Override
    protected void onPageResponse(Object obj) {

    }

    @Override
    protected Runnable getRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                requestTourName();
            }
        };
    }

    private void onTourNameResponse(SetListPageResponse.TourNameResponse response) {
        mPlaylist = new Playlist(mArtist);
        if(response == null) {
            deliverResponse();
            return;
        }
        if (response.tourName == null || response.tourName.isEmpty()) {
            Log.i(TAG, "Tour name is null or empty");

            // If no tour name was detected, make playlist based on first page of results
            // (which is sorted by date, by default)
            if(response.xml == null || response.xml.isEmpty()) {
                Log.e(TAG, "TourNameResponse.xml is null or empty");
                deliverResponse();
                return;
            } else {
                try {
                    new SetListParser(mPlaylist, null).parse(response.xml);
                } catch(Exception e) {
                    Log.e(TAG, "SetListParser exception : " + e.toString());
                }
            }

            deliverResponse();
            return;
        }
        Log.i(TAG, "Tour name = " + response.tourName);
        requestTourPage(response.tourName, mPlaylist, 1);
    }

    private void requestTourName() {
        mSetListFmAPIAdapter.requestTourName(new TourNameCallback(), mArtist);
    }

    private void requestTourPage(String tourName, Playlist playlist, int pageNum) {
        mSetListFmAPIAdapter.requestTourPage(new TourPageCallback(), playlist, tourName, pageNum);
    }

    private void onTourPageResponse(SetListPageResponse response) {
        if (response == null || response.total == 0) {
            Log.e(TAG, "Null tour page response");
            deliverResponse();
            return;
        }

        // if this is the last page
        if (response.itemsPerPage * (response.total / response.itemsPerPage) != response.total) { // TODO: Try being a little less hacky with the math (round up to nearest itemsPerPage
            response.total = (response.itemsPerPage * (response.total / response.itemsPerPage)) - response.total;
        }
        if (response.pageNum >= response.total / response.itemsPerPage) {
            deliverResponse();
        } else {
            requestTourPage(response.tourName, response.playlist, response.pageNum + 1);
        }
    }

}
