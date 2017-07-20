package com.qhiehome.ihome.network.model.charge;

/**
 * Created by YueMa on 2017/7/20.
 */

public class ChargeResponse {

    /**
     * data : {"phone":"xxx...xxx","order":{"id":123456789,"fee":12.34}}
     * errcode : 0
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
        /**
         * phone : xxx...xxx
         * order : {"id":123456789,"fee":12.34}
         */

        private String phone;
        private OrderBean order;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public OrderBean getOrder() {
            return order;
        }

        public void setOrder(OrderBean order) {
            this.order = order;
        }

        public static class OrderBean {
            /**
             * id : 123456789
             * fee : 12.34
             */

            private int id;
            private double fee;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public double getFee() {
                return fee;
            }

            public void setFee(double fee) {
                this.fee = fee;
            }
        }
    }
}
