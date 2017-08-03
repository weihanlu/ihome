package com.qhiehome.ihome.lock;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.qhiehome.ihome.lock.gateway.GateWayClient;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NetworkUtils;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class ConnectLockService extends IntentService {

    private static final String TAG = "ConnectLockService";

    public static final String ACTION_CONNECT = "com.qhiehome.ihome.lock.action.CONNECT";
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
            String gateWayId = intent.getStringExtra(EXTRA_GATEWAY_ID);
            String lockMac = intent.getStringExtra(EXTRA_LOCK_MAC);
            String lockPwd = intent.getStringExtra(EXTRA_LOCK_PWD);
            gateWayClient = GateWayClient.getInstance(this, gateWayId, lockMac);
            switch (action) {
                case ACTION_CONNECT:
                    handleActionConnect(gateWayId, lockMac, lockPwd);
                    break;
                case ACTION_DISCONNECT:
                    handleActionDisconnect();
                    break;
                case ACTION_UP_LOCK:
                    if (gateWayClient != null) {
                        gateWayClient.publishMessage("[01:01]");
                    }
                    break;
                case ACTION_DOWN_LOCK:
                    if (gateWayClient != null) {
                        gateWayClient.publishMessage("[01:02]");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *
     * @param gateWayId denote the unique gateway
     * @param lockMac lock mac address
     * @param lockPwd lock address
     */
    private void handleActionConnect(String gateWayId, String lockMac, String lockPwd) {
        // 1. use gateway to connect lock, max connect times is 3
        if (NetworkUtils.isConnected(this)) {
            connectByGateway(gateWayId, lockMac);
        }
        // 2. use bluetooth to connect lock, max connect times is 3
        if (!gateWayClient.isConnected()) {
            connectByBluetooth();
        }
    }

    private void connectByBluetooth() {

    }

    private void connectByGateway(String gateWayId, String lockMac) {
        gateWayClient.connect();
    }

    private void handleActionDisconnect() {
        gateWayClient.disconnect();
    }
}
