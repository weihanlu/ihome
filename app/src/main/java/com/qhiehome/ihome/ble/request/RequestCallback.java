package com.qhiehome.ihome.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

public interface RequestCallback {
     void onRequestComplete(BluetoothGattCharacteristic bluetoothGattCharacteristic,
                                            boolean paramBoolean, Request.REQUEST_TYPE request_type);
}
