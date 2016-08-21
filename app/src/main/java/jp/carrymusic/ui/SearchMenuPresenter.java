package jp.carrymusic.ui;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jp.carrymusic.R;
import jp.carrymusic.utils.DownloadHelper;

public class SearchMenuPresenter {

    private static final String TAG = SearchMenuPresenter.class.getSimpleName();

    // Hold query while network access to restore when network access failed
    private String mQueryMemo = "";

    private DownloadHelper.DownloadCallback mCallback;

    public void setup(Menu menu) {
        MenuItem progressItem = menu.findItem(R.id.action_progress);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        setupProgress(progressItem);
        setupSearchAction(searchItem, progressItem);
    }

    public void setCallback(DownloadHelper.DownloadCallback callback) {
        this.mCallback = callback;
    }

    private void setupProgress(MenuItem progressItem) {
        MenuItemCompat.setActionView(progressItem, R.layout.menu_progress);
    }

    private void setupSearchAction(final MenuItem searchItem, final MenuItem progressItem) {
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        String searchHint =
                searchView.getContext().getResources().getString(R.string.search_query_hint);
        searchView.setQueryHint(searchHint);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit with query : " + query);
                saveQuery(query);
                showProgressAction(searchItem, progressItem);
                DownloadHelper.fetchNewItem(query, new DownloadHelper.DownloadCallback() {
                    @Override
                    public void onSuccess() {
                        hideProgressAction(searchItem, progressItem);
                        clearQuery();
                        if (mCallback != null) {
                            mCallback.onSuccess();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        hideProgressAction(searchItem, progressItem);
                        searchItem.expandActionView();
                        restoreSearchQuery(searchView);
                        if (mCallback != null) {
                            mCallback.onError(message);
                        }
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

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                restoreSearchQuery(searchView);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                String query = ((SearchView) item.getActionView()).getQuery().toString();
                saveQuery(query);
                return true;
            }
        });
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

    private void saveQuery(String query) {
        if (!query.isEmpty()) {
            mQueryMemo = query;
        }
    }

    private void clearQuery() {
        mQueryMemo = "";
    }

    private void restoreSearchQuery(final SearchView searchView) {
        searchView.post(new Runnable() {
            @Override
            public void run() {
                if (!mQueryMemo.isEmpty()) {  // Dirty solution to handle empty string overwrite
                    searchView.setQuery(mQueryMemo, false);
                    mQueryMemo = "";
                }
            }
        });
    }

}
