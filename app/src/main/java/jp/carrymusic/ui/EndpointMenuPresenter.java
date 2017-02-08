package jp.carrymusic.ui;

import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jp.carrymusic.R;
import jp.carrymusic.api.CarryMusicApiClient;

public class EndpointMenuPresenter {

    private static final String TAG = EndpointMenuPresenter.class.getSimpleName();

    public static final String PREF_NAME = "EndpointMenuPresenter";

    public static final String PREF_ENDPOINT_KEY = "pref_key_endpoint";

    private EndpointUpdatedListener mCallback;

    interface EndpointUpdatedListener {
        SharedPreferences getPreference();
        void updated();
    }

    public void setup(EndpointUpdatedListener callback, Menu menu) {
        this.mCallback = callback;
        MenuItem searchItem = menu.findItem(R.id.action_endpoint);
        setupSearchAction(searchItem);
    }

    private void setupSearchAction(final MenuItem searchItem) {
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        String searchHint =
                searchView.getContext().getResources().getString(R.string.endpoint_query_hint);
        searchView.setQueryHint(searchHint);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit with query : " + query);
                boolean saved = saveEndpoint(query);
                if (saved) {
                    mCallback.updated();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange with newText : " + newText);
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                SharedPreferences prefs = mCallback.getPreference();
                final String currentEndpoint = prefs.getString(PREF_ENDPOINT_KEY, "");
                searchView.post(new Runnable() {
                    @Override
                    public void run() {
                        searchView.setQuery(currentEndpoint, false);
                    }
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
    }

    private boolean saveEndpoint(String endpoint) {
        boolean need_save = !endpoint.isEmpty();
        if (need_save) {
            SharedPreferences prefs = mCallback.getPreference();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_ENDPOINT_KEY, endpoint);
            editor.apply();
            CarryMusicApiClient.setEndpoint(endpoint);
        }
        return need_save;
    }

}
