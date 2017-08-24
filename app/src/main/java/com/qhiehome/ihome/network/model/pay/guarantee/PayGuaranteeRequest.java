package com.qhiehome.ihome.network.model.pay.guarantee;

/**
 * Created by YueMa on 2017/8/21.
 */

public class PayGuaranteeRequest {

    /**
     * orderId : 123456789
     */

    private int orderId;

    public PayGuaranteeRequest(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }


}
