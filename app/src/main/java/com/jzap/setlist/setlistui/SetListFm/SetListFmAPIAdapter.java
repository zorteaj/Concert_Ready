package com.jzap.setlist.setlistui.SetListFm;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jzap.setlist.setlistui.Callback;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.Playlist.Playlist;
import com.jzap.setlist.setlistui.SetListFm.Parser.ArtistsParser;
import com.jzap.setlist.setlistui.SetListFm.Parser.Parser;
import com.jzap.setlist.setlistui.SetListFm.Parser.SetListParser;
import com.jzap.setlist.setlistui.SetListFm.Parser.TourNameParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JZ_W541 on 9/28/2017.
 */

public class SetListFmAPIAdapter {

    private static final String TAG = Config.TAG_HEADER + "SetListFmAdpt";

    private RequestQueue mRequestQueue;

    public SetListFmAPIAdapter(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void requestTourName(Callback callback, String artist) {
        //String url = "https://api.setlist.fm/rest/1.0/search/setlists?artistName=" + artist + "&p=1";
        StringBuilder url = new StringBuilder();
        try {
            url.append("https://api.setlist.fm/rest/1.0/search/setlists?artistName=");
            url.append(URLEncoder.encode(artist, "UTF-8"));
            url.append("&p=1");
            TourNameParser tourNameParser = new TourNameParser();
            request(callback, url.toString(), tourNameParser);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Request Tour Name error: " + e.toString());
        }
    }

    public void requestTourPage(Callback callback, Playlist playlist, String tour, int pageNum) {
        //String url = "https://api.setlist.fm/rest/1.0/search/setlists?artistName=" + playlist.getArtist() + "&p=" + pageNum + "&tourName=" + tour;
        StringBuilder url = new StringBuilder();
        try {
            url.append("https://api.setlist.fm/rest/1.0/search/setlists?artistName=");
            url.append(URLEncoder.encode(playlist.getArtist(), "UTF-8"));
            url.append("&p=" + pageNum + "&tourName=");
            url.append(URLEncoder.encode(tour, "UTF-8"));
            SetListParser setListParser = new SetListParser(playlist, tour);
            request(callback, url.toString(), setListParser);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Request Tour Page error: " + e.toString());
        }
    }

    public void requestArtists(Callback callback, String artist) {
        //String url = "https://api.setlist.fm/rest/1.0/search/artists?artistName=" + artist + "&p=1&sort=relevance";
        StringBuilder url = new StringBuilder();
        try {
            url.append("https://api.setlist.fm/rest/1.0/search/artists?artistName=");
            url.append(URLEncoder.encode(artist, "UTF-8"));
            url.append("&p=1&sort=relevance");
            ArtistsParser artistsParser = new ArtistsParser();
            request(callback, url.toString(), artistsParser);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Request Artists error: " + e.toString());
        }
    }

    public void request(final Callback callback, String url, final Parser parser) {
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            parser.parse(response);
                            callback.call(parser.getType(), parser.getResult());
                        } catch (Exception e) {
                            Log.d(TAG, "ParseXML Exception: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "SetList API call error: " + error.toString());
                        callback.call(parser.getType(), null);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-api-key", Config.SETLISTFM_API_KEY);

                return params;
            }
        };

        mRequestQueue.add(request);
    }
}
