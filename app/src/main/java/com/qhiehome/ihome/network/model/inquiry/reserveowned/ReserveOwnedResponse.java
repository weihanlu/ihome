package com.qhiehome.ihome.network.model.inquiry.reserveowned;

import com.qhiehome.ihome.network.model.base.Response;

import java.util.List;

public class ReserveOwnedResponse extends Response {

    /**
     * data : {"reservation":[{"parking":{"id":123456789,"name":"xxxxxx"},"estate":{"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321},"startTime":1499826000000,"endTime":1499828000000,"cancelTime":1499827000000,"state":31}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<ReservationBean> reservation;

        public List<ReservationBean> getReservation() {
            return reservation;
        }

        public void setReservation(List<ReservationBean> reservation) {
            this.reservation = reservation;
        }

        public static class ReservationBean {
            /**
             * parking : {"id":123456789,"name":"xxxxxx"}
             * estate : {"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321}
             * startTime : 1499826000000
             * endTime : 1499828000000
             * cancelTime : 1499827000000
             * state : 31
             */

            private ParkingBean parking;
            private EstateBean estate;
            private long startTime;
            private long endTime;
            private long cancelTime;
            private int state;

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

            public long getCancelTime() {
                return cancelTime;
            }

            public void setCancelTime(long cancelTime) {
                this.cancelTime = cancelTime;
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
