package jp.carrymusic.ui;

import android.support.v4.media.MediaMetadataCompat;
import android.view.View;
import android.widget.SeekBar;

import jp.carrymusic.databinding.MediaControllerFullBinding;

public class FullMediaControllerPresenter implements Contract.MusicListPresenterContract {

    private final Contract.FullControllerViewContract mView;

    private MediaControllerFullBinding mRoot;

    public FullMediaControllerPresenter(Contract.FullControllerViewContract mView) {
        this.mView = mView;
    }

    public void setup(MediaControllerFullBinding view) {
        mRoot = view;

        mRoot.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRoot.seekbarCaption.setText(genSeekBarCaption(mRoot.seekbar, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {  /* do nothing */}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mView.onSeekBarReleased(seekBar.getProgress());
            }
        });

        mRoot.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.onSkipToNextRequested();
            }
        });

        mRoot.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.onSkipToPreviousRequested();
            }
        });
    }

    @Override
    public void onBottomSheetStateChanged(int newState) {
        // do nothing
    }

    @Override
    public void onBottomSheetSlide(float slideOffset) {
        mRoot.getRoot().setAlpha(slideOffset);
    }

    @Override
    public void onPlayingPositionUpdated(int position) {
        mRoot.seekbar.setProgress(position);
        mRoot.seekbarCaption.setText(genSeekBarCaption(mRoot.seekbar, position));
    }

    @Override
    public void onPlaybackStateChanged(boolean enableToPlay) {
        if (enableToPlay) {
            // set play action on media button
            mRoot.btnPlayStop.setImageResource(android.R.drawable.ic_media_play);
            mRoot.btnPlayStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.onPlayRequested();
                }
            });
        } else {
            // set pause action on media button
            mRoot.btnPlayStop.setImageResource(android.R.drawable.ic_media_pause);
            mRoot.btnPlayStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.onPauseRequested();
                }
            });
        }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        int musicLengthInSecond =
                (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) * 1000;
        mRoot.seekbar.setMax(musicLengthInSecond);
        mRoot.seekbarCaption.setText(genSeekBarCaption(mRoot.seekbar, musicLengthInSecond));
    }

    private String genSeekBarCaption(SeekBar seekBar, int current) {
        current /= 1000;
        int max = seekBar.getMax() / 1000;
        return String.format("%d:%02d / %d:%02d",
                fetchMin(current), fetchSec(current), fetchMin(max), fetchSec(max));
    }

    private int fetchMin(int position) {
        return position / 60;
    }

    private int fetchSec(int position) {
        return position % 60;
    }
}
