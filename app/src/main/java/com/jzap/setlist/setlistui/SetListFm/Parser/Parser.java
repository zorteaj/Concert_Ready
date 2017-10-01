package com.jzap.setlist.setlistui.SetListFm.Parser;

import android.util.Log;

import com.jzap.setlist.setlistui.Config;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by JZ_W541 on 9/19/2017.
 */

public abstract class Parser {

    protected XmlPullParser mXpp;

    private static final String TAG = Config.TAG_HEADER + "Parser";

    public void parse(String in) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        mXpp = factory.newPullParser();
        mXpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        mXpp.setInput(new StringReader(in));
    }

    protected void skip(XmlPullParser xpp) throws XmlPullParserException, IOException {
        if (xpp.getEventType() != XmlPullParser.START_TAG) {
            Log.e(TAG, "Trying to skip a non start tag");
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (xpp.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public abstract Object getResult();

    public abstract int getType();
}
