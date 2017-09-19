package com.qhiehome.ihome.network.model.pay;

import com.qhiehome.ihome.activity.PayActivity;

/**
 * Created by YueMa on 2017/9/18.
 */

public class PayRequest {

    /**
     * orderId : 123456789
     * channel : 1
     * fee : 5.0
     */

    private int orderId;
    private int channel;
    private double fee;

    public PayRequest(int orderId, int channel, double fee) {
        this.orderId = orderId;
        this.channel = channel;
        this.fee = fee;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }
}
