package jp.carrymusic.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import jp.carrymusic.model.MusicProviderSource;
import retrofit.client.Response;

public class FileSaveHelper {

    // buffer size for BufferedWriter
    private static final int BUFFER_SIZE = 1024;
    private static final String MSG_FILE_CREATE_FAILED = "Failed to create file on your device";
    private static final String MSG_IO_EXCEPTION = "Failed to save file on your device";

    public interface SaveMusicCallback {
        void onSuccess(File destFile);
        void onError(String message);
    }

    public static void saveMusic(
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
        return new File(context.getFilesDir(), String.format("%s.mp3", fileName));
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
