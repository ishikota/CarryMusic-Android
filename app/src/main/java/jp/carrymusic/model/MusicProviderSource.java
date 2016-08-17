package jp.carrymusic.model;

import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;

import com.google.gson.annotations.SerializedName;

import java.io.File;

import io.realm.RealmObject;


@SuppressWarnings("unused")
public class MusicProviderSource extends RealmObject {

    @SerializedName("video_id")
    private String videoId;

    private String title;

    private int duration;

    @SerializedName("upload_date")
    private String uploadDate;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    private String videoPath;

    public float getDataSizeInMB() {
        if (this.getVideoPath() == null) {
            return -1;
        } else {
            return ((float) new File(this.getVideoPath()).length() / 1000000);
        }
    }

    public MediaMetadataCompat toMetaData() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.videoId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this.duration)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, this.uploadDate)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, this.getThumbnailUrl())
                .build();
    }


    // Getter and Setters

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Nullable
    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
