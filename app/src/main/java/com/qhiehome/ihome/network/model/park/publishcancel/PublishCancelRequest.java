package com.qhiehome.ihome.network.model.park.publishcancel;

/**
 * Created by YueMa on 2017/7/21.
 */

public class PublishCancelRequest {

    /**
     * share_id : 123456789
     * password : xxxx...xxxx
     */

    private int share_id;
    private String password;

    public int getShare_id() {
        return share_id;
    }

    public void setShare_id(int share_id) {
        this.share_id = share_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
