package com.qhiehome.ihome.network.model.configuration.system;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/9/1.
 */

public class SystemConfigResponse extends Response {


    /**
     * data : {"minReservationInterval":30,"advanceChangeTime":10}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * minReservationInterval : 30
         * advanceChangeTime : 10
         */

        private int minReservationInterval;
        private int advanceChangeTime;

        public int getMinReservationInterval() {
            return minReservationInterval;
        }

        public void setMinReservationInterval(int minReservationInterval) {
            this.minReservationInterval = minReservationInterval;
        }

        public int getAdvanceChangeTime() {
            return advanceChangeTime;
        }

        public void setAdvanceChangeTime(int advanceChangeTime) {
            this.advanceChangeTime = advanceChangeTime;
        }
    }
}
