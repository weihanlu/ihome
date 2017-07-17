package com.qhiehome.ihome.ble.profile;

import java.util.Arrays;
import java.util.List;

public class BLECommandIntent {

	public static final String ACTION_START_CONFIG = "action.start_config";
	
	public static final String ACTION_HANDLE_CONNECTION_STATE_CHANGE = "action.blethread_handle_connection_state_change";
	public static final String EXTRA_CONNECTION_STATE_OLD = "extra.handle_connection_state_old";
	public static final String EXTRA_CONNECTION_STATE_NEW = "extra.handle_connection_state_new";

	public static final String SEND_CURRENT_TIME = "action.send_current_time";
	public static final String SEND_DATA = "action.send_data";
	public static final String EXTRA_DATA = "extra.data_send";
	public static final String ENABLE_NOTIFICATION = "action.enable_notificaiton_203";
	public static final String EXTRA_ENABLE_OR_NOT = "extra.enable_notificaiton_203";
	
	//send password
	public static final String SETTING_PASSWORD = "action.SETTING_PASSWORD";
	public static final String CHECKING_PASSWORD = "action.CHECKING_PASSWORD";
	public static final String EXTRA_PASSWORD = "extra.EXTRA_PASSWORD";
	public static final String EXTRA_ROLE = "extra.EXTRA_ROLE";

	//send button event
	public static final String SEND_BUTTON_EVENT = "action.SEND_BUTTON_EVENT";
	public static final String EXTRA_IS_OWNER = "action.CHECKING_PASSWORD";
	public static final String EXTRA_IS_UP = "extra.EXTRA_IS_UP";
	
	//send auto park
	public static final String SEND_AUTO_PARK = "action.SEND_AUTO_PARK";
	public static final String EXTRA_IS_ENABLE = "extra.EXTRA_IS_ENABLE";
	
	//send auto park cali
	public static final String SEND_AUTO_PARK_CALI = "action.SEND_AUTO_PARK_CALI";

	//start study
	public static final String SEND_STUDY_START = "action.SEND_STUDY_START";
	public static final String SEND_STUDY_SAVE = "action.SEND_STUDY_SAVE";
	public static final String EXTRA_SEND_STUDY_SAVE = "extra.EXTRA_SEND_STUDY_SAVE";
	
	//heart beat
	public static final String ACTION_HEART_BEAT = "action.ACTION_HEART_BEAT";

	//feedback
	public static final String LOG_DATA = "action.log_data";
	public static final String EXTRA_LOG_DATA = "extra.log_data";
	public static final String REGIST_NOTIFICATION_RESULT = "action.result_enable_notificaiton_203";
	public static final String EXTRA_RESULT_REGIST_NOTIFICATION = "extra.result_enable_notificaiton_203";
	public static final String UNREGIST_NOTIFICATION_RESULT = "action.result_unregist_notificaiton_203";
	public static final String EXTRA_RESULT_UNREGIST_NOTIFICATION = "extra.result_unregist_notificaiton_203";
	public static final String TX_LOCK_ACTION = "action.lock_action"; 
	public static final String EXTRA_LOCK_ACTION = "extra.lock_action";
	
	//Current status
	public static final String RX_CURRENT_STATUS = "action.RX_CURRENT_STATUS";
	public static final String EXTRA_MM_SET_ALREADY = "extra.mm_state";
	public static final String EXTRA_LOCK_STATE = "extra.lock_state";
	public static final String EXTRA_BATTERY_LEVEL = "extra.battery_level";

	//address used
	public static final String EXTRA_LOCK_ADDRESS = "extra.EXTRA_LOCK_ADDRESS";
	public static final String EXTRA_LOCK_NAME = "extra.lock_name";

	//Password setting/check
	public static final String RX_PASSWORD_RESULT = "action.RX_PASSWORD_RESULT";
	public static final String EXTRA_PSW_ACTION = "extra.psw_action";
	public static final String EXTRA_PSW_RESULT = "extra.psw_result";
	public static final String EXTRA_PSW_CUSTOMER = "extra.EXTRA_PSW_CUSTOMER";

	public static final String RX_CURRENT_STATUS_CHANGE = "action.RX_CURRENT_STATUS_CHANGE";
	
	//Lock result
	public static final String RX_LOCK_RESULT = "action.RX_LOCK_RESULT";
	public static final String EXTRA_LOCK_RESULT = "extra.lock_result";

    //lock motion beacuse of auto park
	public static final String RX_LCOK_AUTO_PARK = "action.RX_LCOK_AUTO_PARK";
	public static final String EXTRA_LCOK_AUTO_PARK_STATUS = "extra.lock_auto_park_result";
	public static final String EXTRA_LCOK_AUTO_PARK_ACTION = "extra.lock_auto_park_action";
	
	public static final String RX_LCOK_AUTO_PARK_SET_RESULT = "action.RX_LCOK_AUTO_PARK_SET_RESULT";
	public static final String EXTRA_LCOK_AUTO_PARK_SET_RESULT = "extra.EXTRA_LCOK_AUTO_PARK_SET_RESULT";
	
	public static final String RX_LCOK_AUTO_PARK_CALI_RESULT = "action.RX_LCOK_AUTO_PARK_CALI_RESULT";
	public static final String EXTRA_LCOK_AUTO_PARK_CALI_RESULT = "extra.EXTRA_LCOK_AUTO_PARK_CALI_RESULT";
	
	public static final String RX_STUDY_RESULT = "action.RX_STUDY_RESULT";
	public static final String EXTRA_LCOK_STUDY_RESULT = "extra.EXTRA_LCOK_STUDY_RESULT";
	
	public static final String RX_SAVE_STUDY_RESULT = "action.RX_SAVE_STUDY_RESULT";
	public static final String EXTRA_SAVE_STUDY_RESULT = "extra.EXTRA_SAVE_STUDY_RESULT";

	
	public static final String RX_LOCK_RF_START_UP = "action.RX_LOCK_RF_START_UP";
	public static final String RX_LOCK_RF_STOP_UP = "action.RX_LOCK_RF_STOP_UP";
	public static final String EXTRA_LOCK_STOP_UP  = "extra.EXTRA_LOCK_RF_STOP_UP";
	public static final String RX_LOCK_RF_LOCK_STATE  = "extra.RX_LOCK_RF_LOCK_STATE";
	public static final String EXTRA_LOCK_RF_LOCK_STATE  = "extra.EXTRA_LOCK_RF_LOCK_STATE";

	public static final String FW_VERSION = "com.qhiehome.ihome.ble.FW_VERSION";
	public static final String EXTRA_FW_VERSION = "extra.EXTRA_FW_VERSION";

	//debug
	public static final String  DEBUG_MODE_ENTER = "DEBUG_MODE_ENTER";

	private static String[] SPPLE_ACTION_LIST = new String[] {
			ACTION_START_CONFIG,
			ACTION_HANDLE_CONNECTION_STATE_CHANGE, 
			SEND_CURRENT_TIME,
			SEND_DATA,
			ENABLE_NOTIFICATION,
			SETTING_PASSWORD,
			CHECKING_PASSWORD,
			SEND_BUTTON_EVENT,
			SEND_AUTO_PARK,
			DEBUG_MODE_ENTER,
			SEND_AUTO_PARK_CALI,
			SEND_STUDY_START,
			SEND_STUDY_SAVE,
			ACTION_HEART_BEAT
	};

	
	public static final List<String> PrimaryServiceActionList = Arrays
			.asList(SPPLE_ACTION_LIST);

}