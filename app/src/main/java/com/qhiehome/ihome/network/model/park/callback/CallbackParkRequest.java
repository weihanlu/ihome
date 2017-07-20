package com.qhiehome.ihome.network.model.park.callback;

public class CallbackParkRequest {


    /**
     * data : {"parking_id":123456}
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
         * parking_id : 123456
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
