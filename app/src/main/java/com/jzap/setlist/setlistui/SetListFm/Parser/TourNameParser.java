package com.jzap.setlist.setlistui.SetListFm.Parser;

import android.util.Log;

import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.SetListFm.Requestor.TourPlaylistCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by JZ_W541 on 9/28/2017.
 */

public class TourNameParser extends Parser {

    private String mTourName = null;
    private String mXml = null;

    private static final String TAG = Config.TAG_HEADER + "TourNameParser";

        public TourNameParser() {
            super();
        }

    @Override
    public void parse(String in) throws XmlPullParserException, IOException {
        super.parse(in);

        mXml = in;

        mXpp.nextTag();
        mXpp.require(XmlPullParser.START_TAG, null, "setlists");

        while (!(mXpp.next() == XmlPullParser.END_TAG)) {
            if(mXpp.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            if(mXpp.getName().equals("setlist"))
            {
                readSetList(mXpp);
                return; // only check the first setlist
            } else {
                skip(mXpp);
            }
        }

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
            if(xpp.getName().equals("tour")) {
                readTour(xpp);
                return;
            } else {
                skip(xpp);
            }
        }
    }

    private void readTour(XmlPullParser xpp) throws XmlPullParserException, IOException {
        try {
            xpp.require(XmlPullParser.START_TAG, null, "tour");
        } catch(XmlPullParserException e) {
            Log.d(TAG, e.toString());
        }

        for (int i = 0; i < xpp.getAttributeCount(); i++) {
            if (xpp.getAttributeName(i).equals("name")) {
                mTourName = xpp.getAttributeValue(i);
            }
        }

        return;

    }

    @Override
    public Object getResult() {
        return new TourPlaylistCreator.SetListPageResponse.TourNameResponse(mTourName, mXml);
    }

    @Override
    public int getType() {
        return Callback.TOUR_NAME;
    }
}
