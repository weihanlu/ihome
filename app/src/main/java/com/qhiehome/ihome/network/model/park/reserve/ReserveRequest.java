package com.qhiehome.ihome.network.model.park.reserve;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveRequest {


    /**
     * phone : xxxx...xxxx
     * start_time : 1499826000000
     * end_time : 1499828000000
     */

    private String phone;
    private long start_time;
    private long end_time;

    public ReserveRequest(String phone, long start_time, long end_time) {
        this.phone = phone;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }
}
