package jp.carrymusic;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class CarryMusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupRealm();
    }

    private void setupRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext())
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
    }

}
