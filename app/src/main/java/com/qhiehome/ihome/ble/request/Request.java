package com.qhiehome.ihome.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * The class function ?
 */

public class Request {

    private BluetoothGattCharacteristic mCharacteristic;
    private byte[] data;
    private RequestCallback mRequestCallback;
    private REQUEST_TYPE mType;
    private boolean isInit;

    public Request(BluetoothGattCharacteristic characteristic, REQUEST_TYPE type, RequestCallback requestCallack) {
        mCharacteristic = characteristic;
        mType = type;
        mRequestCallback = requestCallack;
        data = mCharacteristic.getValue();
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

    public static enum REQUEST_TYPE {
        READ, WRITE, REG_NOTIFY, UNREG_NOTIFY,
    }

    public BluetoothGattCharacteristic getmCharacteristic() {
        return mCharacteristic;
    }

    public RequestCallback getmRequestCallback() {
        return mRequestCallback;
    }

    public REQUEST_TYPE getmType() {
        return mType;
    }

    public byte[] getValue() {
        return data;
    }

    public boolean isInit() {
        return isInit;
    }
}
