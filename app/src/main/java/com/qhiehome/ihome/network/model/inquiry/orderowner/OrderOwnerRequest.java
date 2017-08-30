package com.qhiehome.ihome.network.model.inquiry.orderowner;

/**
 * Created by YueMa on 2017/8/25.
 */

public class OrderOwnerRequest {

    /**
     * parkingId : 123456789
     */

    private int parkingId;

    public OrderOwnerRequest(int parkingId) {
        this.parkingId = parkingId;
    }

    public int getParkingId() {
        return parkingId;
    }

    public void setParkingId(int parkingId) {
        this.parkingId = parkingId;
    }
}
