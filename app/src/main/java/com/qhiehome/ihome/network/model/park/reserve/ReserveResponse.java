package com.qhiehome.ihome.network.model.park.reserve;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveResponse extends Response {


    /**
     * data : {"order":{"id":123456789,"parking":{"id":123456789,"name":"xxxxxx"},"estate":{"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321},"startTime":1499826992574,"endTime":1499826992574,"state":31}}
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
         * order : {"id":123456789,"parking":{"id":123456789,"name":"xxxxxx"},"estate":{"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321},"startTime":1499826992574,"endTime":1499826992574,"state":31}
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
             * parking : {"id":123456789,"name":"xxxxxx"}
             * estate : {"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321}
             * startTime : 1499826992574
             * endTime : 1499826992574
             * state : 31
             */

            private int id;
            private ParkingBean parking;
            private EstateBean estate;
            private long startTime;
            private long endTime;
            private int state;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public ParkingBean getParking() {
                return parking;
            }

            public void setParking(ParkingBean parking) {
                this.parking = parking;
            }

            public EstateBean getEstate() {
                return estate;
            }

            public void setEstate(EstateBean estate) {
                this.estate = estate;
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

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public static class ParkingBean {
                /**
                 * id : 123456789
                 * name : xxxxxx
                 */

                private int id;
                private String name;

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
            }

            public static class EstateBean {
                /**
                 * id : 123456789
                 * name : xxxxxx
                 * x : 12.345678
                 * y : 87.654321
                 */

                private int id;
                private String name;
                private double x;
                private double y;

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
            }
        }
    }
}
