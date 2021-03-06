package com.qhiehome.ihome.network.model.inquiry.order;

import com.qhiehome.ihome.network.model.base.Response;

import java.io.Serializable;
import java.util.List;

/**
 * Created by YueMa on 2017/7/21.
 */

public class OrderResponse extends Response{

    /**
     * data : {"orderList":[{"id":123456789,"parking":{"id":123456789,"name":"xxxxxx"},"estate":{"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321},"startTime":1499826992574,"endTime":1499826992574,"cancelTime":1499826992574,"createTime":1499826992574,"enterTime":1499826992574,"leaveTime":1499826992574,"paymentTime":1499826992574,"closeTime":1499826992574,"payFee":100,"ownerFee":80,"estateFee":10,"platformFee":10,"state":31}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<OrderListBean> orderList;

        public List<OrderListBean> getOrderList() {
            return orderList;
        }

        public void setOrderList(List<OrderListBean> orderList) {
            this.orderList = orderList;
        }

        public static class OrderListBean implements Serializable{
            /**
             * id : 123456789
             * parking : {"id":123456789,"name":"xxxxxx"}
             * estate : {"id":123456789,"name":"xxxxxx","x":12.345678,"y":87.654321}
             * startTime : 1499826992574
             * endTime : 1499826992574
             * cancelTime : 1499826992574
             * createTime : 1499826992574
             * enterTime : 1499826992574
             * leaveTime : 1499826992574
             * paymentTime : 1499826992574
             * closeTime : 1499826992574
             * payFee : 100
             * ownerFee : 80
             * estateFee : 10
             * platformFee : 10
             * state : 31
             */

            private int id;
            private ParkingBean parking;
            private EstateBean estate;
            private long startTime;
            private long endTime;
            private long cancelTime;
            private long createTime;
            private long enterTime;
            private long leaveTime;
            private long paymentTime;
            private long closeTime;
            private double payFee;
            private double ownerFee;
            private double estateFee;
            private double platformFee;
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

            public long getCancelTime() {
                return cancelTime;
            }

            public void setCancelTime(long cancelTime) {
                this.cancelTime = cancelTime;
            }

            public long getCreateTime() {
                return createTime;
            }

            public void setCreateTime(long createTime) {
                this.createTime = createTime;
            }

            public long getEnterTime() {
                return enterTime;
            }

            public void setEnterTime(long enterTime) {
                this.enterTime = enterTime;
            }

            public long getLeaveTime() {
                return leaveTime;
            }

            public void setLeaveTime(long leaveTime) {
                this.leaveTime = leaveTime;
            }

            public long getPaymentTime() {
                return paymentTime;
            }

            public void setPaymentTime(long paymentTime) {
                this.paymentTime = paymentTime;
            }

            public long getCloseTime() {
                return closeTime;
            }

            public void setCloseTime(long closeTime) {
                this.closeTime = closeTime;
            }

            public double getPayFee() {
                return payFee;
            }

            public void setPayFee(double payFee) {
                this.payFee = payFee;
            }

            public double getOwnerFee() {
                return ownerFee;
            }

            public void setOwnerFee(double ownerFee) {
                this.ownerFee = ownerFee;
            }

            public double getEstateFee() {
                return estateFee;
            }

            public void setEstateFee(double estateFee) {
                this.estateFee = estateFee;
            }

            public double getPlatformFee() {
                return platformFee;
            }

            public void setPlatformFee(double platformFee) {
                this.platformFee = platformFee;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public static class ParkingBean implements Serializable{
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

            public static class EstateBean implements Serializable{
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
