package com.qhiehome.ihome.network.model.inquiry.parkingempty;

/**
 * Created by YueMa on 2017/7/21.
 */

public class ParkingEmptyRequest {

    /**
     * x : 116.499428
     * y : 39.956695
     * radius : 1000
     */

    private double x;
    private double y;
    private int radius;

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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
