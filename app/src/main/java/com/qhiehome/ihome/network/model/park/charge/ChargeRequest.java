package com.qhiehome.ihome.network.model.park.charge;

/**
 * Created by YueMa on 2017/7/20.
 */

public class ChargeRequest {

    /**
     * phone : xxxx...xxxx
     * leaveTime : 1499826000000
     */

    private String phone;
    private long leaveTime;

    public ChargeRequest(String phone, long leaveTime) {
        this.phone = phone;
        this.leaveTime = leaveTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(long leaveTime) {
        this.leaveTime = leaveTime;
    }
}
