package com.qhiehome.ihome.network.model.pay;

/**
 * Created by YueMa on 2017/7/20.
 */

public class PayResponse {

    /**
     * errcode : 0
     * errmsg : success
     */

    private int errcode;
    private String errmsg;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
