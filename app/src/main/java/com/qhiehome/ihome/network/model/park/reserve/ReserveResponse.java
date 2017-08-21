package com.qhiehome.ihome.network.model.park.reserve;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveResponse extends Response {

    /**
     * data : {"order":{"id":26}}
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
         * order : {"id":26}
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
             * id : 26
             */

            private int id;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }
        }
    }
}
