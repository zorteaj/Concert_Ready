package com.jzap.setlist.setlistui.Search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.Playlist.PlaylistActivity;
import com.jzap.setlist.setlistui.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by JZ_W541 on 9/18/2017.
 */

public class SearchResultsRVAdapter extends RecyclerView.Adapter<SearchResultsRVAdapter.ViewHolder> {
    private static final String TAG = Config.TAG_HEADER + "SearchResultsAdptr";

    LinkedHashMap<String, String> mArtistsTopSongs;
    List<LinkedHashMap.Entry<String, String>> mList;
    List<Drawable> mThumbnails;

    static Activity sActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLayout;
        private LinearLayout mLeftPanel;
        private LinearLayout mRightPanel;

        public ViewHolder(LinearLayout l) {
            super(l);
            mLayout = l;
            mLeftPanel = (LinearLayout)((ConstraintLayout) mLayout.getChildAt(0)).getChildAt(1);
            mRightPanel = (LinearLayout)((ConstraintLayout) mLayout.getChildAt(1)).getChildAt(1);

            mLeftPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) ((LinearLayout) v).getChildAt(1);

                    Intent wordIntent = new Intent(sActivity.getApplicationContext(), PlaylistActivity.class);
                    Uri data = Uri.withAppendedPath(SuggestionProvider.CONTENT_URI, t.getText().toString());
                    wordIntent.setData(data);
                    sActivity.startActivity(wordIntent);
                }
            });

            mRightPanel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView t = (TextView) ((LinearLayout) v).getChildAt(1);

                    Intent wordIntent = new Intent(sActivity.getApplicationContext(), PlaylistActivity.class);
                    Uri data = Uri.withAppendedPath(SuggestionProvider.CONTENT_URI, t.getText().toString());
                    wordIntent.setData(data);
                    sActivity.startActivity(wordIntent);
                }
            });
        }
    }

    public SearchResultsRVAdapter(Activity activity, LinkedHashMap<String, String> artists) {
        mArtistsTopSongs = artists;
        sActivity = activity;
        mThumbnails = new ArrayList<>(mArtistsTopSongs.size());
        for(int i = 0; i < mArtistsTopSongs.size(); i++) {
            mThumbnails.add(null);
        }
        Log.d(TAG, "Test D size = " + mThumbnails.size());
    }

    @Override
    public SearchResultsRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) sActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout l = (LinearLayout) inflater.inflate(R.layout.recommended_layout, parent, false);

        SearchResultsRVAdapter.ViewHolder vh = new SearchResultsRVAdapter.ViewHolder(l);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final int pos = position;
        final ViewHolder h = holder;

        LinkedHashMap.Entry<String, String> firstArtist = getArtistsPosition(position * 2);
        final String firstArtistName = firstArtist.getKey();
        String firstTopVideoId = firstArtist.getValue();
        String firstThumbnailURL = "https://img.youtube.com/vi/" + firstTopVideoId + "/0.jpg";

        LinkedHashMap.Entry<String, String> secondArtist = getArtistsPosition(position * 2 + 1);
        final String secondArtistName = secondArtist.getKey();
        String secondTopVideoId = secondArtist.getValue();
        String secondThumbnailURL = "https://img.youtube.com/vi/" + secondTopVideoId + "/0.jpg";

        ImageView im = (ImageView) ((LinearLayout)((ConstraintLayout) holder.mLayout.getChildAt(0)).getChildAt(1)).getChildAt(0);
        final ProgressBar pb = (ProgressBar) (((ConstraintLayout) holder.mLayout.getChildAt(0)).getChildAt(0));

        if(mThumbnails.get(position * 2) != null) {
            pb.setVisibility(View.INVISIBLE);
            im.setImageDrawable(mThumbnails.get(position * 2));
            TextView t = (TextView) ((LinearLayout) ((ConstraintLayout) h.mLayout.getChildAt(0)).getChildAt(1)).getChildAt(1);
            t.setText(firstArtistName);
        } else {

            Glide.with(sActivity).load(firstThumbnailURL).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    pb.setVisibility(View.INVISIBLE);
                    TextView t = (TextView) ((LinearLayout) ((ConstraintLayout) h.mLayout.getChildAt(0)).getChildAt(1)).getChildAt(1);
                    t.setText(firstArtistName);
                    mThumbnails.set(pos * 2, resource);
                    return false;
                }
            }).into(im);
        }


        ImageView im2 = (ImageView) ((LinearLayout)((ConstraintLayout) holder.mLayout.getChildAt(1)).getChildAt(1)).getChildAt(0);
        final ProgressBar pb2 = (ProgressBar) (((ConstraintLayout) holder.mLayout.getChildAt(1)).getChildAt(0));



        if(mThumbnails.get(position * 2 + 1) != null) {
            pb2.setVisibility(View.INVISIBLE);
            im2.setImageDrawable(mThumbnails.get(position * 2 + 1));
            TextView t2 = (TextView) ((LinearLayout) ((ConstraintLayout) h.mLayout.getChildAt(1)).getChildAt(1)).getChildAt(1);
            t2.setText(secondArtistName);
        } else {

            Glide.with(sActivity).load(secondThumbnailURL).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    pb2.setVisibility(View.INVISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    TextView t2 = (TextView) ((LinearLayout) ((ConstraintLayout) h.mLayout.getChildAt(1)).getChildAt(1)).getChildAt(1);
                    t2.setText(secondArtistName);
                    mThumbnails.set(pos * 2 + 1, resource);
                    return false;
                }
            }).into(im2);

        }
    }

    private LinkedHashMap.Entry<String, String> getArtistsPosition(int position) {
        if (mList == null) {
            mList = new ArrayList<>();
            for (Map.Entry<String, String> entry : mArtistsTopSongs.entrySet()) {
                mList.add(entry);
            }
        }
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mArtistsTopSongs.size() / 2;
    }

}
