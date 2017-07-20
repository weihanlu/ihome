package com.qhiehome.ihome.network.model.parkinglist;

/**
 * Created by YueMa on 2017/7/20.
 */

public class ParkingListResponse {

    /**
     * data : {"parking":{"id":123456789,"estate_id":123456789,"owner_id":123456789,"name":"xxxxxx","state":9}}
     * errcode : 0
     * errmsg : success
     */

    private DataBean data;
    private int errcode;
    private String errmsg;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public static class DataBean {
        /**
         * parking : {"id":123456789,"estate_id":123456789,"owner_id":123456789,"name":"xxxxxx","state":9}
         */

        private ParkingBean[] parking;

        public ParkingBean[] getParking() {
            return parking;
        }

        public void setParking(ParkingBean[] parking) {
            this.parking = parking;
        }

        public static class ParkingBean {
            /**
             * id : 123456789
             * estate_id : 123456789
             * owner_id : 123456789
             * name : xxxxxx
             * state : 9
             */

            private int id;
            private int estate_id;
            private int owner_id;
            private String name;
            private int state;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getEstate_id() {
                return estate_id;
            }

            public void setEstate_id(int estate_id) {
                this.estate_id = estate_id;
            }

            public int getOwner_id() {
                return owner_id;
            }

            public void setOwner_id(int owner_id) {
                this.owner_id = owner_id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
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
