package jp.carrymusic.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import jp.carrymusic.R;
import jp.carrymusic.helper.ItemTouchHelperAdapter;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.utils.UIHelper;

public class MusicSortAdapter extends RecyclerView.Adapter<MusicSortAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static final String TAG = MusicSortAdapter.class.getSimpleName();

    private final int PRIMARY_TEXT_COLOR;

    private final int DISABLED_TEXT_COLOR;

    private static final float THUMBNAIL_ENABLED_ALPHA = 1.0f;

    private static final float THUMBNAIL_DISABLED_ALPHA = 0.5f;

    private List<MusicProviderSource> mData;

    public MusicSortAdapter(Context context, List<MusicProviderSource> data) {
        PRIMARY_TEXT_COLOR = context.getResources().getColor(R.color.primary_text);
        DISABLED_TEXT_COLOR = context.getResources().getColor(R.color.disabled_text);
        mData = data;
    }

    @Override
    public MusicSortAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View noteView = inflater.inflate(R.layout.list_item_music, parent, false);
        return new ViewHolder(noteView);
    }

    @Override
    public void onBindViewHolder(MusicSortAdapter.ViewHolder viewHolder, int i) {

        final MusicProviderSource data = mData.get(i);
        viewHolder.data = data;

        viewHolder.musicTitle.setText(data.getTitle());
        viewHolder.duration.setText(UIHelper.genDurationString(data.getDuration()));
        viewHolder.fileSize.setText(UIHelper.genFileSizeString(data.getDataSizeInMB()));
        Picasso.with(viewHolder.thumbnail.getContext())
                .load(data.getThumbnailUrl()).into(viewHolder.thumbnail);

        boolean is_music_cached = data.getVideoPath() != null;
        viewHolder.icon_cache_warning
                .setVisibility(is_music_cached ? View.GONE : View.VISIBLE);
        viewHolder.musicTitle
                .setTextColor(is_music_cached ? PRIMARY_TEXT_COLOR : DISABLED_TEXT_COLOR);
        viewHolder.thumbnail
                .setAlpha(is_music_cached ? THUMBNAIL_ENABLED_ALPHA : THUMBNAIL_DISABLED_ALPHA);

        viewHolder.icon_action_more.setVisibility(View.GONE);
        viewHolder.progress.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Log.d(TAG, String.format("onItemMove: fromPosition=%d, toPosition=%d", fromPosition, toPosition));
        Collections.swap(mData, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public List<MusicProviderSource> getData() {
        return mData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView musicTitle, fileSize, duration;
        public ImageView thumbnail, icon_cache_warning, icon_action_more;
        public MusicProviderSource data;
        public ProgressBar progress;
        public ViewHolder(View itemView) {
            super(itemView);
            musicTitle = (TextView) itemView.findViewById(R.id.music_title);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            duration = (TextView) itemView.findViewById(R.id.duration);
            icon_cache_warning = (ImageView) itemView.findViewById(R.id.icon_cache_warning);
            icon_action_more = (ImageView) itemView.findViewById(R.id.action_more);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            progress = (ProgressBar) itemView.findViewById(R.id.progress);
        }
    }

}
