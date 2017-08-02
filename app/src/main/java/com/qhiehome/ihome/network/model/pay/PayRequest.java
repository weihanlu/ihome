package com.qhiehome.ihome.network.model.pay;

/**
 * Created by YueMa on 2017/7/20.
 */

public class PayRequest {


    /**
     * orderId : 123456789
     * enterTime : 1499826000000
     * leaveTime : 1499826000000
     * paymentTime : 1499826000000
     */

    private int orderId;
    private long enterTime;
    private long leaveTime;
    private long paymentTime;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }

    public long getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(long leaveTime) {
        this.leaveTime = leaveTime;
    }

    public long getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(long paymentTime) {
        this.paymentTime = paymentTime;
    }
}
