package com.qhiehome.ihome.network.model.park.publish;

public class PublishparkRequest {


    /**
     * data : {"parking_id":123456789}
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
         * parking_id : 123456789
         */

        private int[] parking_id;

        public int[] getParking_id() {
            return parking_id;
        }

        public void setParking_id(int[] parking_id) {
            this.parking_id = parking_id;
        }
    }
}
