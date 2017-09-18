package com.qhiehome.ihome.lock.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.lock.AppClient;
import com.qhiehome.ihome.lock.ConnectLockService;
import com.qhiehome.ihome.lock.ble.CommunicationManager;
import com.qhiehome.ihome.lock.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.lock.ble.profile.HostAppService;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

public class BluetoothClient extends AppClient {

    private static final String TAG = BluetoothClient.class.getSimpleName();

    private static volatile BluetoothClient bluetoothClient;

    private BluetoothAdapter mBluetoothAdapter;

    private boolean isFindLock;

    private String mLockName;

    private String mLockMac;

    private Context mContext;

    private int mLockState;

    public enum LOCK_STATE {
        ERROR,
        DOWN,
        UPPING,
        UP,
        DOWNING
    }

    private BluetoothClient(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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

    public void setLockName(String lockName) {
        mLockName = lockName;
    }

    public void setLockState(int lockState) {
        mLockState = lockState;
    }

    private void scanLeDevice(boolean startScan) {
        if (mBluetoothAdapter != null) {
            if (startScan) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    @Override
    public void connect() {
        LogUtil.d(TAG, TAG + " connect");
        scanLeDevice(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFindLock) {
                    scanLeDevice(false);
                    Intent intent = new Intent(ConnectLockService.BROADCAST_CONNECT);
                    intent.putExtra("info", "没有找到车锁，请重新连接");
                    mContext.sendBroadcast(intent);
                }
            }
        }, 5000);
    }

    @Override
    public void disconnect() {
        scanLeDevice(false);
        LogUtil.d(TAG, TAG + " disconnect to bluetooth");
        Intent intent = new Intent(mContext, HostAppService.class);
        intent.setAction(CommunicationManager.ACTION_DISCONNECT_TO_DEVICE);
        mContext.startService(intent);
    }

    @Override
    public void raiseLock() {
        LogUtil.d(TAG, TAG + " raise lock");
        if (!CommunicationManager.getInstance().isBLEConnectted()) {
            connectToBluetooth();
            return;
        }
//        LogUtil.d(TAG, "mLockState is " + mLockState);
//        if (mLockState == LOCK_STATE.UP.ordinal()) {
//            LogUtil.i(TAG, "lock is up");
//        } else if (mLockState == LOCK_STATE.UPPING.ordinal()) {
//            LogUtil.i(TAG, "do nothing upping");
//        } else {
            Intent upIntent = new Intent(BLECommandIntent.SEND_BUTTON_EVENT);
            upIntent.putExtra(BLECommandIntent.EXTRA_IS_OWNER, true);
            upIntent.putExtra(BLECommandIntent.EXTRA_IS_UP, true);
            CommunicationManager.getInstance().sendBLEEvent(mContext, upIntent);
            mLockState = LOCK_STATE.UP.ordinal();
//        }
    }

    @Override
    public void downLock() {
        LogUtil.d(TAG, TAG + " down lock");
        if (!CommunicationManager.getInstance().isBLEConnectted()) {
            connectToBluetooth();
            return;
        }
//        LogUtil.d(TAG, "mLockState is " + mLockState);
//        if (mLockState == LOCK_STATE.DOWN.ordinal()) {
//            LogUtil.i(TAG, "lock is down");
//        } else if (mLockState == LOCK_STATE.DOWNING.ordinal()) {
//            LogUtil.i(TAG, "do nothing downing");
//        } else {
            Intent downIntent = new Intent(BLECommandIntent.SEND_BUTTON_EVENT);
            downIntent.putExtra(BLECommandIntent.EXTRA_IS_OWNER, true);
            downIntent.putExtra(BLECommandIntent.EXTRA_IS_UP, false);
            CommunicationManager.getInstance().sendBLEEvent(mContext, downIntent);
            mLockState = LOCK_STATE.DOWN.ordinal();
//        }
    }

    private void connectToBluetooth() {
        LogUtil.i(TAG, "reconnect to bluetooth");
        Intent connect = new Intent(mContext, HostAppService.class);
        connect.putExtra(CommunicationManager.EXTRA_NAME, mLockName);
        connect.putExtra(CommunicationManager.EXTRA_ADDRESS, mLockMac);
        connect.setAction(CommunicationManager.ACTION_CONNECT_TO_DEVICE);
        mContext.startService(connect);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogUtil.d(TAG, "lock name is " + device.getName() + ", mLockName is " + mLockName);
            if (device.getName() != null && device.getName().equals(mLockName)) {
                isFindLock = true;
                mLockMac = device.getAddress();
                scanLeDevice(false);
                Intent intent = new Intent(mContext, HostAppService.class);
                intent.putExtra(CommunicationManager.EXTRA_ADDRESS, mLockMac);
                intent.putExtra(CommunicationManager.EXTRA_NAME, mLockName);
                intent.setAction(CommunicationManager.ACTION_CONNECT_TO_DEVICE);
                mContext.startService(intent);
            }
        }
    };

}
