package com.qhiehome.ihome.network.model.inquiry.order;

import java.util.List;

/**
 * Created by YueMa on 2017/7/21.
 */

public class OrderResponse {

    /**
     * data : {"order":[{"id":123456789,"parking_id":123456789,"phone":"xxxx...xxxx","enter_time":1499826992574,"leave_time":1499826992574,"payment_time":1499826992574,"close_time":1499826992574,"pay_fee":100,"owner_fee":80,"estate_fee":10,"platform_fee":10,"state":2}]}
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
             * parking_id : 123456789
             * phone : xxxx...xxxx
             * enter_time : 1499826992574
             * leave_time : 1499826992574
             * payment_time : 1499826992574
             * close_time : 1499826992574
             * pay_fee : 100
             * owner_fee : 80
             * estate_fee : 10
             * platform_fee : 10
             * state : 2
             */

            private int id;
            private int parking_id;
            private String phone;
            private long enter_time;
            private long leave_time;
            private long payment_time;
            private long close_time;
            private int pay_fee;
            private int owner_fee;
            private int estate_fee;
            private int platform_fee;
            private int state;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getParking_id() {
                return parking_id;
            }

            public void setParking_id(int parking_id) {
                this.parking_id = parking_id;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public long getEnter_time() {
                return enter_time;
            }

            public void setEnter_time(long enter_time) {
                this.enter_time = enter_time;
            }

            public long getLeave_time() {
                return leave_time;
            }

            public void setLeave_time(long leave_time) {
                this.leave_time = leave_time;
            }

            public long getPayment_time() {
                return payment_time;
            }

            public void setPayment_time(long payment_time) {
                this.payment_time = payment_time;
            }

            public long getClose_time() {
                return close_time;
            }

            public void setClose_time(long close_time) {
                this.close_time = close_time;
            }

            public int getPay_fee() {
                return pay_fee;
            }

            public void setPay_fee(int pay_fee) {
                this.pay_fee = pay_fee;
            }

            public int getOwner_fee() {
                return owner_fee;
            }

            public void setOwner_fee(int owner_fee) {
                this.owner_fee = owner_fee;
            }

            public int getEstate_fee() {
                return estate_fee;
            }

            public void setEstate_fee(int estate_fee) {
                this.estate_fee = estate_fee;
            }

            public int getPlatform_fee() {
                return platform_fee;
            }

            public void setPlatform_fee(int platform_fee) {
                this.platform_fee = platform_fee;
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
