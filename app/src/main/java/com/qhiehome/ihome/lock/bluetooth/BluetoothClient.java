package com.qhiehome.ihome.lock.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.qhiehome.ihome.util.LogUtil;

public class BluetoothClient {

    private static final String TAG = BluetoothClient.class.getSimpleName();

    private static volatile BluetoothClient bluetoothClient;

    private String lockMac;

    private String lockPwd;

    private Context mContext;

    private BluetoothClient(Context context) {
        this.mContext = context;

    }

    public static BluetoothClient getInstance(Context context) {
        if (bluetoothClient == null) {
            synchronized (BluetoothClient.class) {
                if (bluetoothClient == null) {
                    bluetoothClient = new BluetoothClient(context);
                }
            }
        }
        return bluetoothClient;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }

    public void setLockPwd(String lockPwd) {
        this.lockPwd = lockPwd;
    }

    public void connect() {
        LogUtil.d(TAG, TAG + " connect");
    }

    public void disconnect() {
        LogUtil.d(TAG, TAG + " disconnect");
    }

    public void raiseLock() {
        LogUtil.d(TAG, TAG + " raise lock");
    }

    public void downLock() {
        LogUtil.d(TAG, TAG + " down lock");
    }

}
