package jp.carrymusic.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import jp.carrymusic.R;
import jp.carrymusic.helper.SimpleItemTouchHelperCallback;
import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;

public class SortMenuPresenter {

    public interface SortCallback {
        void onSortStarted();
        void onSortCanceled();
        void onSortFinished(List<MusicProviderSource> items);
    }

    private SortCallback mCallback;

    private Menu mMenu;

    private MusicProvider mMusicProvider;

    private RecyclerView.Adapter mOriginalAdapter;

    private MusicSortAdapter mSortAdapter;

    private ItemTouchHelper mItemTouchHelper;

    public void setup(SortCallback callback, Menu menu, MusicProvider musicProvider) {
        this.mCallback = callback;
        this.mMenu = menu;
        this.mMusicProvider = musicProvider;
    }

    public void onSortStarted(MenuItem menuItem, RecyclerView recyclerView) {
        setupSortingMenu(mMenu);
        mOriginalAdapter = recyclerView.getAdapter();
        setupSortableRecyclerView(recyclerView);
        mCallback.onSortStarted();
    }

    public void onSortFinished(MenuItem menuItem, RecyclerView recyclerView) {
        mCallback.onSortFinished(mSortAdapter.getData());
        restoreOriginalMenu(mMenu);
        restoreOriginalRecyclerView(recyclerView);
    }

    public void onSortCanceled(MenuItem menuItem, RecyclerView recyclerView) {
        restoreOriginalMenu(mMenu);
        restoreOriginalRecyclerView(recyclerView);
        mCallback.onSortCanceled();
    }

    private void setupSortingMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_sort).setVisible(false);
        menu.findItem(R.id.action_sort_done).setVisible(true);
        menu.findItem(R.id.action_sort_cancel).setVisible(true);
    }

    private void setupSortableRecyclerView(final RecyclerView recyclerView) {
        mSortAdapter = new MusicSortAdapter(
                recyclerView.getContext(), getCurrentPlayList(mMusicProvider));
        recyclerView.setAdapter(mSortAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mSortAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private List<MusicProviderSource> getCurrentPlayList(MusicProvider musicProvider) {
        List<MusicProviderSource> items = new ArrayList<>();
        for (MusicProviderSource source : musicProvider.getAllMusic()) {
            items.add(source);
        }
        return items;
    }

    private void restoreOriginalMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.action_sort).setVisible(true);
        menu.findItem(R.id.action_sort_done).setVisible(false);
        menu.findItem(R.id.action_sort_cancel).setVisible(false);
    }

    private void restoreOriginalRecyclerView(RecyclerView recyclerView) {
        mItemTouchHelper.attachToRecyclerView(null);
        recyclerView.setAdapter(mOriginalAdapter);
        mOriginalAdapter = null;
        mSortAdapter = null;
        mItemTouchHelper = null;
    }

}
