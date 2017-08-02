package com.qhiehome.ihome.network.model.inquiry.reserveowned;

/**
 * Created by xiang on 2017/8/2.
 */

public class ReserveOwnedRequest {

    /**
     * phone : xxxx...xxxx
     */

    private String phone;

    public ReserveOwnedRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
