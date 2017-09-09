package com.qhiehome.ihome.network.model.estate.map;

/**
 * Created by YueMa on 2017/9/8.
 */

public class DownloadEstateMapRequest {

    /**
     * estateId : 123456789
     */

    private int estateId;

    public DownloadEstateMapRequest(int estateId) {
        this.estateId = estateId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }
}
