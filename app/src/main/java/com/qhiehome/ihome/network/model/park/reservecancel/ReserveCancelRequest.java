package com.qhiehome.ihome.network.model.park.reservecancel;

/**
 * Created by YueMa on 2017/8/11.
 */

public class ReserveCancelRequest {

    public ReserveCancelRequest(int orderId) {
        this.orderId = orderId;
    }

    /**
     * orderId : 123456789
     */

    private int orderId;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
