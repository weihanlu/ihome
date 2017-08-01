package com.qhiehome.ihome.network.model.lock.updatepwd;

public class UpdateLockPwdRequest {


    /**
     * lock_id : 123456789
     * oldpwd : xxxx..xxx
     * newpwd : xxxxxx
     */

    private int parkingId;
    private String oldpwd;
    private String newpwd;

    public UpdateLockPwdRequest(int parkingId, String oldpwd, String newpwd) {
        this.parkingId = parkingId;
        this.oldpwd = oldpwd;
        this.newpwd = newpwd;
    }

    public int getParkingId() {
        return parkingId;
    }

    public void setParkingId(int parkingId) {
        this.parkingId = parkingId;
    }

    public String getOldpwd() {
        return oldpwd;
    }

    public void setOldpwd(String oldpwd) {
        this.oldpwd = oldpwd;
    }

    public String getNewpwd() {
        return newpwd;
    }

    public void setNewpwd(String newpwd) {
        this.newpwd = newpwd;
    }
}
