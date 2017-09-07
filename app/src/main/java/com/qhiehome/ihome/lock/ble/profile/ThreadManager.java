package com.qhiehome.ihome.lock.ble.profile;

import android.content.Context;

import com.qhiehome.ihome.lock.ble.primary_service.PrimaryServiceThread;
import com.qhiehome.ihome.util.LogUtil;

public class ThreadManager {

	private static ThreadManager INSTANCE;
	private PrimaryServiceThread mPrimaryServiceClientThread;
	private final String TAG = "ThreadManager";

	private ThreadManager(Context context) {
		LogUtil.d(TAG, "ThreadManager,ctor now,enter SPPLEClientThread init...");
		mPrimaryServiceClientThread = new PrimaryServiceThread(context);
	}

	public static ThreadManager getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new ThreadManager(context);
		}
		return INSTANCE;
	}

	private void tearDownThread(BLEClientThread paramBLEClientThread) {
		if (paramBLEClientThread != null) {
			paramBLEClientThread.tearDown();
		}
	}

	public PrimaryServiceThread getPrimaryServicelientThread() {
		return mPrimaryServiceClientThread;
	}

	public void tearDown() {
		INSTANCE = null;
		tearDownThread(mPrimaryServiceClientThread);
		mPrimaryServiceClientThread = null;
	}
}
