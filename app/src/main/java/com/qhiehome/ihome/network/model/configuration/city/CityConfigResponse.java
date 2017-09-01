package com.qhiehome.ihome.network.model.configuration.city;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/9/1.
 */

public class CityConfigResponse extends Response {

    /**
     * data : {"minSharingPeriod":30,"minChargingPeriod":10,"freeCancellationTime":0}
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
         * minSharingPeriod : 30
         * minChargingPeriod : 10
         * freeCancellationTime : 0
         */

        private int minSharingPeriod;
        private int minChargingPeriod;
        private int freeCancellationTime;

        public int getMinSharingPeriod() {
            return minSharingPeriod;
        }

        public void setMinSharingPeriod(int minSharingPeriod) {
            this.minSharingPeriod = minSharingPeriod;
        }

        public int getMinChargingPeriod() {
            return minChargingPeriod;
        }

        public void setMinChargingPeriod(int minChargingPeriod) {
            this.minChargingPeriod = minChargingPeriod;
        }

        public int getFreeCancellationTime() {
            return freeCancellationTime;
        }

        public void setFreeCancellationTime(int freeCancellationTime) {
            this.freeCancellationTime = freeCancellationTime;
        }
    }
}
