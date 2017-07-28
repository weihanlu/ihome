package com.qhiehome.ihome.network.model.park.reserve;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveRequest {

    public ReserveRequest(int shareId) {
        this.shareId = shareId;
    }

    /**
     * shareId : 123456789
     */

    private int shareId;

    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }
}
