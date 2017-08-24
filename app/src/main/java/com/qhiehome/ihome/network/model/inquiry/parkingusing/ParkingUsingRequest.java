package com.qhiehome.ihome.network.model.inquiry.parkingusing;

/**
 * Created by YueMa on 2017/8/24.
 */

public class ParkingUsingRequest {

    /**
     * phone : xxxxxxxxx
     */

    private String phone;

    public ParkingUsingRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
