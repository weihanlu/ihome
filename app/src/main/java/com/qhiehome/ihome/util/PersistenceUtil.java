package com.qhiehome.ihome.util;

import android.content.Context;

import com.qhiehome.ihome.bean.persistence.OrderInfoBean;
import com.qhiehome.ihome.bean.persistence.UserBean;

public class PersistenceUtil {

    private static final String CLEAR_STRING = "";
    private static final int CLEAR_NON_STRING = -1;

    public static void setUserInfo(Context context, UserBean userBean){
        String phoneNum = userBean.getPhoneNum();

        SharedPreferenceUtil.setString(context, Constant.PHONE_KEY, phoneNum);
    }

    public static UserBean getUserInfo(Context context) {
        String phoneNum = SharedPreferenceUtil.getString(context, Constant.PHONE_KEY, CLEAR_STRING);

        return new UserBean(phoneNum);
    }

    public static void clearUserInfo(Context context) {
        SharedPreferenceUtil.setString(context, Constant.PHONE_KEY, CLEAR_STRING);
    }

    public static void setOrderInfo(Context context, OrderInfoBean orderInfoBean) {
        int orderId = orderInfoBean.getOrderId();
        int orderState = orderInfoBean.getOrderState();
        long startTime = orderInfoBean.getStartTime();
        long endTime = orderInfoBean.getEndTime();
        String lockName = orderInfoBean.getLockName();
        String lockMac = orderInfoBean.getLockMac();
        String lockPwd = orderInfoBean.getLockPwd();
        String gateWayId = orderInfoBean.getGateWayId();
        String estateName = orderInfoBean.getEstateName();
        float estateLongitude = orderInfoBean.getEstateLongitude();
        float estateLatitude = orderInfoBean.getEstateLatitude();

        SharedPreferenceUtil.setInt(context, Constant.ORDER_ID, orderId);
        SharedPreferenceUtil.setInt(context, Constant.ORDER_STATE, orderState);
        SharedPreferenceUtil.setLong(context, Constant.PARKING_START_TIME, startTime);
        SharedPreferenceUtil.setLong(context, Constant.PARKING_END_TIME, endTime);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_NAME, lockName);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_MAC, lockMac);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_PWD, lockPwd);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_GATEWAY_ID, gateWayId);
        SharedPreferenceUtil.setString(context, Constant.ESTATE_NAME, estateName);
        SharedPreferenceUtil.setFloat(context, Constant.ESTATE_LONGITUDE, estateLongitude);
        SharedPreferenceUtil.setFloat(context, Constant.ESTATE_LATITUDE, estateLatitude);
    }

    public static OrderInfoBean getOrderInfo(Context context) {
        int orderId = SharedPreferenceUtil.getInt(context, Constant.ORDER_ID, CLEAR_NON_STRING);
        int orderState = SharedPreferenceUtil.getInt(context, Constant.ORDER_STATE, CLEAR_NON_STRING);
        long startTime = SharedPreferenceUtil.getLong(context, Constant.PARKING_START_TIME, CLEAR_NON_STRING);
        long endTime = SharedPreferenceUtil.getLong(context, Constant.PARKING_END_TIME, CLEAR_NON_STRING);
        String lockName = SharedPreferenceUtil.getString(context, Constant.RESERVE_LOCK_NAME, CLEAR_STRING);
        String lockMac = SharedPreferenceUtil.getString(context, Constant.RESERVE_LOCK_MAC, CLEAR_STRING);
        String lockPwd = SharedPreferenceUtil.getString(context, Constant.RESERVE_LOCK_PWD, CLEAR_STRING);
        String gateWayId = SharedPreferenceUtil.getString(context, Constant.RESERVE_GATEWAY_ID, CLEAR_STRING);
        String estateName = SharedPreferenceUtil.getString(context, Constant.ESTATE_NAME, CLEAR_STRING);
        float estateLongitude = SharedPreferenceUtil.getFloat(context, Constant.ESTATE_LONGITUDE, CLEAR_NON_STRING);
        float estateLatitude = SharedPreferenceUtil.getFloat(context, Constant.ESTATE_LATITUDE, CLEAR_NON_STRING);

        return new OrderInfoBean(orderId, orderState, startTime, endTime, lockName, lockMac, lockPwd,
                gateWayId, estateName, estateLongitude, estateLatitude);
    }

    public static void clearOrderInfo(Context context) {
        SharedPreferenceUtil.setInt(context, Constant.ORDER_ID, CLEAR_NON_STRING);
        SharedPreferenceUtil.setInt(context, Constant.ORDER_STATE, CLEAR_NON_STRING);
        SharedPreferenceUtil.setLong(context, Constant.PARKING_START_TIME, CLEAR_NON_STRING);
        SharedPreferenceUtil.setLong(context, Constant.PARKING_END_TIME, CLEAR_NON_STRING);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_NAME, CLEAR_STRING);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_MAC, CLEAR_STRING);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_PWD, CLEAR_STRING);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_GATEWAY_ID, CLEAR_STRING);
        SharedPreferenceUtil.setString(context, Constant.ESTATE_NAME, CLEAR_STRING);
        SharedPreferenceUtil.setFloat(context, Constant.ESTATE_LONGITUDE, CLEAR_NON_STRING);
        SharedPreferenceUtil.setFloat(context, Constant.ESTATE_LATITUDE, CLEAR_NON_STRING);
    }

}
