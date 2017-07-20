package com.qhiehome.ihome.network.model.charge;

import com.qhiehome.ihome.network.model.base.Response;

public class ChargeResponse extends Response {

    /**
     * data : {"phone":"xxx...xxx","order":{"id":123456789,"fee":12.34}}
     * errcode : 0
     * errmsg : success
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
