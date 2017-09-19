package com.qhiehome.ihome.application;

import android.app.Application;

import com.qhiehome.ihome.persistence.DaoMaster;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.CrashHandler;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

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

        // init greenDao
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "search-db-encrypted" : "search-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        // init CrashHandler
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, Constant.APP_ID, true);
        // 将该app注册到微信
        msgApi.registerApp(Constant.APP_ID);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public static IhomeApplication getInstance() {
        return ihomeApplication;
    }

}
