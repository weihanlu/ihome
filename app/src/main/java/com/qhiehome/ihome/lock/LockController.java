package com.qhiehome.ihome.lock;

 public interface LockController {

     String BROADCAST_CONNECT = "com.qhiehome.ihome.lock.broad.CONNECT";

     void connect();
     void disconnect();
     void raiseLock();
     void downLock();
}

