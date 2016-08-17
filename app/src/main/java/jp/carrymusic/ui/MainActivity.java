package jp.carrymusic.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import jp.carrymusic.R;

public class MainActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the retained fragment for activity restarts case
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(MusicListFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = new MusicListFragment();
            String tag = MusicListFragment.class.getSimpleName();
            fm.beginTransaction().add(R.id.container, fragment, tag).commit();
        }
    }

    @Override
    protected void onMediaControllerConnected() {
        getBrowseFragment().onConnected();
    }

    private MusicListFragment getBrowseFragment() {
        return (MusicListFragment) getSupportFragmentManager()
                .findFragmentByTag(MusicListFragment.class.getSimpleName());
    }

}
