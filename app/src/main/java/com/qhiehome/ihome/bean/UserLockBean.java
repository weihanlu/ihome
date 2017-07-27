package com.qhiehome.ihome.bean;

public class UserLockBean {

    private String lockEstateName;

    private String lockName;

    private String gatewayId;

    private String lockMac;

    private boolean isRented;

    public UserLockBean(String lockEstateName, String lockName, String gatewayId, String lockMac, boolean isRented) {
        this.lockName = lockName;
        this.lockEstateName = lockEstateName;
        this.gatewayId = gatewayId;
        this.lockMac = lockMac;
        this.isRented = isRented;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
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
