package com.jzap.setlist.setlistui.Playlist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.jzap.setlist.setlistui.Config;
import com.jzap.setlist.setlistui.MessageTypes;
import com.jzap.setlist.setlistui.R;
import com.jzap.setlist.setlistui.SetListFm.Requestor.TourPlaylistCreator;
import com.jzap.setlist.setlistui.YouTubeAPIAdapter;
//import com.jzap.setlist.setlistui.SetListFm.SetListFmAPIAdapter;

/**
 * Created by JZ_W541 on 9/18/2017.
 */

public class PlaylistActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = Config.TAG_HEADER + "PlaylistActivity";
    private static final int RECOVERY_REQUEST = 1;

    private RecyclerView mPlaylistView;
    private RecyclerView.LayoutManager mPlaylistLayoutManager;
    private RecyclerView.Adapter mPlaylistRVAdapter;

    private YouTubePlayer mPlayer;
    private YouTubePlayerView mYouTubeView;
    private YouTubeAPIAdapter mYouTubeAPIAdapter;

    private ViewSwitcher mPlayPause;
    private ImageView mSkipPrevious;
    private ImageView mSkipNext;

    private TextView mPlaylistArtist;
    private TextView mPlaylistEmpty;
    private boolean mEmptyPlaylist = false;

    private ProgressBar mPlaylistProgress;

    private Handler mHandler;

    private String mArtistName;
    private Playlist mPlaylist;

    private boolean mReceivedFirstSong = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        mPlaylistProgress = (ProgressBar) findViewById(R.id.playlistProgress);
        mPlaylistEmpty = (TextView) findViewById(R.id.playlistEmpty);
        mPlaylistEmpty.setVisibility(View.INVISIBLE);

        mYouTubeView = (YouTubePlayerView) findViewById(R.id.youtubePlayer);
        mYouTubeView.initialize(Config.YOUTUBE_API_KEY, this);

        setupHandler();
      //  mSetListFmAPIAdapter = new SetListFmAPIAdapter(this, mHandler);
        mYouTubeAPIAdapter = new YouTubeAPIAdapter(mHandler);

        Uri uri = getIntent().getData();

        if(uri == null) {
            Log.e(TAG, "Uri is null");
            // TODO: Throw exception or something
        } else {
            Log.d(TAG, "Intent data string = " + uri.toString());
            if(savedInstanceState == null) {
                mArtistName = (uri.getLastPathSegment());
                setupPlaylistDescription();
                requestSongs(mArtistName);
            }
        }

        setupPlayerControls();
    }

    private void setupHandler() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch(inputMessage.what) {
                    case MessageTypes.PLAYLIST_RESULT:
                        mPlaylist = (Playlist) inputMessage.obj;
                        displayPlaylist(mPlaylist);
                        if(!mPlaylist.getSongs().isEmpty()) {
                            mYouTubeAPIAdapter.requestVideoIds(mPlaylist);
                        }
                        break;
                    case MessageTypes.VIDEO_IDS_RESPONSE:
                        // Only redisplay playlist if clean() caused changes
                        if(mPlaylist.clean()) {
                            displayPlaylist(mPlaylist);
                        }
                        if(!mReceivedFirstSong) {
                            // only load a video if we failed in our attempt to quickly load the first video
                            // while waiting on the rest to be prepared
                            if(mPlayer!= null) {
                                loadNextVideo();
                            } else {
                                Log.e(TAG, "YouTube Player is not yet initialized");
                                // TODO: Wait for initialiation success of failure
                                // If success, load these videos
                                // If failure, handle somehow
                                /*new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });*/
                            }
                        }
                        break;
                    case MessageTypes.FIRST_SONG_RESPONSE:
                        Playlist.Song firstSong = (Playlist.Song) inputMessage.obj;
                        if(mPlayer != null) {
                            mReceivedFirstSong = true;
                            mPlayer.loadVideo(firstSong.videoId);
                        }
                        break;
                    case MessageTypes.SONG_CLICKED:
                        // don't respond to the clicked song unless our list of videos has been prepared
                        // (this is a minor inconvenience of loading our list of playlist quickly before the
                        // videos have been loaded
                        if(mPlaylist == null)
                        {
                            break;
                        }
                        Playlist.Song song = (Playlist.Song) inputMessage.obj;
                        if(mPlayer != null) {
                            mPlayer.loadVideo(song.videoId);
                            mPlaylist.setPosition(song.position);
                        }
                        break;
                }
            }
        };
    }

    private void loadNextVideo() {
        if(mPlayer == null) {
            Log.e(TAG, "mPlayer is null");
        } else {
            try {
                // TODO: If this method is called after the first video finishes, and the
                // rest of the videos are not yet ready, the user will see that we are at the end of
                // the "playlist", because the next song will not be played.  Very much a corner case,
                // but should be dealt with

                mPlaylist.onCurrentSongEnded();

                if(mPlaylist.setNextPosition()) {
                    mPlayer.loadVideo(mPlaylist.getSong(mPlaylist.getPosition()).videoId);
                } else {
                    if(!mPlaylist.getSongs().isEmpty()) {
                        mPlaylist.setPosition(0);
                        mPlayer.loadVideo(mPlaylist.getSong(mPlaylist.getPosition()).videoId);
                    }
                }

            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void loadPreviousVideo() {
        if(mPlayer == null) {
            Log.e(TAG, "mPlayer is null");
        } else {
            try {
                // TODO: If this method is called after the first video finishes, and the
                // rest of the videos are not yet ready, the user will see that we are at the end of
                // the "playlist", because the next song will not be played.  Very much a corner case,
                // but should be dealt with
                mPlaylist.onCurrentSongEnded();

                mPlaylist.setPreviousPosition();
                mPlayer.loadVideo(mPlaylist.getSong(mPlaylist.getPosition()).videoId);

            } catch (IllegalStateException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void requestSongs(String verifiedArtistName) {
        new TourPlaylistCreator(verifiedArtistName, this, new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch(inputMessage.what) {
                    case MessageTypes.PLAYLIST_CREATED:
                        mPlaylist = (Playlist) inputMessage.obj;
                        if(mPlaylist == null || mPlaylist.getSongs().isEmpty()) {
                            emptyPlaylistCleanUp();
                            break;
                        }
                        displayPlaylist(mPlaylist);
                        if(!mPlaylist.getSongs().isEmpty()) {
                            mYouTubeAPIAdapter.requestVideoIds(mPlaylist);
                        }
                        break;
                }
            }
        }).request();
    }

    private void displayPlaylist(Playlist playlist) {
        mPlaylistProgress.setVisibility(View.INVISIBLE);

        if(mPlaylist == null || mPlaylist.getSongs().isEmpty()) {
            emptyPlaylistCleanUp();
            return;
        }

        mPlaylistView = (RecyclerView) findViewById(R.id.playlist);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mPlaylistView.setHasFixedSize(true);

        mPlaylistLayoutManager = new LinearLayoutManager(this);
        mPlaylistView.setLayoutManager(mPlaylistLayoutManager);
        mPlaylistView.setLayoutManager(mPlaylistLayoutManager);

        mPlaylistRVAdapter = new PlaylistRVAdapter(this, mHandler, playlist);
        mPlaylistView.setAdapter(mPlaylistRVAdapter);
    }

    private void emptyPlaylistCleanUp() {
        mEmptyPlaylist = true;
        mPlaylistProgress.setVisibility(View.INVISIBLE);
        mPlaylistEmpty.setVisibility(View.VISIBLE);
        if(mPlayer != null) {
            mPlayer.release();
        }
        mPlayPause.setVisibility(View.GONE);
        mSkipNext.setVisibility(View.GONE);
        mSkipPrevious.setVisibility(View.GONE);
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        if(mEmptyPlaylist) {
            Log.d(TAG, "Player initialized after discovery of empty playlist - releasing playlist now");
            player.release();
            return;
        }

        if (!wasRestored) {
            mPlayer = player;
            mPlayer.setShowFullscreenButton(false);
            //mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            mPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
               @Override
               public void onLoading() {
                   mPlayPause.setDisplayedChild(1);
                   mPlaylist.pause();
               }

               @Override
               public void onLoaded(String s) {
                   mPlayPause.setDisplayedChild(0);
               }

               @Override
               public void onAdStarted() {

               }

               @Override
               public void onVideoStarted() {
                   mPlaylist.play();
               }

               @Override
               public void onVideoEnded() {
                   mPlaylist.onCurrentSongEnded();
                   loadNextVideo();
               }

               @Override
               public void onError(YouTubePlayer.ErrorReason errorReason) {

               }

           });

            mPlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                @Override
                public void onPlaying() {
                    mPlayPause.setDisplayedChild(0);
                    mPlaylist.play();
                }

                @Override
                public void onPaused() {
                    mPlayPause.setDisplayedChild(1);
                    mPlaylist.pause();
                }

                @Override
                public void onStopped() {

                }

                @Override
                public void onBuffering(boolean b) {

                }

                @Override
                public void onSeekTo(int i) {

                }
            });
        }

    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            //String error = String.format(getString(R.string.player_error), errorReason.toString());
            String error = errorReason.toString();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(Config.YOUTUBE_API_KEY, this);
        }
    }

    protected Provider getYouTubePlayerProvider() {
        return mYouTubeView;
    }

    private void setupPlayerControls() {
        setupPlayPause();
        setupSkipNext();
        setupSkipPrevious();
    }

    private void setupSkipNext() {
        mSkipNext = (ImageView) findViewById(R.id.skipNext);
        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadNextVideo();
            }
        });
    }

    private void setupSkipPrevious() {
        mSkipPrevious = (ImageView) findViewById(R.id.skipPrevious);
        mSkipPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPreviousVideo();
            }
        });
    }

    private void setupPlayPause() {
        mPlayPause = (ViewSwitcher) findViewById(R.id.playPause);
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlayer.isPlaying()) {
                    mPlayer.pause();
                } else {
                    mPlayer.play();
                }
            }
        });
    }

    private void setupPlaylistDescription() {
        mPlaylistArtist = (TextView) findViewById(R.id.playlistArtist);
        mPlaylistArtist.setText(mArtistName);
    }

}
