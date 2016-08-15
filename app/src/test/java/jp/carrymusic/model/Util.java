package jp.carrymusic.model;

import org.robolectric.RuntimeEnvironment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class Util {

    public static String readAssetFile(String filename) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                        RuntimeEnvironment.application.getResources().getAssets().open(filename)));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
