package com.qhiehome.ihome.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class UserLockBean {

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String lockEstateName;

    @NotNull
    @Unique
    private String parkingName;

    @NotNull
    private int parkingId;

    @NotNull
    private String gatewayId;

    @NotNull
    private String lockMac;

    @NotNull
    private String password;

    @NotNull
    private boolean isRented;

    @Generated(hash = 321619495)
    public UserLockBean(Long id, @NotNull String lockEstateName,
            @NotNull String parkingName, int parkingId, @NotNull String gatewayId,
            @NotNull String lockMac, @NotNull String password, boolean isRented) {
        this.id = id;
        this.lockEstateName = lockEstateName;
        this.parkingName = parkingName;
        this.parkingId = parkingId;
        this.gatewayId = gatewayId;
        this.lockMac = lockMac;
        this.password = password;
        this.isRented = isRented;
    }

    @Generated(hash = 513674444)
    public UserLockBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLockEstateName() {
        return this.lockEstateName;
    }

    public void setLockEstateName(String lockEstateName) {
        this.lockEstateName = lockEstateName;
    }

    public String getParkingName() {
        return this.parkingName;
    }

    public void setParkingName(String parkingName) {
        this.parkingName = parkingName;
    }

    public int getParkingId() {
        return this.parkingId;
    }

    public void setParkingId(int parkingId) {
        this.parkingId = parkingId;
    }

    public String getGatewayId() {
        return this.gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getLockMac() {
        return this.lockMac;
    }

    public void setLockMac(String lockMac) {
        this.lockMac = lockMac;
    }

    public boolean getIsRented() {
        return this.isRented;
    }

    public void setIsRented(boolean isRented) {
        this.isRented = isRented;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserLockBean{" +
                "id=" + id +
                ", lockEstateName='" + lockEstateName + '\'' +
                ", parkingName='" + parkingName + '\'' +
                ", parkingId=" + parkingId +
                ", gatewayId='" + gatewayId + '\'' +
                ", lockMac='" + lockMac + '\'' +
                ", password='" + password + '\'' +
                ", isRented=" + isRented +
                '}';
    }

    public enum LOCK_ROLE {
        CUSTOMER,
        OWNER
    }

    
}
