package com.qhiehome.ihome.network.model.pay.notify;

/**
 * Created by YueMa on 2017/9/19.
 */

public class PayNotifyRequest {

    /**
     * orderId : 2
     * channel : 3
     * fee : 5.0
     */

    private int orderId;
    private int channel;
    private double fee;

    public PayNotifyRequest(int orderId, int channel, double fee) {
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
