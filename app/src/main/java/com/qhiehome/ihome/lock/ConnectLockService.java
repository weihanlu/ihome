package com.qhiehome.ihome.lock;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.qhiehome.ihome.lock.bluetooth.BluetoothClient;
import com.qhiehome.ihome.lock.gateway.GateWayClient;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.ToastUtil;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ConnectLockService extends IntentService {

    private static final String TAG = "ConnectLockService";

    public static final String ACTION_GATEWAY_CONNECT = "com.qhiehome.ihome.lock.action.GATEWAY_CONNECT";
    public static final String ACTION_BLUETOOTH_CONNECT = "com.qhiehome.ihome.lock.action.BLUETOOTH_CONNECT";
    public static final String ACTION_DISCONNECT = "com.qhiehome.ihome.lock.action.DISCONNECT";
    public static final String ACTION_UP_LOCK = "com.qhiehome.ihome.lock.action.UP_LOCK";
    public static final String ACTION_DOWN_LOCK = "com.qhiehome.ihome.lock.action.DOWN_LOCK";

    public static final String ACTION_LOCK_STATE = "com.qhiehome.ihom.lock.action.LOCK_STATE";

    public static final String EXTRA_GATEWAY_ID = "com.qhiehome.ihome.lock.extra.GATEWAY_ID";
    public static final String EXTRA_LOCK_MAC = "com.qhiehome.ihome.lock.extra.LOCK_MAC";
    public static final String EXTRA_LOCK_NAME = "com.qhiehome.ihome.lock.extra.LOCK_NAME";

    public static final String BROADCAST_CONNECT = "com.qhiehome.ihome.lock.broad.CONNECT";

    public ConnectLockService() {
        super("ConnectLockService");
    }

    private GateWayClient gateWayClient;

    private BluetoothClient bluetoothClient;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            int lockState = intent.getIntExtra(ACTION_LOCK_STATE, -1);
            switch (action) {
                case ACTION_GATEWAY_CONNECT:
                    String gateWayId = intent.getStringExtra(EXTRA_GATEWAY_ID);
                    String lockMac = intent.getStringExtra(EXTRA_LOCK_MAC);
                    gateWayClient = GateWayClient.getInstance(this);
                    gateWayClient.setGateWayId(gateWayId);
                    gateWayClient.setLockMac(lockMac);
                    handleActionGateWayConnect();
                    break;
                case ACTION_BLUETOOTH_CONNECT:
                    String lockName = intent.getStringExtra(EXTRA_LOCK_NAME);
                    bluetoothClient = BluetoothClient.getInstance(this);
                    bluetoothClient.setLockName(lockName);
                    handleActionBluetoothConnect();
                    break;
                case ACTION_DISCONNECT:
                    if (NetworkUtils.isConnected(this)) {
                        gateWayClient = GateWayClient.getInstance(this);
                        gateWayClient.disconnect();
                    } else {
                        bluetoothClient = BluetoothClient.getInstance(this);
                        bluetoothClient.disconnect();
                    }
                    break;
                case ACTION_UP_LOCK:
                    if (NetworkUtils.isConnected(this)) {
                        gateWayClient = GateWayClient.getInstance(this);
                        gateWayClient.raiseLock();
                    } else {
                        bluetoothClient = BluetoothClient.getInstance(this);
                        bluetoothClient.setLockState(lockState);
                        bluetoothClient.raiseLock();
                    }
                    break;
                case ACTION_DOWN_LOCK:
                    if (NetworkUtils.isConnected(this)) {
                        gateWayClient = GateWayClient.getInstance(this);
                        gateWayClient.downLock();
                    } else {
                        bluetoothClient = BluetoothClient.getInstance(this);
                        bluetoothClient.setLockState(lockState);
                        bluetoothClient.downLock();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void handleActionGateWayConnect() {
        connectByGateway();
    }

    private void connectByGateway() {
        gateWayClient.connect();
    }

    private void handleActionBluetoothConnect() {
        connectByBluetooth();
    }

    private void connectByBluetooth() {
        bluetoothClient.connect();
    }

}
