package com.qhiehome.ihome.network.model.park.publish;

public class PublishparkRequest {

    /**
     * parking_id : 123456789
     * password : xxxx...xxxx
     * start_time : 1499826000000
     * end_time : 1499828000000
     */

    private long parking_id;
    private String password;
    private long start_time;
    private long end_time;

    public long getParking_id() {
        return parking_id;
    }

    public void setParking_id(long parking_id) {
        this.parking_id = parking_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
