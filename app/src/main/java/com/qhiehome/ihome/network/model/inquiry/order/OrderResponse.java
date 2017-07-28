package com.qhiehome.ihome.network.model.inquiry.order;

import com.qhiehome.ihome.network.model.base.Response;

import java.util.List;

/**
 * Created by YueMa on 2017/7/21.
 */

public class OrderResponse extends Response{

    /**
     * data : {"order":[{"id":123456789,"parkingId":123456789,"phone":"xxxx...xxxx","enterTime":1499826992574,"leaveTime":1499826992574,"paymentTime":1499826992574,"closeTime":1499826992574,"payFee":100,"ownerFee":80,"estateFee":10,"platformFee":10,"state":2}]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<OrderBean> order;

        public List<OrderBean> getOrder() {
            return order;
        }

        public void setOrder(List<OrderBean> order) {
            this.order = order;
        }

        public static class OrderBean {
            /**
             * id : 123456789
             * parkingId : 123456789
             * phone : xxxx...xxxx
             * enterTime : 1499826992574
             * leaveTime : 1499826992574
             * paymentTime : 1499826992574
             * closeTime : 1499826992574
             * payFee : 100
             * ownerFee : 80
             * estateFee : 10
             * platformFee : 10
             * state : 2
             */

            private int id;
            private int parkingId;
            private String phone;
            private long enterTime;
            private long leaveTime;
            private long paymentTime;
            private long closeTime;
            private int payFee;
            private int ownerFee;
            private int estateFee;
            private int platformFee;
            private int state;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getParkingId() {
                return parkingId;
            }

            public void setParkingId(int parkingId) {
                this.parkingId = parkingId;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
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

            public int getPayFee() {
                return payFee;
            }

            public void setPayFee(int payFee) {
                this.payFee = payFee;
            }

            public int getOwnerFee() {
                return ownerFee;
            }

            public void setOwnerFee(int ownerFee) {
                this.ownerFee = ownerFee;
            }

            public int getEstateFee() {
                return estateFee;
            }

            public void setEstateFee(int estateFee) {
                this.estateFee = estateFee;
            }

            public int getPlatformFee() {
                return platformFee;
            }

            public void setPlatformFee(int platformFee) {
                this.platformFee = platformFee;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }
        }
    }
}
