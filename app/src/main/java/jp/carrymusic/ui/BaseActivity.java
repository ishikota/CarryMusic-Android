package jp.carrymusic.ui;


import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;

import jp.carrymusic.MediaBrowserProvider;
import jp.carrymusic.MusicService;
import jp.carrymusic.utils.PermissionHelper;

/*
    - Register MediaBrowserCompat to retrieve controller in Child class.
    - Manage MediaBrowserCompat connection in onStart/onStop.
 */
public class BaseActivity extends AppCompatActivity implements MediaBrowserProvider {

    private MediaBrowserCompat mMediaBrowser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionHelper.resolvePermission(BaseActivity.this);
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaBrowser.disconnect();
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        setSupportMediaController(mediaController);
    }

    private MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                connectToSession(mMediaBrowser.getSessionToken());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

}
