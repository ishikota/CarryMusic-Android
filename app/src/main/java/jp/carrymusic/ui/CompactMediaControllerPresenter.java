package jp.carrymusic.ui;

import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.media.MediaMetadataCompat;
import android.view.View;

import jp.carrymusic.databinding.MediaControllerCompactBinding;

public class CompactMediaControllerPresenter implements Contract.MusicListPresenterContract {

    private final Contract.CompactControllerViewContract mView;

    private MediaControllerCompactBinding mRoot;

    public CompactMediaControllerPresenter(Contract.CompactControllerViewContract mView) {
        this.mView = mView;
    }

    public void setup(MediaControllerCompactBinding view) {
        mRoot = view;

        mRoot.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.onMusicTitleClicked();
            }
        });
    }

    // If bottom sheet is expanded then full controller is available.
    // So compact controller should hide its media control item.
    @Override
    public void onBottomSheetStateChanged(int state) {
        if (BottomSheetBehavior.STATE_EXPANDED == state) {
            mRoot.btnMediaControl.setEnabled(false);
            mRoot.btnMediaControl.setVisibility(View.GONE);
        } else if (BottomSheetBehavior.STATE_COLLAPSED == state) {
            mRoot.btnMediaControl.setEnabled(true);
            mRoot.btnMediaControl.setVisibility(View.VISIBLE);
        }
    }

    // Update alpha to become invisible when bottom sheet is fully expanded.
    @Override
    public void onBottomSheetSlide(float slideOffset) {
        mRoot.btnMediaControl.setAlpha(1 - slideOffset);
    }

    @Override
    public void onPlayingPositionUpdated(int position) {
        // do nothing
    }

    @Override
    public void onPlaybackStateChanged(boolean enableToPlay) {
        if (enableToPlay) {
            // set play action on media button
            mRoot.btnMediaControl.setImageResource(android.R.drawable.ic_media_play);
            mRoot.btnMediaControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.onPlayRequested();
                }
            });
        } else {
            // set pause action on media button
            mRoot.btnMediaControl.setImageResource(android.R.drawable.ic_media_pause);
            mRoot.btnMediaControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.onPauseRequested();
                }
            });
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        mRoot.musicTitle.setText(metadata.getDescription().getTitle());
    }


}
