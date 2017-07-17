package com.qhiehome.ihome.ble.primaryservice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.qhiehome.ihome.ble.profile.BLEClientThread;
import com.qhiehome.ihome.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.manager.CommunicationManager;
import com.qhiehome.ihome.ble.request.Request;
import com.qhiehome.ihome.ble.profile.IhomeService;
import com.qhiehome.ihome.util.APPUtils;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.util.UUIDMatcher;

public class PrimaryServiceThread extends BLEClientThread {
	private static UUIDMatcher sUUIDMatcher = new UUIDMatcher();
	private final String TAG = "SPPLEClientThread";
	private BluetoothGattService mService;
	private BluetoothGattCharacteristic mCharacteristicNotification;
	private BluetoothGattCharacteristic mCharacteristicWrite;

	static {
		sUUIDMatcher.addUUID(PrimaryServiceProfile.CHARACTERISTIC_WRITE_UUID, 1);
		sUUIDMatcher.addUUID(PrimaryServiceProfile.CHARACTERISTIC_NOTIFICATION_UUID, 2);
	}

	public PrimaryServiceThread(Context paramContext) {
		super("Primary_service_client", paramContext);
	}

	boolean receiveHead = false;

	@Override
	protected void onBLENotification(BluetoothGattCharacteristic characteristic) {
		if (characteristic == null) {
			LogUtil.e(TAG, "characteristic is null");
			return;
		}
		
		if (!characteristic.getUuid().equals(
				mCharacteristicNotification.getUuid())) {
			LogUtil.e(TAG, "Other characteristic !!!!" + characteristic);
			return;
		}
		
		byte[] wholeData = characteristic.getValue();
		
		String dataStr = APPUtils.byteArrayToString(wholeData);
//		APPUtils.showToast(mContext, dataStr, Toast.LENGTH_SHORT);
		LogUtil.i(TAG,"onNotify: " + dataStr);
		
		if (wholeData == null || wholeData.length < 1) {
			LogUtil.e(TAG,
					"invalid data, uuid: "
							+ PrimaryServiceProfile.getUUIDName(characteristic.getUuid()));
			return;
		}
		
	    int eventType = wholeData[0];
	    switch(eventType){
	    case PrimaryServiceProfile.EVENT_TYPE_ERROR_INFORMATION:
	    	if(wholeData.length >= 2){
				ToastUtil.showToast(mContext, "车位锁异常");
	    	}
	    	break;
	    	
	    case PrimaryServiceProfile.EVENT_TYPE_CURRENT_STATUS:
	    	if(wholeData.length >= 3){
	    		int passwordState = ((wholeData[1] & 0xf0) == 0x10) ? 0 : 1; //10:not set 20:set
	    		int lockState = wholeData[1] & 0x0f;     //1:low 2:mid 3:high
	    		int battery = wholeData[2];	    
	    		LogUtil.i(TAG, "EVENT_TYPE_CURRENT_STATUS, passwordState: " + passwordState +
	    				", lockState: " + lockState + ", battery: " + battery);
				Intent intent = new Intent(BLECommandIntent.RX_CURRENT_STATUS);
				intent.putExtra(BLECommandIntent.EXTRA_MM_SET_ALREADY, passwordState);//1:set 0:not set 
				intent.putExtra(BLECommandIntent.EXTRA_BATTERY_LEVEL, battery);
				intent.putExtra(BLECommandIntent.EXTRA_LOCK_STATE, lockState); //1:low 2:mid 3:high
				mContext.sendBroadcast(intent);
				
				// broadcast service_config_success
				Intent connect_intent = new Intent(
						mContext,
						IhomeService.class);
				connect_intent
						.setAction(CommunicationManager.ACTION_RX_STATUS_SUCCESS);
				mContext.startService(connect_intent);

 	    	}
	    	break;
	    	
	    case PrimaryServiceProfile.EVENT_TYPE_PASSWORD:
	    	if(wholeData.length >= 2){
	    		int action = wholeData[1] & 0x0f; //1:setting 2:check
	    		int result = wholeData[1] & 0xf0; //10: AD 20:USER 30:NG
				Intent intent = new Intent(BLECommandIntent.RX_PASSWORD_RESULT);
				intent.putExtra(BLECommandIntent.EXTRA_PSW_ACTION, action);
				intent.putExtra(BLECommandIntent.EXTRA_PSW_RESULT, result);
				
				//08-21 
				//when check owner's password successfully, we got customer's password
				if(action == 2 && result == 0x10 && wholeData.length >= 5){
					String customerMM = String.format("%02x%02x%02x", wholeData[2], wholeData[3], wholeData[4]);
					if(!customerMM.equals("000000")){
						intent.putExtra(BLECommandIntent.EXTRA_PSW_CUSTOMER,customerMM);
			    		LogUtil.i(TAG, "we got customer's password: " + customerMM);
					}
					getFwVersion();
				}
				mContext.sendBroadcast(intent);
	    	}
	    	break;
	    //new 1012	
	    case PrimaryServiceProfile.EVENT_TYPE_LOCK_ACTION:
	    	if(wholeData.length >= 3){
	    		if(wholeData[2] == 0x00){  
	    			//UP/DOWN response 
		    		int result = wholeData[1]; //10: AD 20:USER 30:NG
					Intent intent = new Intent(BLECommandIntent.RX_LOCK_RESULT);
					intent.putExtra(BLECommandIntent.EXTRA_LOCK_RESULT, result);
					mContext.sendBroadcast(intent);
	    		}else{
		    		int result = wholeData[2]; //10: AD 20:USER 30:NG
		    		if(result == 0x01){ //low
						Intent intent = new Intent(BLECommandIntent.RX_LOCK_RF_LOCK_STATE);
						intent.putExtra(BLECommandIntent.EXTRA_LOCK_RF_LOCK_STATE, true);
						mContext.sendBroadcast(intent);
		    		}else if(result == 0x03){  //high
						Intent intent = new Intent(BLECommandIntent.RX_LOCK_RF_LOCK_STATE);
						intent.putExtra(BLECommandIntent.EXTRA_LOCK_RF_LOCK_STATE, false);
						mContext.sendBroadcast(intent);
		    		}
	    		}
	    	}	    	
	    	break;
	    	
	    //new 1012
	    case PrimaryServiceProfile.EVENT_TYPE_AUTO_PARK:
	    	if(wholeData.length >= 5){
	    		int dataType = wholeData[1];
	    		if(dataType == 0x01){
	    			//app park result
	    			boolean reuslt = wholeData[2] == 0x10;
					Intent intent = new Intent(BLECommandIntent.RX_LCOK_AUTO_PARK_SET_RESULT);
					intent.putExtra(BLECommandIntent.EXTRA_LCOK_AUTO_PARK_SET_RESULT, reuslt);//1:up    2:down
					mContext.sendBroadcast(intent);
	    		}else if(dataType == 0x02){
	    			//auto car calibration result
					Intent intent = new Intent(BLECommandIntent.RX_LCOK_AUTO_PARK_CALI_RESULT);
	    			boolean reuslt = wholeData[2] == 0x10;
					intent.putExtra(BLECommandIntent.EXTRA_LCOK_AUTO_PARK_CALI_RESULT, reuslt); 
					mContext.sendBroadcast(intent);
	    		}else if(dataType == 0x03){
	    			//when car leave
	    			boolean start = (wholeData[2] == 0x01 ) ? true : false;
	    			boolean low   = (wholeData[3] == 0x01 ) ? true : false;
	    			boolean success   = (wholeData[4] == 0x10 ) ? true : false;
	    			if(start && !low && success){
	    				//start up
	    				Intent intent = new Intent(BLECommandIntent.RX_LOCK_RF_START_UP);
	    				mContext.sendBroadcast(intent);
	    			}else if(!start && !low && success){
	    				//stop up
						Intent intent = new Intent(BLECommandIntent.RX_LOCK_RF_STOP_UP);
						intent.putExtra(BLECommandIntent.EXTRA_LOCK_STOP_UP, true); 
						mContext.sendBroadcast(intent);
	    			}else if(!start && !low && !success){
	    				//stop up fail
						Intent intent = new Intent(BLECommandIntent.RX_LOCK_RF_STOP_UP);
						intent.putExtra(BLECommandIntent.EXTRA_LOCK_STOP_UP, false); 
						mContext.sendBroadcast(intent);
	    			}
	    			
	    		}else if(dataType == 0x04){
	    			//report support function
	    			boolean auto_park  = (wholeData[2] & 0x10) == 0x10 ? true : false;
	    			boolean rf         = (wholeData[2] & 0x01) == 0x01 ? true : false;
	    			boolean auto_park_status =(wholeData[3] & 0x10) == 0x10;
	    			
	    			LogUtil.d(TAG, "auto_park: " + auto_park + " ,rf: " + rf + ", auto_park_status: " + auto_park_status);
	    			
	    			APPUtils.setAutoPark(mContext, auto_park);
	    			APPUtils.setRemoteStudy(mContext, rf);
	    			APPUtils.setAutoParkStatus(mContext, auto_park_status);
	    		}
	    		
				break;
	    	}	    	
	    	break;
	    	
	    case PrimaryServiceProfile.EVENT_TYPE_DEVICE_INFO:
	    	break;
	    	
	    //NEW 
	    case PrimaryServiceProfile.EVENT_TYPE_REMOTE_STUDY:
	    	if(wholeData.length >= 5){
	    		if(wholeData[1] == 0x01){
	    			//study finish
	    			boolean sucess = (wholeData[2] == 0x10) ? true : false;
					Intent intent = new Intent(BLECommandIntent.RX_STUDY_RESULT);
					intent.putExtra(BLECommandIntent.EXTRA_LCOK_STUDY_RESULT, sucess); 
					mContext.sendBroadcast(intent);
	    		}else{
	    			//confrim address
	    			boolean sucess = (wholeData[2] == 0x10) ? true : false;
					Intent intent = new Intent(BLECommandIntent.RX_SAVE_STUDY_RESULT);
					intent.putExtra(BLECommandIntent.EXTRA_SAVE_STUDY_RESULT, sucess); 
					mContext.sendBroadcast(intent);
	    		}

	    	}
	    	break;
	    	
	    case PrimaryServiceProfile.EVENT_TYPE_FWVERSION:
	    	if(wholeData.length >= 4){
	    		String version = String.format("V%d.%d.%d", wholeData[1], wholeData[2],wholeData[3]);
	    		APPUtils.setFwVersion(mContext, version);
 	    	}
	    	break;

	    default:
	    }

//		String parseStr = PrimaryServiceProfile.parseReceiveData(wholeData);
//		if(BleDemoApplication.debug_parse_receive){
//			Intent intent = new Intent(BLECommandIntent.LOG_DATA);
//			intent.putExtra(BLECommandIntent.EXTRA_LOG_DATA, parseStr);
//			mContext.sendBroadcast(intent);
//		}
	}

