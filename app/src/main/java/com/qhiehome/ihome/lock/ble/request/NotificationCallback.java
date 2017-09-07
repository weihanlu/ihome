package com.qhiehome.ihome.lock.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

public abstract interface NotificationCallback {
	public abstract void onNotification(
            BluetoothGattCharacteristic characteristic);
}
