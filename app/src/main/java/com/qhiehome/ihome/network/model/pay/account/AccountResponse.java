package com.qhiehome.ihome.network.model.pay.account;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/9/19.
 */

public class AccountResponse extends Response {

    /**
     * data : {"account":10,"chargeOrderId":7,"orderInfo":"xxxxxx","appId":"wx42a91e33c4b3a97b","partnerId":"1489065172","prepayId":"wx2017091916200575f9c6e0db0145249875","packageValue":"Sign=WXPay","nonceStr":"LCs2PmM89bn8yYj6k4h9titnt5hgteoS","timeStamp":"1505809205","sign":"B73A476FB3054825F20BD538B6FD6022"}
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
         * account : 10.0
         * chargeOrderId : 7
         * orderInfo : xxxxxx
         * appId : wx42a91e33c4b3a97b
         * partnerId : 1489065172
         * prepayId : wx2017091916200575f9c6e0db0145249875
         * packageValue : Sign=WXPay
         * nonceStr : LCs2PmM89bn8yYj6k4h9titnt5hgteoS
         * timeStamp : 1505809205
         * sign : B73A476FB3054825F20BD538B6FD6022
         */

        private double account;
        private int chargeOrderId;
        private String orderInfo;
        private String appId;
        private String partnerId;
        private String prepayId;
        private String packageValue;
        private String nonceStr;
        private String timeStamp;
        private String sign;

        public double getAccount() {
            return account;
        }

        public void setAccount(double account) {
            this.account = account;
        }

        public int getChargeOrderId() {
            return chargeOrderId;
        }

        public void setChargeOrderId(int chargeOrderId) {
            this.chargeOrderId = chargeOrderId;
        }

        public String getOrderInfo() {
            return orderInfo;
        }

        public void setOrderInfo(String orderInfo) {
            this.orderInfo = orderInfo;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }

        public String getPrepayId() {
            return prepayId;
        }

        public void setPrepayId(String prepayId) {
            this.prepayId = prepayId;
        }

        public String getPackageValue() {
            return packageValue;
        }

        public void setPackageValue(String packageValue) {
            this.packageValue = packageValue;
        }

        public String getNonceStr() {
            return nonceStr;
        }

        public void setNonceStr(String nonceStr) {
            this.nonceStr = nonceStr;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }
    }
}
