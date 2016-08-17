package jp.carrymusic.ui;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import io.realm.Realm;
import jp.carrymusic.R;
import jp.carrymusic.api.CarryMusicApiClient;
import jp.carrymusic.databinding.FragmentMusicListBinding;
import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.utils.DividerItemDecoration;
import jp.carrymusic.utils.FileSaveHelper;
import retrofit.client.Response;
import rx.Subscriber;

public class MusicListFragment extends Fragment implements MusicListAdapter.MusicListClickListener {

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
        binding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                String videoId = binding.searchField.getText().toString();
                loadNewMusic(videoId);
            }
        });
        binding.musicController.btnPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicProviderSource model = Realm.getDefaultInstance()
                        .where(MusicProviderSource.class).isNotNull("videoPath").findFirst();
                Uri uri = Uri.fromFile(new File(model.getVideoPath()));
                MediaControllerCompat.TransportControls controller = getActivity().getSupportMediaController().getTransportControls();
                controller.playFromUri(uri, null);
            }
        });
        return binding.getRoot();
    }

    private void setupRecyclerView(RecyclerView recyclerView, Context context) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context));
        MusicListAdapter adapter = new MusicListAdapter(context, mMusicProvider.getAllMusic(), this);
        recyclerView.setAdapter(adapter);
    }

    private void loadNewMusic(String videoId) {
        CarryMusicApiClient.getInstance().createVideo(videoId)
                .subscribe(new Subscriber<MusicProviderSource>() {
                    @Override
                    public void onCompleted() {
                        showProgress(false);
                        binding.searchField.setText("");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("hoge", e.getMessage());
                        showProgress(false);
                    }

                    @Override
                    public void onNext(final MusicProviderSource model) {
                        Log.i("hoge", model.toString());
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealm(model);
                            }
                        });
                    }
                });
    }

    private void downloadVideoToDevice(final String videoId) {
        CarryMusicApiClient.getInstance().downloadVideo(videoId).subscribe(new Subscriber<Response>() {
            @Override
            public void onCompleted() {
                Log.d("MusicListFragment", "downloadVideoToDevice:Completed");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Response response) {
                FileSaveHelper.saveMusic(getContext(), videoId, response,
                        new FileSaveHelper.SaveMusicCallback() {
                    @Override
                    public void onSuccess(final File destFile) {
                        Log.d("MusicListFragment", "downloadVideoToDevice:Save success");
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                MusicProviderSource model =
                                        realm.where(MusicProviderSource.class)
                                                .equalTo("videoId", videoId).findFirst();
                                model.setVideoPath(destFile.getAbsolutePath());
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("MusicListFragment", "downloadVideoToDevice:" + message);
                    }
                });
            }
        });
    }

    private void showProgress(boolean show) {
        Log.i("hoge", "progress:" + show);
        binding.btn.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onMusicSelected(MusicProviderSource model) {
        if (model.getVideoPath() == null) {
            downloadVideoToDevice(model.getVideoId());
        } else {
            binding.musicController.musicTitle.setText(model.getTitle());
        }
    }
}
