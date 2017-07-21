package com.qhiehome.ihome.network.model.park.charge;

import com.qhiehome.ihome.network.model.base.Response;

public class ChargeResponse extends Response {

    /**
     * data : {"order":{"id":123456789,"fee":12.34}}
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
         * order : {"id":123456789,"fee":12.34}
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
