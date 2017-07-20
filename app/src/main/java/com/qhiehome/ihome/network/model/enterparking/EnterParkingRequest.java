package com.qhiehome.ihome.network.model.enterparking;

/**
 * Created by YueMa on 2017/7/20.
 */

public class EnterParkingRequest {

    /**
     * phone : xxxx...xxxx
     * gateway_id : xxxxxx
     */

    private String phone;
    private String gateway_id;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGateway_id() {
        return gateway_id;
    }

    public void setGateway_id(String gateway_id) {
        this.gateway_id = gateway_id;
    }
}
