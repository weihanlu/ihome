package com.qhiehome.ihome.lock.bluetooth;

import android.app.IntentService;
import android.content.Intent;

import com.qhiehome.ihome.util.LogUtil;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class BluetoothManagerService extends IntentService {

    private static final String TAG = "BluetoothManagerService";

    public static final String ACTION_BLUETOOTH_CONNECT = "com.qhiehome.ihome.lock.action.BLUETOOTH_CONNECT";
    public static final String ACTION_DISCONNECT = "com.qhiehome.ihome.lock.action.DISCONNECT";
    public static final String ACTION_UP_LOCK = "com.qhiehome.ihome.lock.action.UP_LOCK";
    public static final String ACTION_DOWN_LOCK = "com.qhiehome.ihome.lock.action.DOWN_LOCK";

    public static final String ACTION_LOCK_STATE = "com.qhiehome.ihom.lock.action.LOCK_STATE";
    public static final String EXTRA_LOCK_NAME = "com.qhiehome.ihome.lock.extra.LOCK_NAME";

    private BluetoothClient mBluetoothClient;

    public BluetoothManagerService() {
        super("BluetoothManagerService");
        mBluetoothClient = BluetoothClient.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            int lockState;
            switch (action) {
                case ACTION_BLUETOOTH_CONNECT:
                    String lockName = intent.getStringExtra(EXTRA_LOCK_NAME);
                    mBluetoothClient.setLockName(lockName);
                    mBluetoothClient.connect();
                    break;
                case ACTION_DISCONNECT:
                    mBluetoothClient.disconnect();
                    break;
                case ACTION_UP_LOCK:
                    lockState = intent.getIntExtra(ACTION_LOCK_STATE, -1);
                    LogUtil.d(TAG, "lockState is " + lockState);
                    mBluetoothClient.setLockState(lockState);
                    mBluetoothClient.raiseLock();
                    break;
                case ACTION_DOWN_LOCK:
                    lockState = intent.getIntExtra(ACTION_LOCK_STATE, -1);
                    mBluetoothClient.setLockState(lockState);
                    mBluetoothClient.downLock();
                    break;
                default:
                    break;
            }
        }
    }
}