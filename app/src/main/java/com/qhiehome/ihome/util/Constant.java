package com.qhiehome.ihome.util;

/**
 * Created by xiang on 2017/7/10
 */

public class Constant {

    public static final String GLOBAL_FILTER = "ihome";

    public static final String UPDATE_ENABLED = "false";

    public static final String LOCK_PASSWORD_KEY = "lockpwd";

    public static final String TEST_PHONE_NUM = "f8cfd23a25811570298c8773bdca4d4d538d0d7fe52f6e5b3aefd08b907c8df2";

    public static final String PHONE_KEY = "phoneNum";

    public static final int TIME_PERIOD_LIMIT = 3;

    public static final int RESPONSE_SUCCESS_CODE = 200;

    public static final int ERROR_SUCCESS_CODE = 1;

    public static final int VERIFY_NUM = 6;

    public static final String OWNED_PARKING_KEY = "owned_parking_key";

    public static final String TIMER_ACTION= "TIMER_ACTION";

    public static final String CONTRACT_URL = "http://39.108.77.50:3389/api/AppMobile/protocol.html";

    public static final String OFFICIAL_WEB_URL = "http://www.qhiehome.com";

    public static final String USER_TYPE = "USER_TYPE";
    public static final int USER_TYPE_OWNER = 1;    //业主
    public static final int USER_TYPE_TEMP = 0;     //临时用户

    /*******persistence data********/
    //navigation
    public static final String CURRENT_LONGITUDE = "CURRENT_LONGITUDE";
    public static final String CURRENT_LATITUDE = "CURRENT_LATITUDE";
    public static final String ESTATE_LONGITUDE = "ESTATE_LONGITUDE";
    public static final String ESTATE_LATITUDE = "ESTATE_LATITUDE";
    public static final String ESTATE_NAME = "ESTATE_NAME";
    //record order info
    public static final String ORDER_CREATE_TIME = "orderCreateTime";
    public static final String ORDER_STATE = "ORDER_STATE";
    public static final String ORDER_ID = "order_id";
    //record enter/leave time without network
    public static final String PARKING_ENTER_TIME = "enterTime";
    public static final String PARKING_LEAVE_TIME = "leaveTime";
    public static final String NEED_POST_ENTER_TIME = "NEED_POST_ENTER_TIME";
    public static final String NEED_POST_LEAVE_TIME = "NEED_POST_LEAVE_TIME";
    //remind user parking/leave without network
    public static final String PARKING_START_TIME = "startTime";
    public static final String PARKING_END_TIME = "endTime";
    //control lock without network
    public static final String RESERVE_LOCK_MAC = "RESERVE_LOCK_MAC";
    public static final String RESERVE_LOCK_NAME = "RESERVE_LOCK_NAME";
    public static final String RESERVE_LOCK_PWD = "RESERVE_LOCK_PWD";
    public static final String RESERVE_GATEWAY_ID = "RESERVE_GATEWAY_ID";
    //record advanced using right
    public static final String ADVANCED_USE = "ADVANCED_USE";
    /*******persistence data********/

    public static final int PAY_STATE_ADD_ACCOUNT = 0;
    public static final int PAY_STATE_GUARANTEE = 1;
    public static final int PAY_STATE_TOTAL = 2;

    /********OrderState********/
    public static final int ORDER_STATE_TEMP_RESERVED = 30;//btn：取消+支付  info：剩余支付时间，支付金额
    public static final int ORDER_STATE_RESERVED = 31;//取消+导航+升降车位锁+小区地图+出入证  info：最晚停车时间
    public static final int ORDER_STATE_PARKED = 32;//导航+升降车位锁+小区地图  info：停车时间+最晚离开时间
    public static final int ORDER_STATE_NOT_PAID = 33;//支付 info：支付金额
    public static final int ORDER_STATE_PAID = 34;//NA  info：支付金额
    public static final int ORDER_STATE_TIMEOUT = 38;//NA info：支付金额
    public static final int ORDER_STATE_CANCEL = 39;//NA

    // update module

    public static final String APK_UPDATE_UKEY = "ce208a1437998ef0a8e5f60b7dc4aac1";
    public static final String APK_UPDATE_API_KEY = "61bb58e6d87d6d2d6b84c7a44c237a7e";
    public static final String APK_UPDATE_PAGE_NUM = "1";

    public static final String APK_UPDATE_URL_PATTERN = "http://www.pgyer.com/apiv1/app/install?aKey=%s&_api_key=61bb58e6d87d6d2d6b84c7a44c237a7e&password=ihome";

    //city configurations
    public static final String MIN_SHARING_PERIOD = "minSharingPeriod";
    public static final String MIN_CHARGING_PERIOD = "minChargingPeriod";
    public static final String FREE_CANCELLATION_TIME = "freeCancellationTime";

    // WeChat pay
    public static final String APP_ID = "wx42a91e33c4b3a97b";
    public static final String APP_SIGN = "b84cdbf418f8f46e5661a8a4ae510fed";
    public static final String APP_PACKAGE = "com.qhiehome.ihome";


}
