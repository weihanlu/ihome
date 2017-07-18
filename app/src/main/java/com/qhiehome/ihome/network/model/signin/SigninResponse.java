package com.qhiehome.ihome.network.model.signin;

/**
 *
 */

public class SigninResponse {


    /**
     * errcode : 0
     * errmsg : success
     */

    private int errcode;
    private String errmsg;

    public SigninResponse(int errcode, String errmsg) {
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

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
