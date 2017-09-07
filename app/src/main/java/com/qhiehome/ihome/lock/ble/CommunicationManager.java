package com.qhiehome.ihome.lock.ble;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qhiehome.ihome.lock.ble.profile.BLECommandService;
import com.qhiehome.ihome.util.LogUtil;


public final class CommunicationManager {
	private final String TAG = "CommunicationManager";
	
	/** Connect/Disconnect request when: APP start; DeviceScan;NFC **/
	public static final String ACTION_UI_LUANCH = "com.cram.bledemo.action.app_launch";
	public static final String ACTION_CONNECT_TO_DEVICE = "com.cram.bledemo.action.connect_to_device";
	public static final String EXTRA_CONNECT_FROM_NFC = "connect_from_nfc_receiver";
	public static final String EXTRA_ADDRESS = "address";
	public static final String EXTRA_NAME = "name";
	public static final String ACTION_DISCONNECT_TO_DEVICE = "com.cram.bledemo.action.disconnect_to_device";
    //public static final String ACTION_WRITE_SUCCES = "com.cram.bledemo.action.write_success";
	
	/** When config success, post connect_success notification **/
	public static final String ACTION_SERVICE_CONFIG_SUCCESS = "com.cram.bledemo.action.config_success";
	
	/** When config success, post connect_success notification **/
	public static final String ACTION_RX_STATUS_SUCCESS = "com.cram.bledemo.action.ACTION_RX_STATUS_SUCCESS";

	/** If config fail, disconnect with device and re-connect **/
	public static final String ACTION_SERVICE_CONFIG_FAIL = "com.cram.bledemo.action.config_fail";
	public static final String ACTION_CONNECTION_STATE_CHANGE = "com.cram.connection_state_change";
	
	public static String ACTION_DEVICE_FRIST_TIME = "com.cram.ACTION_DEVICE_FRIST_TIME";
	public static String ACTION_DEVICE_FRIST_TIMEOUT = "com.cram.ACTION_DEVICE_FRIST_TIMEOUT";

	
	public static final String EXTRA_CONNECTION_STATE_OLD = "extra.connection_state_old";
	public static final String EXTRA_CONNECTION_STATE_NEW = "extra.connection_state_new";
	public static  final int BLE_CONNECTTED = 1;
	public static  final int BLE_CONNECTING = 2;
	public static  final int BLE_DISCONNECTTING = 3;
	public static  final int BLE_DISCONNECTTED = 4;
	public static  final int STATE_INIT = -1;
	private              int CONNECTION_STATE = STATE_INIT;
	private              int OLD_CONNECTION_STATE = STATE_INIT;

	public static  int RECONNECT_TIME = 0;
	
	// work around, for bugs... 
	public boolean HOST_DISCONNECT = false;  //just for mainfragment
	public String  DEVICE_REAL_NAME = "";
	
	private static CommunicationManager sInstance;

	public static CommunicationManager getInstance() {
		if (sInstance == null) {
			sInstance = new CommunicationManager();
		}
		return sInstance;
	}

	public void sendBLEEvent(Context context, String action) {
		Intent intent = new Intent(context, BLECommandService.class);
		intent.setAction(action);
		context.startService(intent);
		intent = null;
	}

	public boolean isBleConnecttedOrConnectting(){
		return (CONNECTION_STATE == BLE_CONNECTING) || (CONNECTION_STATE == BLE_CONNECTTED);
	}
	
	public boolean isBleConnectting(){
		return CONNECTION_STATE == BLE_CONNECTING;
	}

	public boolean isConnectted(){
		return CONNECTION_STATE != BLE_CONNECTING;
	}
	
	public boolean isBLEConnectted(){
		return CONNECTION_STATE == BLE_CONNECTTED;
	}
	
	public int getConnectionState(){
		return CONNECTION_STATE;
	}
	
	public int getOldConnectionState(){
		return OLD_CONNECTION_STATE;
	}
	
	public boolean isOldStateConnectted(){
		return OLD_CONNECTION_STATE == BLE_CONNECTTED;
	}

	public void sendBLEEvent(Context context, Intent intent) {
		if (intent == null || intent.getAction() == null) {
			LogUtil.d(TAG, "Invalid intent, do not send ble event");
			return;
		}

		sendBLEEvent(context,intent.getAction(), intent.getExtras());
	}

	public void sendBLEEvent(Context context, String action, Bundle data) {
		if(!isBLEConnectted()){
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
		if(data!=null)
			intent.putExtras(data);
		context.startService(intent);
	}
	
	public void sendBTStateChanged(Context context, int newState) {
		if(CONNECTION_STATE == BLE_CONNECTTED | CONNECTION_STATE == BLE_DISCONNECTTED){
			RECONNECT_TIME = 0;
		}
		OLD_CONNECTION_STATE = CONNECTION_STATE;
		CONNECTION_STATE = newState;
		Intent intent = new Intent(ACTION_CONNECTION_STATE_CHANGE);
		intent.putExtra(EXTRA_CONNECTION_STATE_NEW, newState);
		LogUtil.d(TAG,"old: " + OLD_CONNECTION_STATE + ", new: " + CONNECTION_STATE);
		context.sendBroadcast(intent);
	}
}
