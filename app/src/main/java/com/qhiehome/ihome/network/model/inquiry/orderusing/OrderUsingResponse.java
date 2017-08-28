package com.qhiehome.ihome.network.model.inquiry.orderusing;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/8/25.
 */

public class OrderUsingResponse extends Response {

    /**
     * data : {"order":{"id":123456789,"state":31,"parking":{"id":123456789,"name":"xxxxxxxx","gateWayId":"xxxxxxxx","lockMac":"xxxxxxxx","password":"xxxxxxxx"},"startTime":1499826000000,"endTime":1499826000000}}
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
         * order : {"id":123456789,"state":31,"parking":{"id":123456789,"name":"xxxxxxxx","gateWayId":"xxxxxxxx","lockMac":"xxxxxxxx","password":"xxxxxxxx"},"startTime":1499826000000,"endTime":1499826000000}
         */

        private OrderBean order;

        public OrderBean getOrder() {
            return order;
        }

        public void setOrder(OrderBean order) {
            this.order = order;
        }

        public static class OrderBean {
            /**
             * id : 123456789
             * state : 31
             * parking : {"id":123456789,"name":"xxxxxxxx","gateWayId":"xxxxxxxx","lockMac":"xxxxxxxx","password":"xxxxxxxx"}
             * startTime : 1499826000000
             * endTime : 1499826000000
             */

            private int id;
            private int state;
            private ParkingBean parking;
            private long startTime;
            private long endTime;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public ParkingBean getParking() {
                return parking;
            }

            public void setParking(ParkingBean parking) {
                this.parking = parking;
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

            public static class ParkingBean {
                /**
                 * id : 123456789
                 * name : xxxxxxxx
                 * gateWayId : xxxxxxxx
                 * lockMac : xxxxxxxx
                 * password : xxxxxxxx
                 */

                private int id;
                private String name;
                private String gateWayId;
                private String lockMac;
                private String password;

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

                public String getGateWayId() {
                    return gateWayId;
                }

                public void setGateWayId(String gateWayId) {
                    this.gateWayId = gateWayId;
                }

                public String getLockMac() {
                    return lockMac;
                }

                public void setLockMac(String lockMac) {
                    this.lockMac = lockMac;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(String password) {
                    this.password = password;
                }
            }
        }
    }
}
