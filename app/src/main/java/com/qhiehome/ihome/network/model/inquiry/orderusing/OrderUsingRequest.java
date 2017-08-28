package com.qhiehome.ihome.network.model.inquiry.orderusing;

/**
 * Created by YueMa on 2017/8/25.
 */

public class OrderUsingRequest {

    /**
     * phone : xxxxxxxxx
     */

    private String phone;

    public OrderUsingRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
