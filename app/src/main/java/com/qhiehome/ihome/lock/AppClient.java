package com.qhiehome.ihome.lock;

public abstract class AppClient {

    public abstract void connect();

    public abstract void disconnect();

    public abstract void raiseLock();

    public abstract void downLock();

}

