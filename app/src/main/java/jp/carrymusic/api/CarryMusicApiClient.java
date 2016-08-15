package jp.carrymusic.api;

import com.google.gson.Gson;

import io.realm.Realm;
import io.realm.RealmResults;
import jp.carrymusic.model.MusicProviderSource;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/*
    Use CarryMusicApiClient.getInstance() method to get singleton instance.
 */
public class CarryMusicApiClient {

    private static final String ENDPOINT = "http://52.197.203.125";

    private static CarryMusicApiClient instance;
    private final CarryMusicService service;

    public static CarryMusicApiClient getInstance() {
        if (instance == null) {
            instance = new CarryMusicApiClient();
        }
        return instance;
    }

    private CarryMusicApiClient() {
        service = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setConverter(new GsonConverter(new Gson()))
                .build()
                .create(CarryMusicService.class);
    }

    public Observable<RealmResults<MusicProviderSource>> getAllSongs() {
        return useDefaultScheduler(
                Realm.getDefaultInstance().where(MusicProviderSource.class)
                        .findAll().asObservable());
    }

    public Observable<MusicProviderSource> createVideo(String video_id) {
        return useDefaultScheduler(service.createVideo(video_id));
    }

    public Observable<Response> downloadVideo(String video_id) {
        return useDefaultScheduler(service.downloadVideo(video_id));
    }

    private <T>Observable<T> useDefaultScheduler(Observable<T> observable) {
        return observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
