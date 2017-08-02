package com.qhiehome.ihome.network.model.park.reserve;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveRequest {


    /**
     * shareId : 123456789
     * phone : xxxx...xxxx
     * startTime : 1499826000000
     * endTime : 1499828000000
     */

    private int shareId;
    private String phone;
    private long startTime;
    private long endTime;

    public ReserveRequest(int shareId, String phone, long startTime, long endTime) {
        this.shareId = shareId;
        this.phone = phone;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
