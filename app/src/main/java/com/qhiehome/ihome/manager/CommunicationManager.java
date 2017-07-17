package com.qhiehome.ihome.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qhiehome.ihome.ble.profile.BLECommandService;
import com.qhiehome.ihome.util.LogUtil;

/**
 * This is the global communication manager to store all actions and constant values.
 */

public class CommunicationManager {

    private static final String TAG = "CommunicationManager";

    // connect/Disconnect request when app start
    public static final String ACTION_UI_LAUNCH = "com.qhiehome.ihome.manager.action.app_launch";

    public static final String ACTION_CONNECT_TO_DEVICE = "com.qhiehome.ihome.manager.action.connect_to_device";
    public static final String ACTION_DISCONNECT_TO_DEVICE = "com.qhiehome.ihome.manager.action.disconnect_to_device";

    /** When config success, post connect_success notification **/
    public static final String ACTION_RX_STATUS_SUCCESS = "com.qhiehome.ihome.manager.ACTION_RX_STATUS_SUCCESS";
    /** When config success, post connect_success notification **/
    public static final String ACTION_SERVICE_CONFIG_SUCCESS = "com.qhiehome.ihome.manger.config_success";
    /** If config fail, disconnect with device and re-connect **/
    public static final String ACTION_SERVICE_CONFIG_FAIL = "com.qhiehome.ihome.manger.config_fail";

    /** If config fail, disconnect with device and re-connect **/
    public static final String ACTION_CONNECTION_STATE_CHANGE = "com.qhiehome.ihome.manager.connection_state_change";

    public static final String EXTRA_CONNECTION_STATE_NEW = "extra.connection_state_new";

    public static final String ACTION_DEVICE_FIRST_TIME = "com.qhiehome.ihome.manager.ACTION_DEVICE_FRIST_TIME";
    public static final String ACTION_DEVICE_FIRST_TIMEOUT = "com.qhiehome.ihome.manager.ACTION_DEVICE_FRIST_TIMEOUT";
    //
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_NAME = "name";

    // connect state
    public static final int BLE_CONNECTED = 1;
    public static final int BLE_CONNECTING = 2;
    public static final int BLE_DISCONNECTED = 3;
    public static final int BLE_INIT_CONNECT = -1;

    private int connect_state = BLE_INIT_CONNECT;
    private int old_connection_state = BLE_INIT_CONNECT;

    public static int RECONNECT_TIME = 0;

    public boolean HOST_DISCONNECT = false;  //just for mainfragment
    public String  DEVICE_REAL_NAME = "";

    // singleton pattern
    private CommunicationManager(){}
    private static class CommunicationManagerHelper {
        private static final CommunicationManager instance = new CommunicationManager();
    }
    public static CommunicationManager getInstance() {
        return CommunicationManagerHelper.instance;
    }

    public boolean isBleConnectingOrConnected() {
        return (connect_state == BLE_CONNECTING) || (connect_state == BLE_CONNECTED);
    }

    public boolean isBleConnected() {
        return connect_state == BLE_CONNECTED;
    }

    public boolean isBleConnecting() {
        return connect_state == BLE_CONNECTING;
    }

    public void sendBLEEvent(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            LogUtil.d(TAG, "Invalid intent, do not send ble event");
            return;
        }

        sendBLEEvent(context,intent.getAction(), intent.getExtras());
    }

    public void sendBLEEvent(Context context, String action) {
        Intent intent = new Intent(context, BLECommandService.class);
        intent.setAction(action);
        context.startService(intent);
        intent = null;
    }

    public void sendBLEEvent(Context context, String action, Bundle data) {
        if(!isBleConnected()){
            LogUtil.e(TAG,"no Connection,do not send ble event");
            return;
        }

        if(action == null){
            LogUtil.e(TAG,"no action,do not send ble event");
            return;
        }

        LogUtil.d(TAG, "Receive BLE event send request: " + action);

        Intent intent = new Intent(context, BLECommandService.class);
        intent.setAction(action);
        if(data!=null) {
            intent.putExtras(data);
        }
        context.startService(intent);
    }

    public void sendBTStateChanged(Context context, int newState) {
        if(connect_state == BLE_CONNECTED | connect_state == BLE_DISCONNECTED){
            RECONNECT_TIME = 0;
        }
        old_connection_state = connect_state;
        connect_state = newState;
        Intent intent = new Intent(ACTION_CONNECTION_STATE_CHANGE);
        intent.putExtra(EXTRA_CONNECTION_STATE_NEW, newState);
        LogUtil.d(TAG,"old: " + old_connection_state + ", new: " + connect_state);
        context.sendBroadcast(intent);
    }


}
