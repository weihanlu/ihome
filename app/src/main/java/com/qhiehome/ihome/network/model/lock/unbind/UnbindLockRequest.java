package com.qhiehome.ihome.network.model.lock.unbind;

public class UnbindLockRequest {


    /**
     * lock_id : 123456789
     * lock_password : xxxx...xxxx
     */

    private int lock_id;
    private String lock_password;

    public int getLock_id() {
        return lock_id;
    }

    public void setLock_id(int lock_id) {
        this.lock_id = lock_id;
    }

    public String getLock_password() {
        return lock_password;
    }

    public void setLock_password(String lock_password) {
        this.lock_password = lock_password;
    }
}
