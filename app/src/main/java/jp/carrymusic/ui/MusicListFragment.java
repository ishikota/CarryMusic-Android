package jp.carrymusic.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import io.realm.Realm;
import jp.carrymusic.MusicService;
import jp.carrymusic.R;
import jp.carrymusic.databinding.FragmentMusicListBinding;
import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.utils.DownloadHelper;
import jp.carrymusic.utils.MusicListDivider;
import jp.carrymusic.utils.UndoSnackbar;

public class MusicListFragment extends Fragment implements MusicListAdapter.MusicListClickListener,
        Contract.CompactControllerViewContract, Contract.FullControllerViewContract,
        SortMenuPresenter.SortCallback , EndpointMenuPresenter.EndpointUpdatedListener {


    private static final String TAG = MusicListFragment.class.getSimpleName();

    private MusicProvider mMusicProvider;

    private SearchMenuPresenter mSearchMenuPresenter;

    private SortMenuPresenter mSortMenuPresenter;

    private EndpointMenuPresenter mEndpointMenuPresenter;

    private CompactMediaControllerPresenter mCompactControllerPresenter;

    private FullMediaControllerPresenter mFullControllerPresenter;

    FragmentMusicListBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMusicProvider = new MusicProvider();
        mSearchMenuPresenter = new SearchMenuPresenter();
        mSortMenuPresenter = new SortMenuPresenter();
        mEndpointMenuPresenter = new EndpointMenuPresenter();
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
        mEndpointMenuPresenter.setup(this, menu);
        mSortMenuPresenter.setup(this, menu, mMusicProvider);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: item = " + item);
        switch (item.getItemId()) {
            case R.id.action_sort:
                mSortMenuPresenter.onSortStarted(item, binding.recyclerView);
                break;
            case R.id.action_sort_done:
                mSortMenuPresenter.onSortFinished(item, binding.recyclerView);
                break;
            case R.id.action_sort_cancel:
                mSortMenuPresenter.onSortCanceled(item, binding.recyclerView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar(Toolbar toolbar) {
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
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
            downloadVideoToDevice(model);
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

    private void downloadVideoToDevice(final MusicProviderSource model) {
        Snackbar.make(binding.containerForSnackBar,
                R.string.msg_start_download_cache, Snackbar.LENGTH_SHORT).show();
        updateDownloadingState(model, true);
        DownloadHelper.downloadItemIntoDevice(getContext(), model.getVideoId(),
                new DownloadHelper.DownloadCallback() {
                    @Override
                    public void onSuccess() {
                        updateDownloadingState(model, false);
                    }

                    @Override
                    public void onError(String message) {
                        updateDownloadingState(model, false);
                        Snackbar.make(binding.containerForSnackBar,
                                R.string.msg_failed_to_download_cache, Snackbar.LENGTH_LONG)
                                .setAction(R.string.retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        downloadVideoToDevice(model);
                                    }
                                }).show();
                    }
                });
    }

    private void updateDownloadingState(final MusicProviderSource model, final boolean stateToUpdate) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                model.setDownloading(stateToUpdate);
            }
        });
    }

    @Override
    public void onMoreActionClicked(final MusicProviderSource model, View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_delete_data) {
                    deleteMusicSourceWithUndoAction(model);
                } else if (item.getItemId() == R.id.action_delete_cache) {
                    deleteAudioCacheWithUndoAction(model);
                }
                return false;
            }
        });
        popup.inflate(R.menu.music_list_action_more);

        // hide useless choice
        if (model.getVideoPath() == null) {
            popup.getMenu().removeItem(R.id.action_delete_cache);
        }

        popup.show();
    }

    private void deleteMusicSourceWithUndoAction(final MusicProviderSource model) {
        updateTrashStatus(model, true);
        UndoSnackbar.makeLong(binding.containerForSnackBar, R.string.delete_data, new UndoSnackbar.Callback() {
            @Override
            public void onUndo() {
                updateTrashStatus(model, false);
            }

            @Override
            public void onDismissWithoutRedo(Snackbar snackbar) {
                deleteAudioFromDevice(model.getVideoPath());
                deleteMusicSource(model);
            }
        }).show();
    }

    private void deleteAudioCacheWithUndoAction(final MusicProviderSource model) {
        final String videoPathMemo = model.getVideoPath();
        updateVideoPath(model, null);
        UndoSnackbar.makeLong(binding.containerForSnackBar, R.string.delete_data, new UndoSnackbar.Callback() {
            @Override
            public void onUndo() {
                updateVideoPath(model, videoPathMemo);
            }

            @Override
            public void onDismissWithoutRedo(Snackbar snackbar) {
                deleteAudioFromDevice(videoPathMemo);
            }
        }).show();
    }

    private void updateTrashStatus(final MusicProviderSource music,final boolean trashed) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                music.setTrashed(trashed);
            }
        });
    }

    private void deleteMusicSource(final MusicProviderSource music) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                music.deleteFromRealm();
            }
        });
    }

    private void updateVideoPath(final MusicProviderSource music, final String pathToUpdate) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                music.setVideoPath(pathToUpdate);
            }
        });
    }

    private void deleteAudioFromDevice(final String filePath){
        String tag = "deleteAudioFromDevice:";
        if (filePath != null) {
            File target = new File(filePath);
            if (target.delete()) {
                Log.d(TAG, String.format(
                        "%s Successfully deleted audio file [%s]", tag, filePath));
            } else {
                Log.w(TAG, String.format(
                        "%s Failed to delete audio file [%s].", tag, filePath));
            }
        } else {
            Log.d(TAG,
                    String.format("%s audio file [%s] was already deleted", tag,filePath));
        }
    }

    /*
        Callback from SortMenuPresenter
     */

    @Override
    public void onSortStarted() {
        onPauseRequested();
        binding.bottomSheet.setVisibility(View.GONE);
        binding.toolbar.setTitle(R.string.title_sort_item);
    }

    @Override
    public void onSortCanceled() {
        binding.bottomSheet.setVisibility(View.VISIBLE);
        binding.toolbar.setTitle(R.string.app_name);
    }

    @Override
    public void onSortFinished(final List<MusicProviderSource> items) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < items.size(); i++) {
                    MusicProviderSource sortedItem = items.get(i);
                    sortedItem.setPosition(i);
                }
            }
        });
        binding.bottomSheet.setVisibility(View.VISIBLE);
        binding.toolbar.setTitle(R.string.app_name);
    }

    /*
        Callback from EndpointMenuPresenter
     */

    @Override
    public SharedPreferences getPreference() {
        return getActivity().getSharedPreferences(
                EndpointMenuPresenter.PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void updated() {
        Snackbar.make(binding.containerForSnackBar,
                R.string.endpoint_update_msg, Snackbar.LENGTH_SHORT).show();
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
