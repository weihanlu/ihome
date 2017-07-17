package com.qhiehome.ihome.ble.primaryservice;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;

import com.qhiehome.ihome.bean.LockBean;
import com.qhiehome.ihome.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.util.APPUtils;

import java.util.HashMap;
import java.util.UUID;

public class PrimaryServiceProfile {

	public static String SERVICE = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
	public static String WRITE_CHARACTERISTIC = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"; // TX
	public static String NOTIFICATION_CHARACTERISTIC = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";// RX

	public static UUID SERVICE_UUID = UUID.fromString(SERVICE);
	public static UUID CHARACTERISTIC_WRITE_UUID = UUID
			.fromString(WRITE_CHARACTERISTIC);
	public static UUID CHARACTERISTIC_NOTIFICATION_UUID = UUID
			.fromString(NOTIFICATION_CHARACTERISTIC);

	private static HashMap<UUID, String> sUUIDNameMap = new HashMap<UUID, String>();

	public static final int EVENT_TYPE_DEBUG = 0x00; 
	public static final int EVENT_TYPE_ERROR_INFORMATION = 0x01;
	public static final int EVENT_TYPE_CURRENT_TIME = 0x02;
	public static final int EVENT_TYPE_CURRENT_STATUS = 0x03;
	public static final int EVENT_TYPE_PASSWORD = 0x04;
	public static final int EVENT_TYPE_LOCK_ACTION = 0x05;
	public static final int EVENT_TYPE_AUTO_PARK = 0x06;
	public static final int EVENT_TYPE_DEVICE_INFO = 0x07;
	public static final int EVENT_TYPE_FWVERSION = 0x08;
	public static final int EVENT_TYPE_REMOTE_STUDY = 0x09;
	public static final int EVENT_TYPE_HEART_BEAT = 0x0A;
	
	public static final String TAG = "SPPLEProfile";

	static {
		sUUIDNameMap.put(SERVICE_UUID, "write_uuid");
		sUUIDNameMap.put(CHARACTERISTIC_NOTIFICATION_UUID, "notification_uuid");
	}

	public static String getUUIDName(UUID uuid) {
		return sUUIDNameMap.get(uuid);
	}

	public static String parseReceiveData(byte[] data) {

		StringBuilder builder = new StringBuilder();
		builder.append("RX: ");
		builder.append(APPUtils.byteArrayToString(data));
		builder.append("\n");
		int eventType = data[0];
		switch (eventType) {
		case EVENT_TYPE_ERROR_INFORMATION:
			builder.append("<Error information>");
			if (data.length >= 2) {
				builder.append("<Error code: " + data[1] + " >");
			}
			break;

		case EVENT_TYPE_CURRENT_STATUS:
			builder.append("<Current status>");
			if (data.length >= 3) {
				builder.append("<Status: " + data[1] + " >");
				builder.append("<Battery : " + data[2] + "% >");
			}
			break;

		case EVENT_TYPE_PASSWORD:
			builder.append("<Password>");
			if (data.length >= 2) {
				String role = data[1] == 0 ? "Admin" : "User";
				builder.append("<Permission: " + role + " >");
			}
			break;

		default:
			builder.append("<Unknow event type:" + eventType + ">");
		}
		return builder.toString();
	}

	public static String parseSendData(Context context, byte[] data) {

		StringBuilder builder = new StringBuilder();
		builder.append("TX: ");
		builder.append(APPUtils.byteArrayToString(data));
		builder.append("\n");
		int eventType = data[0];
		switch (eventType) {

		case EVENT_TYPE_CURRENT_TIME:
			builder.append("<Current time:>");
			if (data.length >= 8) {
				builder.append("<Time: "
						+ APPUtils.datebcd2string(data).substring(2) + " >");
			}
			break;

		case EVENT_TYPE_PASSWORD:
			builder.append("<Password>");
			if (data.length >= 5) {
				if ((data[1] & 0xf0) == 0x10)
					builder.append("<Adimi; ");
				else if ((data[1] & 0xf0) == 0x20)
					builder.append("<User; ");

				if ((data[1] & 0x0f) == 1)
					builder.append("Setting; ");
				else if ((data[1] & 0x0f) == 2)
					builder.append("check");

				builder.append("MM: ");
				builder.append(APPUtils.byteArrayToDeciString(APPUtils
						.bcd2int(new byte[] { data[2], data[3], data[4] })));
			}
			break;

		case EVENT_TYPE_LOCK_ACTION:
			builder.append("<Button event>");
			if (data.length >= 3) {
				if ((data[1] & 0xf0) == 0x10)
					builder.append("<Adimi; ");
				else if ((data[1] & 0xf0) == 0x20)
					builder.append("<User; ");

				if (data[2] == 1)
					builder.append("; Close>");
				else if (data[2] == 0)
					builder.append("; Open>");
				Intent intent = new Intent(BLECommandIntent.TX_LOCK_ACTION);
				intent.putExtra(BLECommandIntent.EXTRA_LOCK_ACTION,
						data[2] == 0);
				context.sendBroadcast(intent);
			}
			break;

		case EVENT_TYPE_AUTO_PARK:
			builder.append("<Auto park>");
			if (data.length >= 2) {
				String role = data[1] == 0 ? "Disable" : "Enable";
				builder.append("<set: " + role + " >");
			}
			break;

		default:
			builder.append("<Unknown event type:" + eventType + ">");
		}
		return builder.toString();
	}

