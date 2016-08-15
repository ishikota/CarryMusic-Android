package jp.carrymusic.model;

import android.os.Build;

import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

import jp.carrymusic.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class MusicProviderSourceTest {

    private static final String TMP_FILE_PATH = "tmp_music_provider_source.txt";

    @Test
    public void parseModelFromJson() {
        MusicProviderSource model = genModel("music_provider_source.json");
        assertEquals("L0iiOMvNv1Y", model.getVideoId());
        assertEquals("Perfume 「Lovefool」 - ペプシネックス CM", model.getTitle());
        assertEquals(15, model.getDuration());
        assertEquals("2010-06-18", model.getUploadDate());
        assertEquals("https://i.ytimg.com/vi/L0iiOMvNv1Y/maxresdefault.jpg", model.getThumbnailUrl());
    }

    @Test
    public void getDataSizeInMegaByte() {
        createTmpFile();
        MusicProviderSource model = genModel("music_provider_source.json");
        model.setVideoPath(TMP_FILE_PATH);
        assertEquals(new File(TMP_FILE_PATH).length(), model.getDataSizeInMB(), 0.1);
        deleteTmpFile();
    }


    private MusicProviderSource genModel(String source_path) {
        String json = Util.readAssetFile(source_path);
        return new Gson().fromJson(json, MusicProviderSource.class);
    }

    private void createTmpFile() {
        File file = new File(TMP_FILE_PATH);
        try {
            if(!file.exists() && !file.createNewFile()) {
                fail("Failed to create tmp file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTmpFile() {
        File file = new File(TMP_FILE_PATH);
        if(!file.delete()) {
            fail("Failed to delete tmp file");
        }
    }
}
