package com.qhiehome.ihome.ble.profile;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.qhiehome.ihome.util.LogUtil;

public class BLECommandService extends Service {

    private static final String TAG = BLECommandService.class.getSimpleName();

    private ThreadManager mThreadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "BLECommandService onCreate");
        mThreadManager = ThreadManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "BLECommandService destroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            LogUtil.e(TAG, "Intent is null! onStartCommand");
            return START_STICKY;
        }
        String action = intent.getAction();
        LogUtil.d(TAG, "onStartCommand: " + intent);
        if (TextUtils.isEmpty(action)) {
            LogUtil.e(TAG, "Received empty intent action, nothing to do");
            return START_STICKY;
        }
        if (BLECommandIntent.PrimaryServiceActionList.contains(action)) {
            mThreadManager.getPrimaryServiceThread().newCommand(intent);
        }
        return START_STICKY;
    }
}
