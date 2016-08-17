/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.carrymusic.playback;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.carrymusic.model.MusicProvider;
import jp.carrymusic.model.MusicProviderSource;

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager {

    private MusicProvider mMusicProvider;
    private MetadataUpdateListener mListener;

    // "Now playing" queue:
    private List<MusicProviderSource> mPlayingQueue;
    private int mCurrentIndex;

    public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull MetadataUpdateListener listener) {
        this.mMusicProvider = musicProvider;
        this.mListener = listener;

        mPlayingQueue = Collections.synchronizedList(new ArrayList<MusicProviderSource>());
        mCurrentIndex = 0;
    }

    private void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < mPlayingQueue.size()) {
            mCurrentIndex = index;
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }

    public boolean setCurrentQueueItem(String mediaId) {
        int index = -1;
        for (MusicProviderSource model: mMusicProvider.getAllMusic()) {
            if (model.getVideoId().equals(mediaId)) {
                index = mMusicProvider.getAllMusic().indexOf(model);
            }
        }
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            index %= mPlayingQueue.size();
        }
        mCurrentIndex = index;
        return true;
    }

    public int getCurrentQueueSize() {
        if (mPlayingQueue == null) {
            return 0;
        }
        return mPlayingQueue.size();
    }

    public MusicProviderSource getCurrentMusic() {
        if (mPlayingQueue.size() == 0) return null;
        return mPlayingQueue.get(mCurrentIndex);
    }

    protected void setCurrentQueue(String title, List<MusicProviderSource> newQueue) {
        mPlayingQueue = newQueue;
        mCurrentIndex = 0;
        mListener.onQueueUpdated(title, newQueue);
    }

    public void updateMetadata() {
        MusicProviderSource currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        MediaMetadataCompat metadata = currentMusic.toMetaData();
        mListener.onMetadataChanged(metadata);
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);
        void onMetadataRetrieveError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, List<MusicProviderSource> newQueue);
    }
}
