package com.qhiehome.ihome.util;

import android.content.Context;

/**
 * Created by YueMa on 2017/9/13.
 */

public class OrderUtil {

    private OrderUtil(){}

    private static class OrderUtilHolder {
        private static final OrderUtil INSTANCE = new OrderUtil();
    }

    public static OrderUtil getInstance() {
        return OrderUtilHolder.INSTANCE;
    }

    public void setOrderInfo(Context context, int orderId, int orderState, long startTime, long endTime, String lockName, String lockMac, String lockPwd, String gateWayId, String estateName, double estateX, double estateY){
        SharedPreferenceUtil.setInt(context, Constant.ORDER_ID, orderId);
        SharedPreferenceUtil.setInt(context, Constant.ORDER_STATE, orderState);
        SharedPreferenceUtil.setLong(context, Constant.PARKING_START_TIME, startTime);
        SharedPreferenceUtil.setLong(context, Constant.PARKING_END_TIME, endTime);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_NAME, lockName);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_MAC, lockMac);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_LOCK_PWD, lockPwd);
        SharedPreferenceUtil.setString(context, Constant.RESERVE_GATEWAY_ID, gateWayId);
        SharedPreferenceUtil.setString(context, Constant.ESTATE_NAME, estateName);
        SharedPreferenceUtil.setFloat(context, Constant.ESTATE_LONGITUDE, (float) estateX);
        SharedPreferenceUtil.setFloat(context, Constant.ESTATE_LATITUDE, (float) estateY);
    }
}