	public static BluetoothGattCharacteristic getSyncTimeChara(
			BluetoothGattCharacteristic mCharacteristic1) {
		byte time[] = APPUtils.date2bcd();
		byte[] data = new byte[8];
		data[0] = (byte) EVENT_TYPE_CURRENT_TIME;
		for (int i = 0; i <= 6; i++) {
			data[i + 1] = time[i];
		}
		if (data != null)
			mCharacteristic1.setValue(data);
		return mCharacteristic1;
	}

	public static BluetoothGattCharacteristic getSettingMMChara(
			BluetoothGattCharacteristic mCharacteristicWrite, int[] password,
			int role) {
		// TODO Auto-generated method stub
		byte[] data = new byte[5];
		byte[] MM = APPUtils.int2bcd(password);
		data[0] = (byte) EVENT_TYPE_PASSWORD;
		if (role == LockBean.LOCK_ROLE.OWNER.ordinal())
			data[1] = (byte) 0x11;
		else if (role == LockBean.LOCK_ROLE.CUSTOMER.ordinal())
			data[1] = (byte) 0x21;
		for (int i = 0; i <= 2; i++) {
			data[i + 2] = MM[i];
		}
		if (data != null)
			mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getCheckingMMChara(
			BluetoothGattCharacteristic mCharacteristicWrite, int[] password,
			int role) {
		byte[] data = new byte[5];
		byte[] MM = APPUtils.int2bcd(password);
		data[0] = (byte) EVENT_TYPE_PASSWORD;
		if (role == LockBean.LOCK_ROLE.OWNER.ordinal())
			data[1] = (byte) 0x12;
		else if (role == LockBean.LOCK_ROLE.CUSTOMER.ordinal())
			data[1] = (byte) 0x22;
		for (int i = 0; i <= 2; i++) {
			data[i + 2] = MM[i];
		}
		if (data != null)
			mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getButtonEvent(
			BluetoothGattCharacteristic mCharacteristicWrite, boolean up,
			boolean owner) {
		byte data[] = new byte[3];
		data[0] = (byte) EVENT_TYPE_LOCK_ACTION;
		data[1] = (byte) (owner ? 0x10 : 0x20);
		data[2] = (byte) (up ? 0x01 : 0x02);
		mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getAutoParkChara(
			BluetoothGattCharacteristic mCharacteristicWrite, boolean isEnable) {
		byte data[] = new byte[5];
		data[0] = (byte) EVENT_TYPE_AUTO_PARK;
		data[1] = (byte) (0x01);
		data[2] = (byte) (isEnable ? 0x02 : 0x01);
		data[3] = (byte) (0x00);
		data[4] = (byte) (0x00);
		mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}
	
	public static BluetoothGattCharacteristic getAutoParkCaliChara(
			BluetoothGattCharacteristic mCharacteristicWrite) {
		byte data[] = new byte[5];
		data[0] = (byte) EVENT_TYPE_AUTO_PARK;
		data[1] = (byte) (0x02);
		data[2] = (byte) (0x01);
		data[3] = (byte) (0x00);
		data[4] = (byte) (0x00);
		mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getDebugModeChara(
			BluetoothGattCharacteristic mCharacteristicWrite) {
		byte data[] = new byte[3];
		data[0] = (byte) EVENT_TYPE_DEBUG;
		data[1] = (byte) 0x03;
		data[2] = (byte) 0x00;
		mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getStartAddressStudyChara(
			BluetoothGattCharacteristic mCharacteristicWrite) {
		byte data[] = new byte[5];
		data[0] = (byte) EVENT_TYPE_REMOTE_STUDY;
		data[1] = (byte) 0x01;
		data[2] = (byte) 'r';
		data[3] = (byte) 'f';
		data[4] = (byte) 's';
		mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getConfirmSaveAddressChara(
			BluetoothGattCharacteristic mCharacteristicWrite, boolean save) {
		byte data[] = new byte[5];
		data[0] = (byte) EVENT_TYPE_REMOTE_STUDY;
		data[1] = (byte) 0x02;
		if(save){
			data[2] = (byte) 'c';
			data[3] = (byte) 'f';
			data[4] = (byte) 'm';
		}else{
			data[2] = (byte) 'r';
			data[3] = (byte) 'e';
			data[4] = (byte) 'j';
		}
		mCharacteristicWrite.setValue(data);
		return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getHeartBeatChara(BluetoothGattCharacteristic mCharacteristicWrite){
			byte data[] = new byte[3];
			data[0] = (byte) EVENT_TYPE_HEART_BEAT;
			data[1] = (byte) 0x01;
			data[2] = (byte) 0x00;
			mCharacteristicWrite.setValue(data);
			return mCharacteristicWrite;
	}

	public static BluetoothGattCharacteristic getFwVersionChara(
			BluetoothGattCharacteristic mCharacteristicWrite) {
		mCharacteristicWrite.setValue(new byte[]{0x08, 'f', 'w'});
		return mCharacteristicWrite;
	}

}
