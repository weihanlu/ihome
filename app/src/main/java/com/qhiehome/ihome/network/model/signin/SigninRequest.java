package com.qhiehome.ihome.network.model.signin;

/**
 * Created by xiang on 2017/7/18.
 */

public class SigninRequest {


    /**
     * phone : 123123131
     */

    private String phone;

    public SigninRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
