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
    private int orderId;

    public AccountBalanceRequest(String phone, double accountChange) {
        this.phone = phone;
        this.accountChange = accountChange;
    }

    public AccountBalanceRequest(String phone, double accountChange, int orderId) {
        this.phone = phone;
        this.accountChange = accountChange;
        this.orderId = orderId;
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

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
