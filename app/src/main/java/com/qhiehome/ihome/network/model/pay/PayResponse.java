package com.qhiehome.ihome.network.model.pay;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/9/18.
 */

public class PayResponse extends Response {

    /**
     * data : {"payFee":0.01,"orderInfo":"xxxxxx","appId":"wx42a91e33c4b3a97b","partnerId":"1489065172","prepayId":"wx20170918103147d562b1dcd70312578552","packageValue":"Sign=WXPay","nonceStr":"zJIu5979g3d4PPCt","timeStamp":"1505701908","sign":"57307E449A4351C93E7950E2DCEB3FCA"}
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
         * payFee : 0.01
         * orderInfo : xxxxxx
         * appId : wx42a91e33c4b3a97b
         * partnerId : 1489065172
         * prepayId : wx20170918103147d562b1dcd70312578552
         * packageValue : Sign=WXPay
         * nonceStr : zJIu5979g3d4PPCt
         * timeStamp : 1505701908
         * sign : 57307E449A4351C93E7950E2DCEB3FCA
         */

        private double payFee;
        private String orderInfo;
        private String appId;
        private String partnerId;
        private String prepayId;
        private String packageValue;
        private String nonceStr;
        private String timeStamp;
        private String sign;

        public double getPayFee() {
            return payFee;
        }

        public void setPayFee(double payFee) {
            this.payFee = payFee;
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
