package com.qhiehome.ihome.bean;

public class LockBean {

    private String address;
    private String realName;
    private String name;
    private String password;
    private String password_customer;
    private int role;
    private int isDefault;
    private int curState;
    private int autopark;

    public LockBean(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public LockBean(String address,String name,  int role, int isDefault, String realName){
        this.address = address;
        this.realName = realName;
        this.name = name;
        this.role = role;
        this.isDefault = isDefault;
        this.curState = LOCK_STATE.UP.ordinal();//set UP as a default state
        this.autopark = AUTO_PACK.DISABLE.ordinal();
        this.password = "";
        this.password_customer = "";
    }

    private enum LOCK_STATE{
        UP,          //UP means locked
        UPING,
        DOWN,        //DOWN means unlocked
        DOWNING
    };

    public enum LOCK_ROLE{
        CUSTOMER,
        OWNER
    };

    public enum LOCK_DEFAULT{
        DEFAULT,
        NOT_DEFAULT,
    };

    private enum AUTO_PACK{
        DISABLE,
        ENABLE,
    };


    public String getAddress() {
        return address;
    }
    public void setTime(String address) {
        this.address = address;
    }

    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }

    public String getCustomerPassword(){
        return password_customer;
    }
    public void setCustomerPassword(String password){
        this.password_customer = password;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getRealName(){
        return realName;
    }
    public void setRealName(String realName){
        this.realName = name;
    }

    public int getRole() {
        return role;
    }
    public void setRole(int role) {
        this.role = role;
    }

    public int isDefault() {
        return isDefault;
    }
    public void setDefault(int isdefault) {
        this.isDefault = isdefault;
    }

    public int getCurState() {
        return curState;
    }
    public void setCurState(int curState) {
        this.curState = curState;
    }

    public int getAutoPark() {
        return autopark;
    }
    public void setAutoPark(int autopark) {
        this.autopark = autopark;
    }

    public String toString(){
        return address + ", " + name;
    }

}