	@Override
	// no use now...
	protected void onBLERequestComplete(
			BluetoothGattCharacteristic characteristic, boolean paramBoolean,
			Request.REQUEST_TYPE type, byte[] data) {
		
		LogUtil.i(TAG, "onBLERequestComplete!!! REQUEST_TYPE: " + type + ", result: " + paramBoolean);

		if(type.equals(Request.REQUEST_TYPE.READ)
				&& characteristic.getUuid().equals(mCharacteristicNotification.getUuid()) ){
			Intent intent = new Intent(BLECommandIntent.REGIST_NOTIFICATION_RESULT);
			intent.putExtra(BLECommandIntent.EXTRA_RESULT_REGIST_NOTIFICATION, paramBoolean);
			mContext.sendBroadcast(intent);
			return;
		}
		
		if(type.equals(Request.REQUEST_TYPE.UNREG_NOTIFY)
				&& characteristic.getUuid().equals(mCharacteristicNotification.getUuid()) ){
			Intent intent = new Intent(BLECommandIntent.UNREGIST_NOTIFICATION_RESULT);
			intent.putExtra(BLECommandIntent.EXTRA_RESULT_UNREGIST_NOTIFICATION, paramBoolean);
			mContext.sendBroadcast(intent);
			return;
		}
		
		if(type.equals(Request.REQUEST_TYPE.WRITE) &&
				characteristic.getUuid().equals(mCharacteristicWrite.getUuid())){
//			String parseStr = PrimaryServiceProfile.parseSendData(mContext, data);
////			LogUtils.i("BLEClientThread", "onRequestComplete 3...: " +  APPUtils.byteArrayToString(data));
//
//			Intent intent = new Intent(BLECommandIntent.LOG_DATA);
//			intent.putExtra(BLECommandIntent.EXTRA_LOG_DATA, parseStr);
//			mContext.sendBroadcast(intent);
		}

	}

