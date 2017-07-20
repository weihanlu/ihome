package com.qhiehome.ihome.network.model.park.list;

import com.qhiehome.ihome.network.model.base.Response;

public class ParkingListResponse extends Response{

    /**
     * data : {"parking":{"id":123456789,"estate_id":123456789,"owner_id":123456789,"name":"xxxxxx","state":9}}
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
