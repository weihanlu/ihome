package com.qhiehome.ihome.network.model.park.reserve;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveRequest {

    /**
     * phone : f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2
     * estateId : 1
     * startTime : 1600000000000
     * endTime : 1600000000001
     */

    private String phone;
    private int estateId;
    private long startTime;
    private long endTime;

    public ReserveRequest(String phone, int estateId, long startTime, long endTime) {
        this.phone = phone;
        this.estateId = estateId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
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
