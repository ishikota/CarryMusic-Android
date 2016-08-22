package jp.carrymusic.utils;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import jp.carrymusic.R;

public class UndoSnackbar {

    public interface Callback {
        void onUndo();
        void onDismissWithoutRedo(Snackbar snackbar);
    }

    private static final int UNDO_LABEL_RESOURCE_ID = R.string.undo;

    private UndoSnackbar() {}

    public static Snackbar make(
            @NonNull View view, @StringRes int resId, int duration, Callback callback) {
        return setupRedoFeature(Snackbar.make(view, resId, duration), callback);
    }

    public static Snackbar makeLong(
            @NonNull View view, @StringRes int resId, Callback callback) {
        return make(view, resId, Snackbar.LENGTH_LONG, callback);
    }

    private static Snackbar setupRedoFeature(Snackbar snackbar, final Callback callback) {
        return snackbar
                .setAction(UNDO_LABEL_RESOURCE_ID, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callback.onUndo();
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        if (Snackbar.Callback.DISMISS_EVENT_ACTION != event) {
                            callback.onDismissWithoutRedo(snackbar);
                        }
                    }
                });
    }

}