	@Override
	protected void onCommand(Intent intent) {
		LogUtil.i(TAG, "onCommand ===========" + intent.getAction());

		String action = intent.getAction();
		if (TextUtils.isEmpty(action)) {
			LogUtil.e(TAG, "Action is empty");
			return;
		}

		if (BLECommandIntent.ACTION_START_CONFIG.equals(action)) {
			LogUtil.i(TAG, "Start config now..." + intent.getAction());
			// init service and chara
			if (configSPPLEService()){

				// broadcast service_config_success
				Intent connect_intent = new Intent(
						mContext,
						IhomeService.class);
				connect_intent
						.setAction(CommunicationManager.ACTION_SERVICE_CONFIG_SUCCESS);
				mContext.startService(connect_intent);
				connect_intent = null;
				LogUtil.e(TAG, "Config success...");

			} else {
				LogUtil.e(TAG, "Config fail...");
				Intent disconnect_intent = new Intent(mContext,
						IhomeService.class);
				disconnect_intent
						.setAction(CommunicationManager.ACTION_SERVICE_CONFIG_FAIL);
				mContext.startService(disconnect_intent);
				disconnect_intent = null;
			}
			return;
		}
		
		if (BLECommandIntent.ACTION_HANDLE_CONNECTION_STATE_CHANGE
				.equals(action)) {
			if (intent.getBooleanExtra(
					BLECommandIntent.EXTRA_CONNECTION_STATE_NEW, false)) {
				doInitTask();
				sendCurrentTime();
			}
			return;
		}
		
		if(BLECommandIntent.SEND_DATA.equals(action)){
			byte[] data_hex_array = intent.getByteArrayExtra(BLECommandIntent.EXTRA_DATA);
			if(data_hex_array == null){
				LogUtil.e(TAG, "SEND_DATA  data_hex_array == null.");
				return;
			}
			if(mCharacteristicWrite == null)
				return;
			mCharacteristicWrite.setValue(data_hex_array);
			getRequestManager().addRequest(mCharacteristicWrite, this,
					Request.REQUEST_TYPE.WRITE);
			return;
		}
		
		if(BLECommandIntent.SEND_CURRENT_TIME.equals(action)){
			LogUtil.i(TAG, "send current time...");
			sendCurrentTime();
			return;
		}
		
		if(BLECommandIntent.ENABLE_NOTIFICATION.equals(action)){
			if(intent.getBooleanExtra(BLECommandIntent.EXTRA_ENABLE_OR_NOT,false)){
				enbaleNotificationAndReadData(true);
			}else{
				enbaleNotificationAndReadData(false);
			}
			return;
		}
		
		if(BLECommandIntent.SETTING_PASSWORD.equals(action)){
			int[] mm = intent.getIntArrayExtra(BLECommandIntent.EXTRA_PASSWORD);
			int role = intent.getIntExtra(BLECommandIntent.EXTRA_ROLE, -1);
			setpassword(mm, role);
			return;
		}
		
		if(BLECommandIntent.CHECKING_PASSWORD.equals(action)){
			int[] mm = intent.getIntArrayExtra(BLECommandIntent.EXTRA_PASSWORD);
			int role = intent.getIntExtra(BLECommandIntent.EXTRA_ROLE, -1);
			checkpassword(mm, role);
			return;
		}
		
		if(BLECommandIntent.SEND_BUTTON_EVENT.equals(action)){
			boolean isUp = intent.getBooleanExtra(BLECommandIntent.EXTRA_IS_UP, false);
			boolean isOwner = intent.getBooleanExtra(BLECommandIntent.EXTRA_IS_OWNER, false);
			sendButtonEvent(isUp, isOwner);
			return;
		}
		
		if(BLECommandIntent.SEND_AUTO_PARK.equals(action)){
			boolean isEnable = intent.getBooleanExtra(BLECommandIntent.EXTRA_IS_ENABLE, false);
			setAutoPark(isEnable);
			return;
		}
		
		if(BLECommandIntent.SEND_AUTO_PARK_CALI.equals(action)){
			startAutoParkCali();
			return;
		}
		
		if(BLECommandIntent.SEND_STUDY_START.equals(action)){
			startAddressStudy();
			return;
		}
		
		if(BLECommandIntent.SEND_STUDY_SAVE.equals(action)){
			boolean isSave = intent.getBooleanExtra(BLECommandIntent.EXTRA_SEND_STUDY_SAVE, false);
			confirmSaveAddress(isSave);
			return;
		}

		if(BLECommandIntent.DEBUG_MODE_ENTER.equals(action)){
//			goToDebugMode();
		}

		if(BLECommandIntent.ACTION_HEART_BEAT.equals(action)){
			getRequestManager().addRequest(
					PrimaryServiceProfile.getHeartBeatChara(mCharacteristicWrite), this,
					Request.REQUEST_TYPE.WRITE);
		}
		
	}

