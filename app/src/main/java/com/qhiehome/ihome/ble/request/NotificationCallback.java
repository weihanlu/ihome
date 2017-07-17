package com.qhiehome.ihome.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * This class.. ?
 */

public interface NotificationCallback {
    void onNotification(
            BluetoothGattCharacteristic characteristic);
}
