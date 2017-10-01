package com.jzap.setlist.setlistui.SetListFm.Parser;

import android.util.Log;

import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.Playlist.Playlist;
import com.jzap.setlist.setlistui.SetListFm.Requestor.TourPlaylistCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by JZ_W541 on 9/19/2017.
 */

public class SetListParser extends Parser {

    private static final String TAG = Config.TAG_HEADER + "SetListParser";
    private Playlist mPlaylist;
    private int mItemsPerPage;
    private int mPageNum;
    private int mTotal;
    private String mTourName;

    public SetListParser(Playlist playlist, String tourName) {
        super();
        mPlaylist = playlist;
        mTourName = tourName;
    }

    @Override
    public void parse(String in) throws XmlPullParserException, IOException {
        super.parse(in);

        mXpp.nextTag();
        mXpp.require(XmlPullParser.START_TAG, null, "setlists");

        for (int i = 0; i < mXpp.getAttributeCount(); i++) {
            String attribName = mXpp.getAttributeName(i);
            if (attribName.equals("itemsPerPage")) {
                try {
                    mItemsPerPage = Integer.parseInt(mXpp.getAttributeValue(i));
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.toString());
                }
            } else if (attribName.equals("page")) {
                mPageNum = Integer.parseInt(mXpp.getAttributeValue(i));
            } else if (attribName.equals("total")) {
                mTotal = Integer.parseInt(mXpp.getAttributeValue(i));
            }
        }

        while (!(mXpp.next() == XmlPullParser.END_TAG)) {
            if(mXpp.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            if(mXpp.getName().equals("setlist"))
            {
                readSetList(mXpp);
            } else {
                skip(mXpp);
            }
        }
    }

    @Override
    public Object getResult() {
        TourPlaylistCreator.SetListPageResponse result =
                new TourPlaylistCreator.SetListPageResponse(mItemsPerPage, mTotal, mPageNum, mPlaylist, mTourName);
        return result;
    }

    @Override
    public int getType() {
        return Callback.SETLIST;
    }

    private void readSetList(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "setlist");
        } catch(XmlPullParserException e) {
            Log.e(TAG, e.toString());
        }

        while (!(xpp.next() == XmlPullParser.END_TAG)) {
            if(xpp.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            if(xpp.getName().equals("url")) {
                readURL(xpp);
            } else if(xpp.getName().equals("sets")) {
                readSets(xpp);
            } else {
                skip(xpp);
            }
        }
    }

    private void readURL(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "url");
        } catch(XmlPullParserException e) {
            Log.e(TAG, e.toString());
        }

        // Don't delete, even though url is not used, xpp.nextText() is necessary
        String url = xpp.nextText();
    }

    private void readSets(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "sets");
        } catch(XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }

        while (!(xpp.next() == XmlPullParser.END_TAG)) {
            if(xpp.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            if(xpp.getName().equals("set")) {
                readSet(xpp);
            } else {
                skip(xpp);
            }
        }
    }

    private void readSet(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "set");
        } catch(XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }

        while (!(xpp.next() == XmlPullParser.END_TAG)) {
            if(xpp.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            if(xpp.getName().equals("song")) {
                readSong(xpp);
            } else {
                skip(xpp);
            }
        }
    }

    private void readSong(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "song");
        } catch(XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }

        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (xpp.getAttributeName(i).equals("name")) {
                String songName = xpp.getAttributeValue(i);
                if (validateSong(songName)) {
                    mPlaylist.insert(songName);
                } else {
                    Log.d(TAG, "Received a blank/empty or otherwise invalid song name");
                }
            }
        }

        while (!(xpp.next() == XmlPullParser.END_TAG)) {
            if (xpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            } else {
                skip(xpp);
            }
        }
    }

    private boolean validateSong(String songName) {
        if(songName.equals("") || songName.equals(" ") || songName.equals("?")) {
            return false;
        } else {
            return true;
        }
    }

}
