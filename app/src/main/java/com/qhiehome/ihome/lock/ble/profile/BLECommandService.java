package com.qhiehome.ihome.lock.ble.profile;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.qhiehome.ihome.util.LogUtil;

public class BLECommandService extends Service {
	private final String TAG = "BLECommandIntentService";

	private ThreadManager mThreadManager;

	@Override
	public void onCreate() {
		LogUtil.d(TAG, "onCreate...");
		mThreadManager = ThreadManager.getInstance(this);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		LogUtil.d(TAG, "onDestroy...");
		onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		if (intent == null) {
			LogUtil.e(TAG, "Intent is null! onStartCommand");
			return START_STICKY;
		}

		String str = intent.getAction();
		LogUtil.d(TAG, "onStartCommand: " + intent );
		if (TextUtils.isEmpty(str)) {
			LogUtil.e(TAG, "Received empty intent action, nothing todo");
			return START_STICKY;
		}
		if (BLECommandIntent.PrimaryServiceActionList.contains(str)) {
			mThreadManager.getPrimaryServicelientThread().newCommand(intent);
		}
		return START_STICKY;
	}

}
