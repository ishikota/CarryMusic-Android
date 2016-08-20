package jp.carrymusic.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import jp.carrymusic.MusicService;
import jp.carrymusic.R;
import jp.carrymusic.databinding.FragmentMusicListBinding;
import jp.carrymusic.databinding.MediaControllerCompactBinding;
import jp.carrymusic.databinding.MediaControllerFullBinding;
import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.utils.DividerItemDecoration;
import jp.carrymusic.utils.DownloadHelper;

public class MusicListFragment extends Fragment implements MusicListAdapter.MusicListClickListener {

    private static final String TAG = MusicListFragment.class.getSimpleName();

    private MusicProvider mMusicProvider;

    FragmentMusicListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMusicProvider = new MusicProvider();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);
        setupRecyclerView(binding.recyclerView, getContext());
        setupSeekBar(binding.mediaControllerFull.seekbar);
        setupMediaController(binding.mediaControllerCompact, binding.mediaControllerFull);
        setupBottomSheet(BottomSheetBehavior.from(binding.bottomSheet));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "fragment.onStart");
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "fragment.onStop");
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        if (controller != null) {
            controller.unregisterCallback(mCallback);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView, Context context) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context));
        MusicListAdapter adapter = new MusicListAdapter(context, mMusicProvider.getAllMusic(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.mediaControllerFull.seekbarCaption
                        .setText(genSeekBarCaption(seekBar, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getActivity().getSupportMediaController().getTransportControls().seekTo(seekBar.getProgress());
            }
        });
    }

    private void setupMediaController(
            MediaControllerCompactBinding compact, MediaControllerFullBinding full) {
        full.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportMediaController().getTransportControls().skipToNext();
            }
        });
        full.btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportMediaController().getTransportControls().skipToPrevious();
            }
        });
    }

    private void setupBottomSheet(BottomSheetBehavior behavior) {
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "BottomSheetBehavior.onStateChanged = " + newState);
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.mediaControllerCompact.btnMediaControl.setEnabled(false);
                    binding.mediaControllerCompact.btnMediaControl.setVisibility(View.GONE);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.mediaControllerCompact.btnMediaControl.setEnabled(true);
                    binding.mediaControllerCompact.btnMediaControl.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d(TAG, "BottomSheetBehavior.onSlide = " + slideOffset);
                binding.mediaControllerFull.getRoot().setAlpha(slideOffset);
                binding.mediaControllerCompact.btnMediaControl.setAlpha(1-slideOffset);
            }
        });
    }

    @Override
    public void onMusicSelected(MusicProviderSource model) {
        if (model.getVideoPath() == null) {
            downloadVideoToDevice(model.getVideoId());
        } else {
            MediaControllerCompat controller = getActivity().getSupportMediaController();
            int state = controller.getPlaybackState().getState();
            String currentMusicId = controller.getMetadata() != null ?
                    controller.getMetadata().getDescription().getMediaId() : "";

            Log.d(MusicListFragment.class.getSimpleName(), "state = " + state);
            if (state == PlaybackStateCompat.STATE_PLAYING && currentMusicId.equals(model.getVideoId())) {
                getActivity().getSupportMediaController().getTransportControls().pause();
            } else {
                getActivity().getSupportMediaController().getTransportControls().playFromMediaId(model.getVideoId(), null);
            }
        }
    }

    private void downloadVideoToDevice(final String videoId) {
        DownloadHelper.downloadItemIntoDevice(getContext(), videoId, new DownloadHelper.DownloadCallback() {
            @Override
            public void onSuccess() {
                // TODO should notify success in some way
            }

            @Override
            public void onError(String message) {
                // TODO should notify error in some way
            }
        });
    }

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            Log.d(TAG, "Received playback state change to state " + state.getState());
            MusicListFragment.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            Log.d(TAG, "Received metadata state change to mediaId=" +
                    metadata.getDescription().getMediaId() +
                    " song=" + metadata.getDescription().getTitle());
            MusicListFragment.this.onMetadataChanged(metadata);
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            Log.d(TAG, "Received session event : event = " + event);
            if (event.equals(MusicService.SESSION_EVENT_NOTIFY_CURRENT_POSITION)) {
                int currentDuration = extras.getInt(MusicService.EXTRA_DURATION);
                binding.mediaControllerFull.seekbar.setProgress(currentDuration);
                binding.mediaControllerFull.seekbarCaption
                        .setText(genSeekBarCaption(binding.mediaControllerFull.seekbar, currentDuration));
                Log.d(TAG, "Received current position : " + currentDuration);
            }
        }
    };


    public void onConnected() {
        MediaControllerCompat controller = getActivity().getSupportMediaController();
        Log.d(TAG, "onConnected, mediaController==null? " + (controller == null));
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d(TAG, "onMetadataChanged " + metadata);
        if (getActivity() == null) {
            Log.w(TAG, "onMetadataChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (metadata == null) {
            return;
        }

        int musicLengthInSecond =
                (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) * 1000;
        binding.mediaControllerFull.seekbar.setMax(musicLengthInSecond);
        binding.mediaControllerFull.seekbarCaption
                .setText(genSeekBarCaption(binding.mediaControllerFull.seekbar, musicLengthInSecond));
        binding.mediaControllerCompact.musicTitle.setText(metadata.getDescription().getTitle());
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged " + state);
        if (getActivity() == null) {
            Log.w(TAG, "onPlaybackStateChanged called when getActivity null," +
                    "this should not happen if the callback was properly unregistered. Ignoring.");
            return;
        }
        if (state == null) {
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Log.e(TAG, "error playbackstate: " + state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }
        if (enablePlay) {
            // set play icon
            binding.mediaControllerCompact.btnMediaControl.setImageResource(android.R.drawable.ic_media_play);
            binding.mediaControllerCompact.btnMediaControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportMediaController().getTransportControls().play();
                }
            });
            binding.mediaControllerFull.btnPlayStop.setImageResource(android.R.drawable.ic_media_play);
            binding.mediaControllerFull.btnPlayStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportMediaController().getTransportControls().play();
                }
            });
        } else {
            // set pause icon
            binding.mediaControllerCompact.btnMediaControl.setImageResource(android.R.drawable.ic_media_pause);
            binding.mediaControllerCompact.btnMediaControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportMediaController().getTransportControls().pause();
                }
            });
            binding.mediaControllerFull.btnPlayStop.setImageResource(android.R.drawable.ic_media_pause);
            binding.mediaControllerFull.btnPlayStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportMediaController().getTransportControls().pause();
                }
            });
        }
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
