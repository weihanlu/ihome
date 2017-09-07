package com.qhiehome.ihome.network.model.pay.accountbalance;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/8/24.
 */

public class AccountBalanceResponse extends Response {

    /**
     * data : {"account":30}
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
         * account : 30.0
         */

        private double account;

        private String orderInfo;

        public String getOrderInfo() {
            return orderInfo;
        }

        public void setOrderInfo(String orderInfo) {
            this.orderInfo = orderInfo;
        }

        public double getAccount() {
            return account;
        }

        public void setAccount(double account) {
            this.account = account;
        }
    }
}
