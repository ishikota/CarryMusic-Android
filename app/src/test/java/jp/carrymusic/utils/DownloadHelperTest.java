package jp.carrymusic.utils;

import android.app.Application;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP, application = Application.class)
public class DownloadHelperTest {

    @Test
    public void testSanitizeFileName() {
        String original_filename = "スピッツ / 正夢.mp3";
        String sanitized = DownloadHelper.sanitizeFileName(original_filename);
        File file = new File(sanitized);
        try {
            boolean success = file.createNewFile();
            assertTrue(success);
            if (!file.delete()) {
                fail(format("Delete file failed. => %s", file.getAbsolutePath()));
            }
        } catch (IOException e) {
            fail(format("IOException (message = %s)", e.getMessage()));
        }
    }

}
