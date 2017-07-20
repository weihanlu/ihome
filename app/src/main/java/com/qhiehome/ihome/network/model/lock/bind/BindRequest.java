package com.qhiehome.ihome.network.model.lock.bind;

public class BindRequest {

    /**
     * phone : xxxx...xxxx
     * estate_id : 123456789
     * parking_name : xxxxxx
     * lock_password : xxxx...xxxx
     * lock_gateway_id : xxxxxx
     */

    private String phone;
    private int estate_id;
    private String parking_name;
    private String lock_password;
    private String lock_gateway_id;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getEstate_id() {
        return estate_id;
    }

    public void setEstate_id(int estate_id) {
        this.estate_id = estate_id;
    }

    public String getParking_name() {
        return parking_name;
    }

    public void setParking_name(String parking_name) {
        this.parking_name = parking_name;
    }

    public String getLock_password() {
        return lock_password;
    }

    public void setLock_password(String lock_password) {
        this.lock_password = lock_password;
    }

    public String getLock_gateway_id() {
        return lock_gateway_id;
    }

    public void setLock_gateway_id(String lock_gateway_id) {
        this.lock_gateway_id = lock_gateway_id;
    }
}
