package jp.carrymusic.api;

import jp.carrymusic.model.MusicProviderSource;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by kota on 2016/08/06.
 */
public interface CarryMusicService {

    @POST("/videos")
    Observable<MusicProviderSource> createVideo(@Query("video_id") String video_id);

    @GET("/videos/{video_id}/download")
    Observable<Response> downloadVideo(@Path("video_id") String video_id);

}
