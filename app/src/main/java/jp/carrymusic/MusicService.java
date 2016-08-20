package jp.carrymusic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.playback.LocalPlayback;
import jp.carrymusic.playback.Playback;
import jp.carrymusic.playback.PlaybackManager;
import jp.carrymusic.playback.QueueManager;

import static jp.carrymusic.utils.MediaIDHelper.MEDIA_ID_ROOT;


public class MusicService extends MediaBrowserServiceCompat
        implements PlaybackManager.PlaybackServiceCallback {

    private static final String TAG = MusicService.class.getSimpleName();

    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.example.android.uamp.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";

    // Values used for sync Playback current position with UI.
    public static final String SESSION_EVENT_NOTIFY_CURRENT_POSITION = "jp.carrymusic.notify_current_position";
    public static final String EXTRA_DURATION = "jp.carrymusic.extra_duration";

    // While playback plays, this runnable post current playing position through Session object.
    private Runnable mSeekBarSyncAction = new Runnable() {

        @Override
        public void run() {
            Playback playback = mPlaybackManager.getPlayback();
            if (playback.isPlaying()) {
                Log.i(TAG, "mSeekBarSyncAction current position = " + playback.getCurrentStreamPosition());
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_DURATION, playback.getCurrentStreamPosition());
                mSession.sendSessionEvent(SESSION_EVENT_NOTIFY_CURRENT_POSITION, bundle);
                mSeekBarSyncHandler.postDelayed(mSeekBarSyncAction, 1000);
            }
        }
    };

    private MediaSessionCompat mSession;
    private PlaybackManager mPlaybackManager;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private final Handler mSeekBarSyncHandler = new Handler();
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;
    MediaNotificationManager mMediaNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        MusicProvider musicProvider = new MusicProvider();
        QueueManager queueManager = new QueueManager(
                musicProvider, new QueueManager.MetadataUpdateListener() {

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                mSession.setMetadata(metadata);
            }

            @Override
            public void onMetadataRetrieveError() {
                mPlaybackManager.updatePlaybackState("Unable to retrieve metadata.");
            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {
                mPlaybackManager.handlePlayRequest();
            }

            @Override
            public void onQueueUpdated(String title, List<MusicProviderSource> newQueue) {
                // TODO
                //mSession.setQueue(newQueue);
                mSession.setQueueTitle(title);
            }
        });

        mPlaybackManager = new PlaybackManager(this,
                musicProvider, queueManager, new LocalPlayback(this));

        // Start a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPlaybackManager.updatePlaybackState(null);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);

            // Receive pause intent from notification controller or when headphone is disconnected
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // Service is being killed, so make sure we release our resources
        mPlaybackManager.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();
        mSession.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null); // TODO provide appropriate items
    }

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        if (!mSession.isActive()) {
            mSession.setActive(true);
        }

        mSeekBarSyncHandler.post(mSeekBarSyncAction);
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        mSeekBarSyncHandler.removeCallbacks(mSeekBarSyncAction);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

}
