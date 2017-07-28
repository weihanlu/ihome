package com.qhiehome.ihome.network.model.park.reserve;

import com.qhiehome.ihome.network.model.base.Response;

/**
 * Created by YueMa on 2017/7/28.
 */

public class ReserveResponse extends Response {

    /**
     * data : {"estate":{"name":"BUPT","x":116.364695,"y":39.967366,"singleParking":{"id":1,"name":"CrAM_CAB827","gatewayId":"3f5f016186619056","lockMac":"00158D0000CAB827","password":"f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2","singleShare":{"id":1,"startTime":1600000000001,"endTime":1600000000002}}}}
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
         * estate : {"name":"BUPT","x":116.364695,"y":39.967366,"singleParking":{"id":1,"name":"CrAM_CAB827","gatewayId":"3f5f016186619056","lockMac":"00158D0000CAB827","password":"f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2","singleShare":{"id":1,"startTime":1600000000001,"endTime":1600000000002}}}
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
             * name : BUPT
             * x : 116.364695
             * y : 39.967366
             * singleParking : {"id":1,"name":"CrAM_CAB827","gatewayId":"3f5f016186619056","lockMac":"00158D0000CAB827","password":"f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2","singleShare":{"id":1,"startTime":1600000000001,"endTime":1600000000002}}
             */

            private String name;
            private double x;
            private double y;
            private SingleParkingBean singleParking;

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

            public SingleParkingBean getSingleParking() {
                return singleParking;
            }

            public void setSingleParking(SingleParkingBean singleParking) {
                this.singleParking = singleParking;
            }

            public static class SingleParkingBean {
                /**
                 * id : 1
                 * name : CrAM_CAB827
                 * gatewayId : 3f5f016186619056
                 * lockMac : 00158D0000CAB827
                 * password : f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2
                 * singleShare : {"id":1,"startTime":1600000000001,"endTime":1600000000002}
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
                     * id : 1
                     * startTime : 1600000000001
                     * endTime : 1600000000002
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