	private boolean configSPPLEService() {

		mService = null;
		mCharacteristicWrite = null;
		mCharacteristicNotification = null;
		
		mService = getRequestManager().getGattService(PrimaryServiceProfile.SERVICE_UUID);
		mCharacteristicWrite = getRequestManager().getGattChara(
				PrimaryServiceProfile.SERVICE_UUID,
				PrimaryServiceProfile.CHARACTERISTIC_WRITE_UUID);
		mCharacteristicNotification = getRequestManager().getGattChara(
				PrimaryServiceProfile.SERVICE_UUID,
				PrimaryServiceProfile.CHARACTERISTIC_NOTIFICATION_UUID);
		LogUtil.i(TAG, "----config resule---- mService: " + mService
				+ ", mCharacteristicWrite: " + mCharacteristicWrite
				+ ", mCharacteristicNotification: " + mCharacteristicNotification);

		// config chara
		if (mCharacteristicWrite != null && mCharacteristicNotification != null) {
			return true;
		} else {
			LogUtil.e(TAG,
					"mCharacteristics are not all available, Can not configSPPLEService!!!");
			return false;
		}
	}

	private void doInitTask() {
		LogUtil.i(TAG, "doInitTask, registerNotification");
		getRequestManager().registerNotification(mCharacteristicNotification, this);
		getRequestManager().addRequest(mCharacteristicNotification, this,
			Request.REQUEST_TYPE.READ);
	}

