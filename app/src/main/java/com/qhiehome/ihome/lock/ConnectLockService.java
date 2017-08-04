package com.qhiehome.ihome.lock;

import android.app.IntentService;
import android.content.Intent;

import com.qhiehome.ihome.lock.gateway.GateWayClient;


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
    public void onCreate() {
        super.onCreate();
        gateWayClient = GateWayClient.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String gateWayId = intent.getStringExtra(EXTRA_GATEWAY_ID);
            String lockMac = intent.getStringExtra(EXTRA_LOCK_MAC);
            String lockPwd = intent.getStringExtra(EXTRA_LOCK_PWD);
            gateWayClient.setGateWayId(gateWayId);
            gateWayClient.setLockMac(lockMac);
            switch (action) {
                case ACTION_GATEWAY_CONNECT:
                    handleActionGateWayConnect(gateWayId, lockMac);
                    break;
                case ACTION_BLUETOOTH_CONNECT:
                    handleActionBluetoothConnect(lockMac, lockPwd);
                    break;
                case ACTION_DISCONNECT:
                    gateWayClient.disconnect();
                    break;
                case ACTION_UP_LOCK:
                    gateWayClient.raiseLock();
                    break;
                case ACTION_DOWN_LOCK:
                   gateWayClient.downLock();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *
     * @param lockMac lock mac address
     * @param lockPwd lock password
     */
    private void handleActionBluetoothConnect(String lockMac, String lockPwd) {
        connectByBluetooth();
    }

    /**
     *
     * @param gateWayId denote the unique gateway
     * @param lockMac lock mac address
     */
    private void handleActionGateWayConnect(String gateWayId, String lockMac) {
        connectByGateway(gateWayId, lockMac);
    }

    private void connectByGateway(String gateWayId, String lockMac) {
        gateWayClient.connect();
    }

    private void connectByBluetooth() {

    }

}
