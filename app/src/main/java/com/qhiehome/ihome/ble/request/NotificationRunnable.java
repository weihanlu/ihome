package com.qhiehome.ihome.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Arrays;

/**
 * This class ?
 */

public class NotificationRunnable implements Runnable {

    private final BluetoothGattCharacteristic mCharacteristic;

    public NotificationRunnable(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            throw new IllegalArgumentException("BluetoothGattCharacteristic is not allowed to be null");
        }
        mCharacteristic = new BluetoothGattCharacteristic(characteristic.getUuid(), characteristic.getProperties()
                            , characteristic.getPermissions());
        mCharacteristic.setWriteType(characteristic.getWriteType());
        if (characteristic.getValue() != null) {
            mCharacteristic.setValue(Arrays.copyOf(characteristic.getValue(), characteristic.getValue().length));
        }
    }

    @Override
    public void run() {
        RequestManager.getInstance().newNotification(this.mCharacteristic);
    }
}
