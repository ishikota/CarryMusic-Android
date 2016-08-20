package jp.carrymusic.ui;

import android.support.v4.media.MediaMetadataCompat;

public class Contract {

    public interface MusicListPresenterContract {
        void onBottomSheetStateChanged(int newState);
        void onBottomSheetSlide(float slideOffset);
        void onPlayingPositionUpdated(int position);
        void onPlaybackStateChanged(boolean enableToPlay);
        void onMetadataChanged(MediaMetadataCompat metadata);
    }

    public interface CompactControllerViewContract {
        void onMusicTitleClicked();
        void onPlayRequested();
        void onPauseRequested();
    }

    public interface FullControllerViewContract {
        void onSeekBarReleased(int progress);
        void onPlayRequested();
        void onPauseRequested();
        void onSkipToNextRequested();
        void onSkipToPreviousRequested();
    }
}
