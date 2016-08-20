package jp.carrymusic.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
        final MusicProviderSource data = getData().get(i);
        viewHolder.textView.setText(data.getTitle());
        if (data.getVideoPath()!=null) {
            viewHolder.fileSizeView.setText(String.format("%.1f MB", data.getDataSizeInMB()));
        }
        viewHolder.durationView.setText(String.format("%d:%d", data.getDuration()/60, data.getDuration()%60));
        viewHolder.data = data;
        viewHolder.statusLabel.setText(data.getVideoPath()!=null ? "CACHED" : "DOWNLOAD");
        Picasso.with(viewHolder.thumbnail.getContext())
                .load(data.getThumbnailUrl()).into(viewHolder.thumbnail);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView, fileSizeView, durationView, statusLabel;
        public ImageView thumbnail;
        public MusicProviderSource data;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            fileSizeView = (TextView) itemView.findViewById(R.id.file_size);
            durationView = (TextView) itemView.findViewById(R.id.duration);
            statusLabel = (TextView) itemView.findViewById(R.id.status_label);
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
