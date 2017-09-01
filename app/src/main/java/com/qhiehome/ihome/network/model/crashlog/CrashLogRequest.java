package com.qhiehome.ihome.network.model.crashlog;

public class CrashLogRequest {

    /**
     * crashlog : xxxxxxxxx
     */

    private String crashlog;

    public CrashLogRequest(String crashlog) {
        this.crashlog = crashlog;
    }

    public String getCrashlog() {
        return crashlog;
    }

    public void setCrashlog(String crashlog) {
        this.crashlog = crashlog;
    }
}
