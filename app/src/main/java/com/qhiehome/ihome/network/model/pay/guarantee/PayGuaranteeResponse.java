package com.qhiehome.ihome.network.model.pay.guarantee;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/8/21.
 */

public class PayGuaranteeResponse extends Response {

    /**
     * data : {"estate":{"id":1,"name":"北京邮电大学科研楼停车场","x":116.364695,"y":39.967366,"unitPrice":10,"guaranteeFee":10,"singleParking":{"id":1,"name":"CrAM_095D28","gatewayId":"c8dbf72c4e241fb0","lockMac":"00158D0001095D28","password":"123456","singleShare":{"id":271,"startTime":1503300600000,"endTime":1503302400000}}}}
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
         * estate : {"id":1,"name":"北京邮电大学科研楼停车场","x":116.364695,"y":39.967366,"unitPrice":10,"guaranteeFee":10,"singleParking":{"id":1,"name":"CrAM_095D28","gatewayId":"c8dbf72c4e241fb0","lockMac":"00158D0001095D28","password":"123456","singleShare":{"id":271,"startTime":1503300600000,"endTime":1503302400000}}}
         */

        private EstateBean estate;

        public EstateBean getEstate() {
            return estate;
        }

        public void setEstate(EstateBean estate) {
            this.estate = estate;
        }

        public static class EstateBean {
            /**
             * id : 1
             * name : 北京邮电大学科研楼停车场
             * x : 116.364695
             * y : 39.967366
             * unitPrice : 10.0
             * guaranteeFee : 10.0
             * singleParking : {"id":1,"name":"CrAM_095D28","gatewayId":"c8dbf72c4e241fb0","lockMac":"00158D0001095D28","password":"123456","singleShare":{"id":271,"startTime":1503300600000,"endTime":1503302400000}}
             */

            private int id;
            private String name;
            private double x;
            private double y;
            private double unitPrice;
            private double guaranteeFee;
            private SingleParkingBean singleParking;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
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

            public double getUnitPrice() {
                return unitPrice;
            }

            public void setUnitPrice(double unitPrice) {
                this.unitPrice = unitPrice;
            }

            public double getGuaranteeFee() {
                return guaranteeFee;
            }

            public void setGuaranteeFee(double guaranteeFee) {
                this.guaranteeFee = guaranteeFee;
            }

            public SingleParkingBean getSingleParking() {
                return singleParking;
            }

            public void setSingleParking(SingleParkingBean singleParking) {
                this.singleParking = singleParking;
            }

            public static class SingleParkingBean {
                /**
                 * id : 1
                 * name : CrAM_095D28
                 * gatewayId : c8dbf72c4e241fb0
                 * lockMac : 00158D0001095D28
                 * password : 123456
                 * singleShare : {"id":271,"startTime":1503300600000,"endTime":1503302400000}
                 */

                private int id;
                private String name;
                private String gatewayId;
                private String lockMac;
                private String password;
                private SingleShareBean singleShare;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getGatewayId() {
                    return gatewayId;
                }

                public void setGatewayId(String gatewayId) {
                    this.gatewayId = gatewayId;
                }

                public String getLockMac() {
                    return lockMac;
                }

                public void setLockMac(String lockMac) {
                    this.lockMac = lockMac;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(String password) {
                    this.password = password;
                }

                public SingleShareBean getSingleShare() {
                    return singleShare;
                }

                public void setSingleShare(SingleShareBean singleShare) {
                    this.singleShare = singleShare;
                }

                public static class SingleShareBean {
                    /**
                     * id : 271
                     * startTime : 1503300600000
                     * endTime : 1503302400000
                     */

                    private int id;
                    private long startTime;
                    private long endTime;

                    public int getId() {
                        return id;
                    }

                    public void setId(int id) {
                        this.id = id;
                    }

                    public long getStartTime() {
                        return startTime;
                    }

                    public void setStartTime(long startTime) {
                        this.startTime = startTime;
                    }

                    public long getEndTime() {
                        return endTime;
                    }

                    public void setEndTime(long endTime) {
                        this.endTime = endTime;
                    }
                }
            }
        }
    }
}
