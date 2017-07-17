package com.qhiehome.ihome.ble.profile;

import android.content.Context;

import com.qhiehome.ihome.ble.primaryservice.PrimaryServiceThread;
import com.qhiehome.ihome.util.LogUtil;

public class ThreadManager {

    private static final String TAG = ThreadManager.class.getSimpleName();

    private PrimaryServiceThread mPrimaryServiceThread;
    private static ThreadManager instance;

    private ThreadManager(Context context) {
        LogUtil.d(TAG, "ThreadManager, enter SPPLEClient Thread init...");
        mPrimaryServiceThread = new PrimaryServiceThread(context);
    }

    public static ThreadManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThreadManager(context);
        }
        return instance;
    }

    private void tearDownThread(BLEClientThread bleClientThread) {
        if (bleClientThread != null) {
            bleClientThread.tearDown();
        }
    }

    public PrimaryServiceThread getPrimaryServiceThread() {
        return mPrimaryServiceThread;
    }

    public void tearDown() {
        instance = null;
        tearDownThread(mPrimaryServiceThread);
        mPrimaryServiceThread = null;
    }

}
