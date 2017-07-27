package com.qhiehome.ihome.network.model.park.publish;

import java.util.List;

public class PublishparkRequest {

    /**
     * parking_id : 123456789
     * password : xxxx...xxxx
     * "share":[
     *     {
     *         "start_time": 1499826000000,
     *         "end_time": 1499828000000
     *     }
     * ]
     */

    private long parkingId;
    private String password;
    private List<ShareBean> share;

    public long getParkingId() {
        return parkingId;
    }

    public void setParkingId(long parkingId) {
        this.parkingId = parkingId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ShareBean> getShare() {
        return share;
    }

    public void setShare(List<ShareBean> share) {
        this.share = share;
    }

    public static class ShareBean {
        /**
         * start_time : 1499826000000
         * end_time : 1499828000000
         */

        private long startTime;
        private long endTime;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
    }
}
