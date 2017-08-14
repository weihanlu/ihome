package com.qhiehome.ihome.network.model.base;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Created by YueMa on 2017/7/21.
 */

public class ParkingResponse extends Response implements Serializable{


    /**
     * data : {"estate":[{"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321,"unitPrice":10,"guaranteeFee":10,"parking":[{"id":123456789,"name":"xxxxxx","gatewayId":"xxxxxx","lockMac":"xxxxxx","share":[{"id":123456789,"startTime":1499826000000,"endTime":1499828000000}]}]}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<EstateBean> estate;

        public List<EstateBean> getEstate() {
            return estate;
        }

        public void setEstate(List<EstateBean> estate) {
            this.estate = estate;
        }

        public static class EstateBean implements Serializable{
            /**
             * id : 123456789
             * name : xxxxxx
             * x : 12.345678
             * y : 87.654321
             * unitPrice : 10
             * guaranteeFee : 10
             * parking : [{"id":123456789,"name":"xxxxxx","gatewayId":"xxxxxx","lockMac":"xxxxxx","share":[{"id":123456789,"startTime":1499826000000,"endTime":1499828000000}]}]
             */

            private int id;
            private String name;
            private double x;
            private double y;
            private int unitPrice;
            private int guaranteeFee;
            private List<ParkingBean> parking;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

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

            public int getUnitPrice() {
                return unitPrice;
            }

            public void setUnitPrice(int unitPrice) {
                this.unitPrice = unitPrice;
            }

            public int getGuaranteeFee() {
                return guaranteeFee;
            }

            public void setGuaranteeFee(int guaranteeFee) {
                this.guaranteeFee = guaranteeFee;
            }

            public List<ParkingBean> getParking() {
                return parking;
            }

            public void setParking(List<ParkingBean> parking) {
                this.parking = parking;
            }

            public static class ParkingBean implements Serializable{
                /**
                 * id : 123456789
                 * name : xxxxxx
                 * gatewayId : xxxxxx
                 * lockMac : xxxxxx
                 * share : [{"id":123456789,"startTime":1499826000000,"endTime":1499828000000}]
                 */

                private int id;
                private String name;
                private String gatewayId;
                private String lockMac;
                private List<ShareBean> share;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

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

                public List<ShareBean> getShare() {
                    return share;
                }

                public void setShare(List<ShareBean> share) {
                    this.share = share;
                }

                public static class ShareBean implements Serializable, Comparable<ShareBean>{
                    /**
                     * id : 123456789
                     * startTime : 1499826000000
                     * endTime : 1499828000000
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

                    @Override
                    public int compareTo(@NonNull ShareBean o) {
                        long thisStartTime = this.getStartTime();
                        long otherStartTime = o.getStartTime();
                        if (thisStartTime < otherStartTime) {
                            return -1;
                        } else if (thisStartTime > otherStartTime) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
        }
    }
}
