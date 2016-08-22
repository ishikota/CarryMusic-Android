package jp.carrymusic.model;

import io.realm.Realm;
import io.realm.RealmResults;

/*
    - provides playlist (play-queue) in List<MediaItem> format for
      MediaBrowserServiceCompat.onLoadChildren
    -
 */
public class MusicProvider {

    public RealmResults<MusicProviderSource> getAllMusic() {
        return Realm.getDefaultInstance()
                .where(MusicProviderSource.class)
                .equalTo("trashed", false)
                .findAll();
    }

}
