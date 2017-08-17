package com.qhiehome.ihome.network.model.park.reserve;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveRequest {

    /**
     * phone : xxxx...xxxx
     * shareId : 123456789
     * startTime : 1499826000000
     * endTime : 1499828000000
     */

    private String phone;
    private int shareId;
    private long startTime;
    private long endTime;

    public ReserveRequest(String phone, int shareId, long startTime, long endTime) {
        this.phone = phone;
        this.shareId = shareId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
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
