package com.qhiehome.ihome.network.model.alipay;

public class AliPayRequest {


    /**
     * orderId : xxx
     */

    private int orderId;

    public AliPayRequest(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
