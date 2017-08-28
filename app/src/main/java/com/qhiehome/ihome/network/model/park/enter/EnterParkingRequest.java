package com.qhiehome.ihome.network.model.park.enter;

public class EnterParkingRequest {

    /**
     * phone : xxxx...xxxx
     * enterTime : 1499826000000
     */

    private String phone;
    private long enterTime;

    public EnterParkingRequest(String phone, long enterTime) {
        this.phone = phone;
        this.enterTime = enterTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }
}
