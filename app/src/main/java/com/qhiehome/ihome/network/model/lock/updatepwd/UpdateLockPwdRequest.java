package com.qhiehome.ihome.network.model.lock.updatepwd;

public class UpdateLockPwdRequest {


    /**
     * lock_id : 123456789
     * oldpwd : xxxx..xxx
     * newpwd : xxxxxx
     */

    private int lock_id;
    private String oldpwd;
    private String newpwd;

    public int getLock_id() {
        return lock_id;
    }

    public void setLock_id(int lock_id) {
        this.lock_id = lock_id;
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
