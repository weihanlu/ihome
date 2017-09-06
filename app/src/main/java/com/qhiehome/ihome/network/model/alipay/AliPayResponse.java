package com.qhiehome.ihome.network.model.alipay;

import com.qhiehome.ihome.network.model.base.Response;

public class AliPayResponse extends Response{

    /**
     * data : {"orderInfo":""}
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
         * orderInfo :
         */

        private String orderInfo;

        public String getOrderInfo() {
            return orderInfo;
        }

        public void setOrderInfo(String orderInfo) {
            this.orderInfo = orderInfo;
        }
    }
}
