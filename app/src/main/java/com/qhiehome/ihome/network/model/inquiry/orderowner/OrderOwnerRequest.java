package com.qhiehome.ihome.network.model.inquiry.orderowner;

/**
 * Created by YueMa on 2017/8/25.
 */

public class OrderOwnerRequest {

    /**
     * phone : xxxxxxxxx
     */

    private String phone;

    public OrderOwnerRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
