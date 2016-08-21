package jp.carrymusic.utils;

public class UIHelper {

    public static String genFileSizeString(float sizeInMB) {
        if (sizeInMB <= 0) {
            return "-- MB";
        } else {
            return String.format("%.1f MB", sizeInMB);
        }
    }

    /*
        ex. in 400 => out "06:40"
     */
    public static String genDurationString(int durationInSecond) {
        return String.format("%d:%02d", fetchMin(durationInSecond), fetchSec(durationInSecond));
    }

    private static int fetchMin(int position) {
        return position / 60;
    }

    private static int fetchSec(int position) {
        return position % 60;
    }
}
