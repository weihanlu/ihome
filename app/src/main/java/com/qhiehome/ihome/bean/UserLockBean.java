package com.qhiehome.ihome.bean;

public class UserLockBean {

    private String lockEstateName;

    private String parkingName;

    private int parkingId;

    private String gatewayId;

    private String lockMac;

    private boolean isRented;

    public UserLockBean(String lockEstateName, String parkingName, int parkingId, String gatewayId, String lockMac, boolean isRented) {
        this.parkingName = parkingName;
        this.parkingId = parkingId;
        this.lockEstateName = lockEstateName;
        this.gatewayId = gatewayId;
        this.lockMac = lockMac;
        this.isRented = isRented;
    }

    public String getParkingName() {
        return parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public int getParkingId() {
        return parkingId;
    }

    public void setParkingId(int parkingId) {
        this.parkingId = parkingId;
    }

    public String getLockEstateName() {
        return lockEstateName;
    }

    public void setLockEstateName(String lockEstateName) {
        this.lockEstateName = lockEstateName;
    }

    public boolean isRented() {
        return isRented;
    }

    public void setRented(boolean rented) {
        isRented = rented;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getLockMac() {
        return lockMac;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }
}
