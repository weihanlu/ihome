package com.qhiehome.ihome.network.model.pay.accountbalance;

/**
 * Created by YueMa on 2017/8/24.
 */

public class AccountBalanceRequest {

    /**
     * phone : xxxx...xxxx
     * accountChange : -10.0
     */

    private String phone;
    private double accountChange;

    public AccountBalanceRequest(String phone, double accountChange) {
        this.phone = phone;
        this.accountChange = accountChange;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getAccountChange() {
        return accountChange;
    }

    public void setAccountChange(double accountChange) {
        this.accountChange = accountChange;
    }
}
