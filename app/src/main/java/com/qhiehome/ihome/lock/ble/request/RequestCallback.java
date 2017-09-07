package com.qhiehome.ihome.lock.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

public abstract interface RequestCallback {
	public abstract void onRequestComplete(
            BluetoothGattCharacteristic paramBluetoothGattCharacteristic,
            boolean paramBoolean, Request.REQUEST_TYPE paramREQUEST_TYPE);
}
