package com.qhiehome.ihome.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * a device use bluetooth low enery tech.
 */

public class BLEDevice implements Parcelable{

    private String name;
    private String address;

    private int role;   // ??
    private int isLock; // ??
    private int isDefault; // ??

    public BLEDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getIsLock() {
        return isLock;
    }

    public void setIsLock(int isLock) {
        this.isLock = isLock;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BLEDevice bleDevice = (BLEDevice) o;

        return address.equals(bleDevice.address);

    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeInt(role);
        dest.writeInt(isLock);
        dest.writeInt(isDefault);
    }

    public static final Creator<BLEDevice> CREATOR = new Creator<BLEDevice>() {
        @Override
        public BLEDevice createFromParcel(Parcel source) {
            BLEDevice bleDevice = new BLEDevice(source.readString(), source.readString());
            bleDevice.setRole(source.readInt());
            bleDevice.setRole(source.readInt());
            bleDevice.setIsDefault(source.readInt());
            return bleDevice;
        }

        @Override
        public BLEDevice[] newArray(int size) {
            return new BLEDevice[size];
        }
    };

}
