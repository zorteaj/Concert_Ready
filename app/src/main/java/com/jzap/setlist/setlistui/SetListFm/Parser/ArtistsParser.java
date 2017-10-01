package com.jzap.setlist.setlistui.SetListFm.Parser;

import android.util.Log;

import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JZ_W541 on 9/19/2017.
 */

public class ArtistsParser extends Parser {

    private static final String TAG = Config.TAG_HEADER + "ArtistParser";

    private List<String> mArtists;

    public ArtistsParser() {
        super();
        mArtists = new ArrayList<>();
    }

    @Override
    public void parse(String in) throws XmlPullParserException, IOException {
        super.parse(in);

        mXpp.nextTag();
        mXpp.require(XmlPullParser.START_TAG, null, "artists");

        while (!(mXpp.next() == XmlPullParser.END_TAG)) {
            if (mXpp.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (mXpp.getName().equals("artist")) {
                readArtist(mXpp);
            } else {
                skip(mXpp);
            }
        }
    }

    @Override
    public Object getResult() {
        return mArtists;
    }

    @Override
    public int getType() {
        return Callback.ARTISTS;
    }

    public void readArtist(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "artist");
        } catch (XmlPullParserException e) {
            Log.e(TAG, "readArtist exception");
        }

        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (xpp.getAttributeName(i).equals("name")) {
                mArtists.add(xpp.getAttributeValue(i));
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

}
