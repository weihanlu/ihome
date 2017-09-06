package com.qhiehome.ihome.bean;

public class ParkingItem {

    private String parkingId;
    private int estateId;

    public ParkingItem(String parkingId, int estateId) {
        this.parkingId = parkingId;
        this.estateId = estateId;
    }

    public String getParkingId() {
        return parkingId;
    }

    public void setParkingId(String parkingId) {
        this.parkingId = parkingId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

}
