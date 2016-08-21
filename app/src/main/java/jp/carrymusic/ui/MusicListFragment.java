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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import jp.carrymusic.MusicService;
import jp.carrymusic.R;
import jp.carrymusic.databinding.FragmentMusicListBinding;
import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.utils.DownloadHelper;
import jp.carrymusic.utils.MusicListDivider;

public class MusicListFragment extends Fragment implements MusicListAdapter.MusicListClickListener,
        Contract.CompactControllerViewContract, Contract.FullControllerViewContract {


    private static final String TAG = MusicListFragment.class.getSimpleName();

    private MusicProvider mMusicProvider;

    private SearchMenuPresenter mSearchMenuPresenter;

    private CompactMediaControllerPresenter mCompactControllerPresenter;

    private FullMediaControllerPresenter mFullControllerPresenter;

    FragmentMusicListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMusicProvider = new MusicProvider();
        mSearchMenuPresenter = new SearchMenuPresenter();
        mCompactControllerPresenter = new CompactMediaControllerPresenter(this);
        mFullControllerPresenter = new FullMediaControllerPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false);

        setupToolbar(binding.toolbar);
        setupRecyclerView(binding.recyclerView, getContext());
        setupBottomSheet(BottomSheetBehavior.from(binding.bottomSheet));

        mCompactControllerPresenter.setup(binding.mediaControllerCompact);
        mFullControllerPresenter.setup(binding.mediaControllerFull);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.main_activity_actions, menu);
        mSearchMenuPresenter.setup(menu);
    }

    private void setupToolbar(Toolbar toolbar) {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    }

    private void setupRecyclerView(RecyclerView recyclerView, Context context) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new MusicListDivider(context));
        MusicListAdapter adapter = new MusicListAdapter(context, mMusicProvider.getAllMusic(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupBottomSheet(final BottomSheetBehavior behavior) {
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d(TAG, "BottomSheetBehavior.onStateChanged = " + newState);
                mCompactControllerPresenter.onBottomSheetStateChanged(newState);
                mFullControllerPresenter.onBottomSheetStateChanged(newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d(TAG, "BottomSheetBehavior.onSlide = " + slideOffset);
                mCompactControllerPresenter.onBottomSheetSlide(slideOffset);
                mFullControllerPresenter.onBottomSheetSlide(slideOffset);
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
                mCompactControllerPresenter.onPlayingPositionUpdated(currentDuration);
                mFullControllerPresenter.onPlayingPositionUpdated(currentDuration);
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

        mCompactControllerPresenter.onMetadataChanged(metadata);
        mFullControllerPresenter.onMetadataChanged(metadata);
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

        mCompactControllerPresenter.onPlaybackStateChanged(enablePlay);
        mFullControllerPresenter.onPlaybackStateChanged(enablePlay);
    }

    /*
        Callback called from MusicListAdapter
    */
    @Override
    public void onMusicSelected(MusicProviderSource model) {
        if (model.getVideoPath() == null) {
            downloadVideoToDevice(model.getVideoId());
        } else {
            MediaControllerCompat controller = getActivity().getSupportMediaController();
            int state = controller.getPlaybackState().getState();
            String currentMusicId = controller.getMetadata() != null ?
                    controller.getMetadata().getDescription().getMediaId() : "";

            Log.d(TAG, "state = " + state);
            if (state == PlaybackStateCompat.STATE_PLAYING && currentMusicId.equals(model.getVideoId())) {
                onPauseRequested();
            } else {
                playFromModel(model);
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

    /*
        Contract method for CompactMediaControllerPresenter
     */

    @Override
    public void onMusicTitleClicked() {
        BottomSheetBehavior behavior = BottomSheetBehavior.from(binding.bottomSheet);
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private MediaControllerCompat.TransportControls getMediaTransportControl() {
        return getActivity().getSupportMediaController().getTransportControls();
    }

    private void playFromModel(MusicProviderSource model) {
        getMediaTransportControl().playFromMediaId(model.getVideoId(), null);
    }

    @Override
    public void onPlayRequested() {
        getMediaTransportControl().play();
    }

    @Override
    public void onPauseRequested() {
        getMediaTransportControl().pause();
    }


    /*
        Contract method for FullMediaControllerPresenter
     */

    @Override
    public void onSeekBarReleased(int progress) {
        getMediaTransportControl().seekTo(progress);
    }

    @Override
    public void onSkipToNextRequested() {
        getMediaTransportControl().skipToNext();
    }

    @Override
    public void onSkipToPreviousRequested() {
        getMediaTransportControl().skipToPrevious();
    }
}
