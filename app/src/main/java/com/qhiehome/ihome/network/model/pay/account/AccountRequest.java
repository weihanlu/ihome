package com.qhiehome.ihome.network.model.pay.account;

/**
 * Created by YueMa on 2017/9/19.
 */

public class AccountRequest {

    /**
     * phone : xxxx...xxxx
     * accountChange : 10.0
     * channel : 2
     */

    private String phone;
    private double accountChange;
    private int channel;

    public AccountRequest(String phone, double accountChange, int channel) {
        this.phone = phone;
        this.accountChange = accountChange;
        this.channel = channel;
    }

    public AccountRequest(String phone, double accountChange) {
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

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }
}
