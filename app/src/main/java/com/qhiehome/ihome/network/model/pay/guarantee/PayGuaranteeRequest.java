package com.qhiehome.ihome.network.model.pay.guarantee;

/**
 * Created by YueMa on 2017/8/21.
 */

public class PayGuaranteeRequest {

    /**
     * phone : xxxx...xxxx
     * orderId : 123456789
     * shareId : 123456789
     */

    private String phone;
    private int orderId;
    private int shareId;

    public PayGuaranteeRequest(String phone, int orderId, int shareId) {
        this.phone = phone;
        this.orderId = orderId;
        this.shareId = shareId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }
}