	private boolean enbaleNotificationAndReadData(boolean isEnable) {
		if(isEnable){
			LogUtil.i(TAG, "add RegisterNotification...");
			getRequestManager().registerNotification(mCharacteristicNotification,
				this);
			getRequestManager().addRequest(mCharacteristicNotification, this,
				Request.REQUEST_TYPE.READ);
		}else{
			LogUtil.i(TAG, "add UnRegisterNotification...");
			getRequestManager().unregisterNotification(mCharacteristicNotification,this);
		}
		return true;
	}
	
	private void sendCurrentTime() {
		LogUtil.i(TAG, "add a write request, sendCurrentTime...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getSyncTimeChara(mCharacteristicWrite), this,
				Request.REQUEST_TYPE.WRITE);
	}

	private void getFwVersion() {
		LogUtil.i(TAG, "add a getFwVersion request, request for fw...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getFwVersionChara(mCharacteristicWrite), this,
				Request.REQUEST_TYPE.WRITE);
	}

	private void setpassword(int[] password, int role) {
		LogUtil.i(TAG, "add a write request, setpassword...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getSettingMMChara(mCharacteristicWrite, password, role), this,
				Request.REQUEST_TYPE.WRITE);
	}
	
	private void checkpassword(int[] password, int role) {
		LogUtil.i(TAG, "add a write request, checkpassword...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getCheckingMMChara(mCharacteristicWrite, password, role), this,
				Request.REQUEST_TYPE.WRITE);
	}
	
	private void sendButtonEvent(boolean up, boolean owner) {
		LogUtil.i(TAG, "add a write request, checkpassword...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getButtonEvent(mCharacteristicWrite, up, owner), this,
				Request.REQUEST_TYPE.WRITE);
	}

	public void tearDown() {
		super.tearDown();
	}
	
	//new
	private void setAutoPark(boolean isEnable) {
		LogUtil.i(TAG, "add a write request, setAutoPark...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getAutoParkChara(mCharacteristicWrite, isEnable), this,
				Request.REQUEST_TYPE.WRITE);
 	}

	//new
	private void startAutoParkCali() {
		LogUtil.i(TAG, "add a write request, setAutoParkCali...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getAutoParkCaliChara(mCharacteristicWrite), this,
				Request.REQUEST_TYPE.WRITE);
 	}
	
	//new
	private void startAddressStudy() {
		LogUtil.i(TAG, "add a write request, StartAddressStudy...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getStartAddressStudyChara(mCharacteristicWrite), this,
				Request.REQUEST_TYPE.WRITE);
 	}

	//new
	private void confirmSaveAddress(boolean save) {
		LogUtil.i(TAG, "add a write request, confirmSaveAddress...");
		getRequestManager().addRequest(
				PrimaryServiceProfile.getConfirmSaveAddressChara(mCharacteristicWrite,save), this,
				Request.REQUEST_TYPE.WRITE);
 	}
}
