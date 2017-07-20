package com.qhiehome.ihome.network.model.estatelist;

import com.qhiehome.ihome.network.model.base.Response;

public class EstateListResponse extends Response{

    /**
     * data : {"estate":{"id":123456789,"city":"北京","district":"朝阳","name":"东山墅","alias_name":"value","addresss":"东四环北路7号","x":116.499428,"y":39.956695,"introduction":"别墅","owner_settle":80,"estate_settle":10,"platform_settle":10,"state":9}}
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
         * estate : {"id":123456789,"city":"北京","district":"朝阳","name":"东山墅","alias_name":"value","addresss":"东四环北路7号","x":116.499428,"y":39.956695,"introduction":"别墅","owner_settle":80,"estate_settle":10,"platform_settle":10,"state":9}
         */

        private EstateBean[] estate;

            public EstateBean[] getEstate() {
                return estate;
            }

        public void setEstate(EstateBean[] estate) {
            this.estate = estate;
        }

        public static class EstateBean {
            /**
             * id : 123456789
             * city : 北京
             * district : 朝阳
             * name : 东山墅
             * alias_name : value
             * addresss : 东四环北路7号
             * x : 116.499428
             * y : 39.956695
             * introduction : 别墅
             * owner_settle : 80
             * estate_settle : 10
             * platform_settle : 10
             * state : 9
             */

            private int id;
            private String city;
            private String district;
            private String name;
            private String alias_name;
            private String addresss;
            private double x;
            private double y;
            private String introduction;
            private int owner_settle;
            private int estate_settle;
            private int platform_settle;
            private int state;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getDistrict() {
                return district;
            }

            public void setDistrict(String district) {
                this.district = district;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getAlias_name() {
                return alias_name;
            }

            public void setAlias_name(String alias_name) {
                this.alias_name = alias_name;
            }

            public String getAddresss() {
                return addresss;
            }

            public void setAddresss(String addresss) {
                this.addresss = addresss;
            }

            public double getX() {
                return x;
            }

            public void setX(double x) {
                this.x = x;
            }

            public double getY() {
                return y;
            }

            public void setY(double y) {
                this.y = y;
            }

            public String getIntroduction() {
                return introduction;
            }

            public void setIntroduction(String introduction) {
                this.introduction = introduction;
            }

            public int getOwner_settle() {
                return owner_settle;
            }

            public void setOwner_settle(int owner_settle) {
                this.owner_settle = owner_settle;
            }

            public int getEstate_settle() {
                return estate_settle;
            }

            public void setEstate_settle(int estate_settle) {
                this.estate_settle = estate_settle;
            }

            public int getPlatform_settle() {
                return platform_settle;
            }

            public void setPlatform_settle(int platform_settle) {
                this.platform_settle = platform_settle;
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
