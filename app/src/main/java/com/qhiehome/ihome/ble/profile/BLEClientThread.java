package com.qhiehome.ihome.ble.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;

import com.qhiehome.ihome.ble.request.RequestManager;
import com.qhiehome.ihome.ble.request.NotificationCallback;
import com.qhiehome.ihome.ble.request.Request;
import com.qhiehome.ihome.ble.request.RequestCallback;


public abstract class BLEClientThread implements RequestCallback,
		NotificationCallback {
	public static final long INTENT_WAKELOCK_TIMEOUT = 2000L;
	public Context mContext;
	private Handler mHandler;
	private HandlerThread mHandlerThread;
	private RequestManager mRequestManager;
	private PowerManager.WakeLock mWakeLock;

	public BLEClientThread(String threadName, Context context) {
		mContext = context.getApplicationContext() == null ? context : context.getApplicationContext();
		mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE))
		        .newWakeLock(1, threadName);
		mWakeLock.setReferenceCounted(true);
		mHandlerThread = new HandlerThread(threadName);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
		mRequestManager = RequestManager.getInstance();
	}

	public final void newCommand(Intent paramIntent) {
		acquireWakelock();
		mHandler.post(new ReceiveNewCommandRunnable(paramIntent));
	}

	public final void onNotification(BluetoothGattCharacteristic characteristic) {
		acquireWakelock();
		mHandler.post(new ReceiveNotificationRunnable(characteristic));
	}

	public final void onRequestComplete(
	        BluetoothGattCharacteristic characteristic, boolean paramBoolean,
	        Request.REQUEST_TYPE paramREQUEST_TYPE) {
		acquireWakelock();
		
//		if(paramREQUEST_TYPE == REQUEST_TYPE.WRITE)
//			LogUtils.i("BLEClientThread", "onRequestComplete 0...: " +  APPUtils.byteArrayToString( characteristic.getValue()));

		mHandler.post(new RequestProcessCompleteRunnable(characteristic,
		        paramBoolean, paramREQUEST_TYPE, characteristic.getValue()));
	}

	private class ReceiveNewCommandRunnable implements Runnable {
		private Intent intent;

		ReceiveNewCommandRunnable(Intent paramIntent) {
			intent = paramIntent;
		}

		public void run() {
			onCommand(intent);
			releaseWakeLock();
		}
	}

	private class RequestProcessCompleteRunnable implements Runnable {
		BluetoothGattCharacteristic characteristic;
		boolean isSuccess;
		Request.REQUEST_TYPE type;
        byte[] mData;
		RequestProcessCompleteRunnable(
		        BluetoothGattCharacteristic paramBluetoothGattCharacteristic,
		        boolean paramBoolean, Request.REQUEST_TYPE paramREQUEST_TYPE, byte[] data) {

			characteristic = paramBluetoothGattCharacteristic;
			mData = data;
//			if(paramREQUEST_TYPE == REQUEST_TYPE.WRITE)
//				LogUtils.i("BLEClientThread", "onRequestComplete 1...: " +  APPUtils.byteArrayToString(mData));

			isSuccess = paramBoolean;
			type = paramREQUEST_TYPE;
		}

		public void run() {
//			if(type == REQUEST_TYPE.WRITE)
//				LogUtils.i("BLEClientThread", "onRequestComplete 2...: " +  APPUtils.byteArrayToString(mData));

			onBLERequestComplete(characteristic, isSuccess, type,mData);
			releaseWakeLock();
		}
	}

	private class ReceiveNotificationRunnable implements Runnable {
		BluetoothGattCharacteristic characteristic;

		ReceiveNotificationRunnable(BluetoothGattCharacteristic characteristic) {
			this.characteristic = characteristic;
		}

		public void run() {
			onBLENotification(characteristic);
			releaseWakeLock();
		}
	}

	protected abstract void onBLENotification(
	        BluetoothGattCharacteristic characteristic);

	protected abstract void onBLERequestComplete(
	        BluetoothGattCharacteristic characteristic, boolean paramBoolean,
	        Request.REQUEST_TYPE paramREQUEST_TYPE, byte[] data);

	protected abstract void onCommand(Intent paramIntent);

	protected void acquireWakelock() {
		if (mWakeLock != null) {
			mWakeLock.acquire();
		}
	}

	protected void acquireWakelock(long paramLong) {
		if (mWakeLock != null) {
			mWakeLock.acquire(paramLong);
		}
	}

	protected final Context getContext() {
		return mContext;
	}

	protected final Handler getHandler() {
		return mHandler;
	}

	protected final RequestManager getRequestManager() {
		return mRequestManager;
	}

	protected final boolean isReady() {
		// return mAppCore.isConnected();
		return false;
	}

	protected void releaseWakeLock() {
		if ((mWakeLock != null) && (mWakeLock.isHeld())) {
			mWakeLock.release();
		}
	}

	public void tearDown() {
		if (mHandlerThread != null) {
			mHandler = null;
			mHandlerThread.quitSafely();
			mHandlerThread = null;
		}
		if ((mWakeLock != null) && (mWakeLock.isHeld())) {
			mWakeLock.release();
		}
	}

}
