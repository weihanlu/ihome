package com.qhiehome.ihome.network.model.base;

import java.util.List;

/**
 * Created by YueMa on 2017/7/21.
 */

public class ParkingResponse {

    /**
     * data : {"estate":[{"name":"xxxxxx","x":12.345678,"y":87.654321,"parking":[{"id":"xxxx...xxxx","name":"xxxxxx","share":[{"id":123456789,"start_time":1499826000000,"end_time":1499828000000}]}]}]}
     * errcode : 1
     * errmsg : success
     */

    private DataBean data;
    private int errcode;
    private String errmsg;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public static class DataBean {
        private List<EstateBean> estate;

        public List<EstateBean> getEstate() {
            return estate;
        }

        public void setEstate(List<EstateBean> estate) {
            this.estate = estate;
        }

        public static class EstateBean {
            /**
             * name : xxxxxx
             * x : 12.345678
             * y : 87.654321
             * parking : [{"id":"xxxx...xxxx","name":"xxxxxx","share":[{"id":123456789,"start_time":1499826000000,"end_time":1499828000000}]}]
             */

            private String name;
            private double x;
            private double y;
            private List<ParkingBean> parking;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public double getX() {
                return x;
            }

            public void setX(double x) {
                this.x = x;
            }

            public double getY() {
                return y;
            }

            public void setY(double y) {
                this.y = y;
            }

            public List<ParkingBean> getParking() {
                return parking;
            }

            public void setParking(List<ParkingBean> parking) {
                this.parking = parking;
            }

            public static class ParkingBean {
                /**
                 * id : xxxx...xxxx
                 * name : xxxxxx
                 * share : [{"id":123456789,"start_time":1499826000000,"end_time":1499828000000}]
                 */

                private String id;
                private String name;
                private String gatewayId;
                private String lockMac;

                public String getGatewayId() {
                    return gatewayId;
                }

                public void setGatewayId(String gatewayId) {
                    this.gatewayId = gatewayId;
                }

                public String getLockMac() {
                    return lockMac;
                }

                public void setLockMac(String lockMac) {
                    this.lockMac = lockMac;
                }

                private List<ShareBean> share;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public List<ShareBean> getShare() {
                    return share;
                }

                public void setShare(List<ShareBean> share) {
                    this.share = share;
                }

                public static class ShareBean {
                    /**
                     * id : 123456789
                     * start_time : 1499826000000
                     * end_time : 1499828000000
                     */

                    private int id;
                    private long startTime;
                    private long endTime;

                    public int getId() {
                        return id;
                    }

                    public void setId(int id) {
                        this.id = id;
                    }

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
        }
    }
}
