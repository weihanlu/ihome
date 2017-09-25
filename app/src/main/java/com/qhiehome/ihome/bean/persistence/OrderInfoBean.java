package com.qhiehome.ihome.bean.persistence;

public class OrderInfoBean {

    private int orderId;
    private int orderState;
    private long startTime;
    private long endTime;
    private String lockName;
    private String lockMac;
    private String lockPwd;
    private String gateWayId;
    private String estateName;
    private float estateLongitude;
    private float estateLatitude;

    public OrderInfoBean(int orderId, int orderState, long startTime, long endTime, String lockName, String lockMac, String lockPwd, String gateWayId, String estateName, float estateLongitude, float estateLatitude) {
        this.orderId = orderId;
        this.orderState = orderState;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lockName = lockName;
        this.lockMac = lockMac;
        this.lockPwd = lockPwd;
        this.gateWayId = gateWayId;
        this.estateName = estateName;
        this.estateLongitude = estateLongitude;
        this.estateLatitude = estateLatitude;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderState() {
        return orderState;
    }

    public void setOrderState(int orderState) {
        this.orderState = orderState;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public String getLockMac() {
        return lockMac;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }

    public String getLockPwd() {
        return lockPwd;
    }

    public void setLockPwd(String lockPwd) {
        this.lockPwd = lockPwd;
    }

    public String getGateWayId() {
        return gateWayId;
    }

    public void setGateWayId(String gateWayId) {
        this.gateWayId = gateWayId;
    }

    public String getEstateName() {
        return estateName;
    }

    public void setEstateName(String estateName) {
        this.estateName = estateName;
    }

    public float getEstateLongitude() {
        return estateLongitude;
    }

    public void setEstateLongitude(float estateLongitude) {
        this.estateLongitude = estateLongitude;
    }

    public float getEstateLatitude() {
        return estateLatitude;
    }

    public void setEstateLatitude(float estateLatitude) {
        this.estateLatitude = estateLatitude;
    }
}
