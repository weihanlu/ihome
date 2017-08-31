package com.qhiehome.ihome.network.model.crashlog;

public class CrashLogRequest {


    /**
     * advice : advice
     */

    private String crashLog;

    public CrashLogRequest(String crashLog) {
        this.crashLog = crashLog;
    }

    public String getCrashLog() {
        return crashLog;
    }

    public void setCrashLog(String crashLog) {
        this.crashLog = crashLog;
    }
}
