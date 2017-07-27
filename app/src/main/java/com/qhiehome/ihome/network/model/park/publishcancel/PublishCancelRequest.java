package com.qhiehome.ihome.network.model.park.publishcancel;

/**
 * Created by YueMa on 2017/7/21.
 */

public class PublishCancelRequest {

    /**
     * share_id : 123456789
     * password : xxxx...xxxx
     */

    private long shareId;
    private String password;

    public PublishCancelRequest(long shareId, String password) {
        this.shareId = shareId;
        this.password = password;
    }

    public long getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
