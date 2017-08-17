package com.qhiehome.ihome.application;

import android.app.Application;

import com.qhiehome.ihome.persistence.DaoMaster;
import com.qhiehome.ihome.persistence.DaoSession;

import org.greenrobot.greendao.database.Database;


/**
 * This is the global entrance of the app
 *
 * The Ihome application will use below structures:
 *     1. Http Request structure: Retrofit
 *     2. View binding: butterknife
 *     3. ORM structure interact with database: Active Android
 */

public class IhomeApplication extends Application {

    private static final String TAG = "IhomeApplication";

    /* A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher */
    public static final boolean ENCRYPTED = false;

    private DaoSession daoSession;

    private static IhomeApplication ihomeApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        ihomeApplication = this;

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "search-db-encrypted" : "search-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static IhomeApplication getInstance() {
        return ihomeApplication;
    }

}
