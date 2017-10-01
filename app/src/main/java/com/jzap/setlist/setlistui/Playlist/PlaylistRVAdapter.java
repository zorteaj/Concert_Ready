package com.jzap.setlist.setlistui.Playlist;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.MessageTypes;
import com.jzap.setlist.setlistui.R;

import es.claucookie.miniequalizerlibrary.EqualizerView;

/**
 * Created by JZ_W541 on 9/19/2017.
 */

public class PlaylistRVAdapter extends RecyclerView.Adapter<PlaylistRVAdapter.ViewHolder> {

    private static final String TAG = Config.TAG_HEADER + "PlaylistRVAdapter";

    Playlist mPlaylist;
    private Handler mHandler;
    private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLayout;

        public ViewHolder(LinearLayout layout) {
            super(layout);
            mLayout = layout;
            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Log.e(TAG, "Adapter position is NO_POSITION");
                        // TODO: Continue or return?
                    }
                    mPlaylist.onCurrentSongEnded();
                    mHandler.obtainMessage(MessageTypes.SONG_CLICKED, mPlaylist.getSong(position)).sendToTarget();
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlaylistRVAdapter(Context context, Handler handler, Playlist playlist) {
        mContext = context;
        mHandler = handler;
        mPlaylist = playlist;
        mPlaylist.setAdapter(this);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlaylistRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout l = (LinearLayout) inflater.inflate(R.layout.song_layout, parent, false);

        PlaylistRVAdapter.ViewHolder vh = new PlaylistRVAdapter.ViewHolder(l);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PlaylistRVAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        // TODO: I suspect to do this well, I'd have to make a custom layout

        Playlist.Song song = mPlaylist.getSong(position);

        ViewSwitcher vs = (ViewSwitcher) holder.mLayout.getChildAt(0);

        LinearLayout l = (LinearLayout) holder.mLayout.getChildAt(1);
        TextView t2 = (TextView) l.getChildAt(0);
        ProgressBar p = (ProgressBar) l.getChildAt(1);

        t2.setText(song.name);

        double topSongCount = (double) mPlaylist.getSong(0).count;
        double ratio = ((double) song.count)/ topSongCount;
        //Log.d(TAG, "Top song count = " + topSongCount + ", song count = " + String.valueOf(song.count) + ", ratio = " + ratio);
        p.setProgress((int)(ratio * 100));

        EqualizerView equalizer = (EqualizerView) vs.getChildAt(1);

        if (song.state == Playlist.Song.PLAYING) {
            t2.setTypeface(null, Typeface.BOLD);
            equalizer.animateBars();
            vs.setDisplayedChild(1);
        } else if (song.state == Playlist.Song.STOPPED) {
            equalizer.stopBars();
            TextView t1 = (TextView) vs.getChildAt(0);
            t1.setText(String.valueOf(song.position + 1));
            t2.setTypeface(null, Typeface.NORMAL);
            vs.setDisplayedChild(0);
        } else if (song.state == Playlist.Song.PAUSED) {
            equalizer.stopBars();
            vs.setDisplayedChild(1);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPlaylist.getSongs().size();
    }
}
