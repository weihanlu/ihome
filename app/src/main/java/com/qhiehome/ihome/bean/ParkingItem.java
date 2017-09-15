package com.qhiehome.ihome.bean;

public class ParkingItem {

    private String parkingId;
    private int estateId;
    private String password;

    public ParkingItem(String parkingId, int estateId, String password) {
        this.parkingId = parkingId;
        this.estateId = estateId;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
