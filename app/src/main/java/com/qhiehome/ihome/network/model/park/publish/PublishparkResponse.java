package com.qhiehome.ihome.network.model.park.publish;

import com.qhiehome.ihome.network.model.base.Response;

import java.util.List;

public class PublishparkResponse extends Response {


    /**
     * data : {"shareId":[1,2,3]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<Integer> shareId;

        public List<Integer> getShareId() {
            return shareId;
        }

        public void setShareId(List<Integer> shareId) {
            this.shareId = shareId;
        }
    }
}
