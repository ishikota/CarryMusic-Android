package jp.carrymusic.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import jp.carrymusic.R;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    SearchMenuPresenter searchMenuPresenter = new SearchMenuPresenter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup App bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        searchMenuPresenter.setup(menu);

        return super.onCreateOptionsMenu(menu);
    }


}
