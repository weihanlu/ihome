package com.qhiehome.ihome.lock.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

public class Request {
	private BluetoothGattCharacteristic mCharacteristic;
	private byte[] data;
	private RequestCallback mRequestCallback;
	private REQUEST_TYPE mType;
	private boolean isInit;
	
	public Request(BluetoothGattCharacteristic characteristic,
	        REQUEST_TYPE type, RequestCallback requestCallback) {
		mCharacteristic = characteristic;
		mType = type;
		mRequestCallback = requestCallback;
		data = characteristic.getValue();
		isInit = false;
	}

	public Request(BluetoothGattCharacteristic characteristic,
	        REQUEST_TYPE type, RequestCallback requestCallback,boolean isInit) {
		mCharacteristic = characteristic;
		mType = type;
		mRequestCallback = requestCallback;
		data = characteristic.getValue();
		this.isInit = isInit;
	}

	public BluetoothGattCharacteristic getCharacteristic() {
		return mCharacteristic;
	}

	public RequestCallback getRequestCallback() {
		return mRequestCallback;
	}

	public REQUEST_TYPE getType() {
		return mType;
	}

	public static enum REQUEST_TYPE {
		READ, WRITE, REG_NOTIFY, UNREG_NOTIFY,
	}
	
	public byte[] getValue() {
		return data;
	}
	
	public boolean isInit() {
		return isInit;
	}

}
