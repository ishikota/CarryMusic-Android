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

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import jp.carrymusic.R;
import jp.carrymusic.model.MusicProviderSource;
import jp.carrymusic.utils.UIHelper;

public class MusicListAdapter extends RealmRecyclerViewAdapter<MusicProviderSource, MusicListAdapter.ViewHolder> {

    private static final String TAG = MusicListAdapter.class.getSimpleName();

    private final MusicListClickListener mClickCallback;

    private final int PRIMARY_TEXT_COLOR;

    private final int DISABLED_TEXT_COLOR;

    private static final float THUMBNAIL_ENABLED_ALPHA = 1.0f;

    private static final float THUMBNAIL_DISABLED_ALPHA = 0.5f;

    public MusicListAdapter(Context context, OrderedRealmCollection<MusicProviderSource> data,
                            MusicListClickListener callback) {
        super(context, data, true);
        mClickCallback = callback;
        PRIMARY_TEXT_COLOR = context.getResources().getColor(R.color.primary_text);
        DISABLED_TEXT_COLOR = context.getResources().getColor(R.color.disabled_text);
    }

    @Override
    public MusicListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View noteView = inflater.inflate(R.layout.list_item_music, parent, false);
        return new ViewHolder(noteView);
    }

    @Override
    public void onBindViewHolder(MusicListAdapter.ViewHolder viewHolder, int i) {

        // Suppress "may NullPointerException" warning
        if (getData() == null) {
            Log.e(TAG, String.format("onBindViewHolder: getData on position %d returns null", i));
            return;
        }

        final MusicProviderSource data = getData().get(i);
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

        boolean is_downloading = data.isDownloading();
        viewHolder.icon_action_more.setVisibility(is_downloading ? View.GONE : View.VISIBLE);
        viewHolder.progress.setVisibility(is_downloading ? View.VISIBLE : View.GONE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            itemView.setOnClickListener(this);
            icon_action_more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.action_more) {
                mClickCallback.onMoreActionClicked(data, v);
            } else {
                mClickCallback.onMusicSelected(data);
            }
        }
    }

    public interface MusicListClickListener {
        void onMusicSelected(MusicProviderSource model);
        void onMoreActionClicked(MusicProviderSource model, View v);
    }

}
