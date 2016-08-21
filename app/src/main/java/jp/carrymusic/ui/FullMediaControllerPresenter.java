package jp.carrymusic.ui;

import android.support.v4.media.MediaMetadataCompat;
import android.view.View;
import android.widget.SeekBar;

import jp.carrymusic.R;
import jp.carrymusic.databinding.MediaControllerFullBinding;
import jp.carrymusic.utils.UIHelper;

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
                mRoot.seekbarCurrentCaption.setText(UIHelper.genDurationString(progress / 1000));
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
        // do nothing
    }

    @Override
    public void onPlayingPositionUpdated(int position) {
        mRoot.seekbar.setProgress(position);
        mRoot.seekbarCurrentCaption.setText(
                UIHelper.genDurationString(mRoot.seekbar.getProgress() / 1000));
    }

    @Override
    public void onPlaybackStateChanged(boolean enableToPlay) {
        if (enableToPlay) {
            // set play action on media button
            mRoot.btnPlayStop.setImageResource(R.drawable.ic_play_arrow_black_48dp);
            mRoot.btnPlayStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.onPlayRequested();
                }
            });
        } else {
            // set pause action on media button
            mRoot.btnPlayStop.setImageResource(R.drawable.ic_pause_black_48dp);
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
        mRoot.seekbarMaxCaption.setText(UIHelper.genDurationString(mRoot.seekbar.getMax() / 1000));
    }

}
