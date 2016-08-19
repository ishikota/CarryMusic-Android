package jp.carrymusic.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jp.carrymusic.R;
import jp.carrymusic.utils.DownloadHelper;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

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

        // Setup progress item
        final MenuItem progressItem = menu.findItem(R.id.action_progress);
        MenuItemCompat.setActionView(progressItem, R.layout.menu_progress);

        // Setup search item
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit with query : " + query);
                showProgressAction(searchItem, progressItem);
                DownloadHelper.fetchNewItem(query, new DownloadHelper.DownloadCallback() {
                    @Override
                    public void onSuccess() {
                        hideProgressAction(searchItem, progressItem);
                    }

                    @Override
                    public void onError(String message) {
                        // TODO notify error in some way
                        hideProgressAction(searchItem, progressItem);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange with newText : " + newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void showProgressAction(MenuItem search, MenuItem progress) {
        search.collapseActionView();
        search.setVisible(false);
        progress.setVisible(true);
    }

    private void hideProgressAction(MenuItem search, MenuItem progress) {
        search.setVisible(true);
        progress.setVisible(false);
    }


}
