package jp.carrymusic.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import jp.carrymusic.R;
import jp.carrymusic.model.MusicProviderSource;

public class MusicListAdapter extends RealmRecyclerViewAdapter<MusicProviderSource, MusicListAdapter.ViewHolder> {

    private static final String TAG = MusicListAdapter.class.getSimpleName();

    private final MusicListClickListener mClickCallback;

    public MusicListAdapter(Context context, OrderedRealmCollection<MusicProviderSource> data,
                            MusicListClickListener callback) {
        super(context, data, true);
        mClickCallback = callback;
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
        viewHolder.duration.setText(String.format("%d:%d", data.getDuration()/60, data.getDuration()%60));
        Picasso.with(viewHolder.thumbnail.getContext())
                .load(data.getThumbnailUrl()).into(viewHolder.thumbnail);

        boolean is_music_cached = data.getVideoPath() != null;
        if (is_music_cached) {
            viewHolder.icon_cache_warning.setVisibility(View.GONE);
            viewHolder.fileSize.setText(String.format("%.1f MB", data.getDataSizeInMB()));
        } else {
            viewHolder.icon_cache_warning.setVisibility(View.VISIBLE);
            viewHolder.fileSize.setVisibility(View.GONE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView musicTitle, fileSize, duration;
        public ImageView thumbnail, icon_cache_warning;
        public MusicProviderSource data;
        public ViewHolder(View itemView) {
            super(itemView);
            musicTitle = (TextView) itemView.findViewById(R.id.music_title);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            duration = (TextView) itemView.findViewById(R.id.duration);
            icon_cache_warning = (ImageView) itemView.findViewById(R.id.icon_cache_warning);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickCallback.onMusicSelected(data);
        }
    }

    public interface MusicListClickListener {
        void onMusicSelected(MusicProviderSource model);
    }

}
