package jp.carrymusic.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import jp.carrymusic.api.CarryMusicApiClient;
import jp.carrymusic.model.MusicProviderSource;
import retrofit.client.Response;
import rx.SingleSubscriber;

public class DownloadHelper {

    private static final String TAG = DownloadHelper.class.getSimpleName();

    // buffer size for BufferedWriter
    private static final int BUFFER_SIZE = 1024;
    private static final String MSG_FILE_CREATE_FAILED = "Failed to create file on your device";
    private static final String MSG_IO_EXCEPTION = "Failed to save file on your device";

    public interface DownloadCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface SaveMusicCallback {
        void onSuccess(File destFile);
        void onError(String message);
    }

    /*
        This method internally saves fetched item into db.
     */
    public static void fetchNewItem(String videoId, @NonNull final DownloadCallback callback) {
        CarryMusicApiClient.getInstance().createVideo(videoId).toSingle()
                .subscribe(new SingleSubscriber<MusicProviderSource>() {
                    @Override
                    public void onSuccess(final MusicProviderSource model) {
                        Log.d(TAG, "fetchNewItem:onNext : " + model.getTitle());
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                model.setPosition(getNextNewMusicPosition());
                                realm.copyToRealm(model);
                            }
                        });
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        callback.onError(e.getMessage());
                    }
                });
    }

    /*
        find minimum music position for new items.
     */
    private static int getNextNewMusicPosition() {
        return Realm.getDefaultInstance().where(MusicProviderSource.class)
                .equalTo("trashed", false)
                .min("position").intValue() - 1;
    }

    /*
        This method internally saves fetched item into db.
    */
    public static void downloadItemIntoDevice(
            final Context context, final String videoId, @NonNull final DownloadCallback callback) {
        CarryMusicApiClient.getInstance().downloadVideo(videoId).toSingle()
                .subscribe(new SingleSubscriber<Response>() {

                    @Override
                    public void onSuccess(Response response) {
                        saveMusic(context, videoId, response,
                                new SaveMusicCallback() {
                                    @Override
                                    public void onSuccess(final File destFile) {
                                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                MusicProviderSource model =
                                                        realm.where(MusicProviderSource.class)
                                                                .equalTo("videoId", videoId).findFirst();
                                                model.setVideoPath(destFile.getAbsolutePath());
                                            }
                                        });
                                        callback.onSuccess();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e(TAG, "downloadItemIntoDevice:onError=" + message);
                                        callback.onError(message);  // error while save data
                                    }
                                });

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());  // network error
                    }
                });
    }


    private static void saveMusic(
            Context context, String videoId, Response response, SaveMusicCallback callback) {
        Realm realm = Realm.getDefaultInstance();
        MusicProviderSource model =
                realm.where(MusicProviderSource.class).equalTo("videoId", videoId).findFirst();
        File dest = getMusicDestinationFile(context, model.getTitle());
        Pair<Boolean, String> result = saveDataToDevice(dest, response);
        if (result.first) {  // if success to save
            callback.onSuccess(dest);
        } else {
            callback.onError(result.second);
        }
    }


    private static File getMusicDestinationFile(Context context, String fileName) {
        String destPath = sanitizeFileName(String.format("%s.mp3", fileName));
        return new File(context.getFilesDir(), destPath);
    }

    @VisibleForTesting
    public static String sanitizeFileName(String original) {
        return original.replaceAll("/", "-").replaceAll("\\s+", "");
    }

    /*
        @return result : pair of success flg and error message. (if success error message is null).
     */
    private static Pair<Boolean, String> saveDataToDevice(@NonNull File destFile, @NonNull Response response) {
        String errorMessage = null;
        if (!prepareNewFile(destFile)) {
            errorMessage = MSG_FILE_CREATE_FAILED;
        } else {
            try {
                if (!writeOutData(destFile, response.getBody().in())) {
                    errorMessage = MSG_IO_EXCEPTION;
                }
            } catch (IOException e) {
                errorMessage = MSG_IO_EXCEPTION;
            }
        }
        return new Pair<>(errorMessage == null, errorMessage);
    }

    private static boolean prepareNewFile(File file) {
        boolean success = true;
        if (!file.exists()) {
            try {
                success = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    private static boolean writeOutData(File destFile, InputStream source) {
        boolean success = true;
        BufferedOutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(destFile));
            byte[] line = new byte[BUFFER_SIZE];
            int size;
            while (true) {
                size = source.read(line);
                if (size <= 0) {
                    break;
                }
                fos.write(line, 0, size);
            }
            fos.close();
        } catch (IOException e) {
            success = false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    success = false;
                }
            }
        }
        return success;
    }

}
