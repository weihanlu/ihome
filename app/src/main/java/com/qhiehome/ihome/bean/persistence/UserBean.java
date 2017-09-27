package com.qhiehome.ihome.bean.persistence;

public class UserBean {

    private String phoneNum;

    public UserBean(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
