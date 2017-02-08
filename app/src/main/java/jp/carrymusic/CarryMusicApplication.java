package jp.carrymusic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import jp.carrymusic.api.CarryMusicApiClient;
import jp.carrymusic.ui.EndpointMenuPresenter;

public class CarryMusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupRealm();
        CarryMusicApiClient.setEndpoint(getEndpoint());
    }

    private void setupRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext())
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
    }

    private String getEndpoint() {
        SharedPreferences prefs =
                getSharedPreferences(EndpointMenuPresenter.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(EndpointMenuPresenter.PREF_ENDPOINT_KEY, "");
    }

}
