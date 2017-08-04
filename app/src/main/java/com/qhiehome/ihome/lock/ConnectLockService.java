package com.qhiehome.ihome.lock;

import android.app.IntentService;
import android.content.Intent;

import com.qhiehome.ihome.lock.gateway.GateWayClient;
import com.qhiehome.ihome.util.LogUtil;


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

    public static final String EXTRA_GATEWAY_ID = "com.qhiehome.ihome.lock.extra.GATEWAYID";
    public static final String EXTRA_LOCK_MAC = "com.qhiehome.ihome.lock.extra.LOCKMAC";
    public static final String EXTRA_LOCK_PWD = "com.qhiehome.ihome.lock.extra.LOCKPWD";

    public static final String BROADCAST_CONNECT = "com.qhiehome.ihome.lock.broad.CONNECT";

    public ConnectLockService() {
        super("ConnectLockService");
    }

    private GateWayClient gateWayClient;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String lockMac = intent.getStringExtra(EXTRA_LOCK_MAC);
            switch (action) {
                case ACTION_GATEWAY_CONNECT:
                    String gateWayId = intent.getStringExtra(EXTRA_GATEWAY_ID);
                    gateWayClient = GateWayClient.getInstance(this);
                    gateWayClient.setGateWayId(gateWayId);
                    gateWayClient.setLockMac(lockMac);
                    handleActionGateWayConnect();
                    break;
                case ACTION_BLUETOOTH_CONNECT:
                    String lockPwd = intent.getStringExtra(EXTRA_LOCK_PWD);
                    handleActionBluetoothConnect(lockMac, lockPwd);
                    break;
                case ACTION_DISCONNECT:
                    gateWayClient = GateWayClient.getInstance(this);
                    gateWayClient.disconnect();
                    break;
                case ACTION_UP_LOCK:
                    gateWayClient = GateWayClient.getInstance(this);
                    gateWayClient.raiseLock();
                    break;
                case ACTION_DOWN_LOCK:
                    gateWayClient = GateWayClient.getInstance(this);
                    gateWayClient.downLock();
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

    private void handleActionBluetoothConnect(String lockMac, String lockPwd) {
        connectByBluetooth(lockMac, lockPwd);
    }

    private void connectByBluetooth(String lockMac, String lockPwd) {

    }

}
