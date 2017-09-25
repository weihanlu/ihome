package com.qhiehome.ihome.util;

import android.content.Context;

import com.qhiehome.ihome.bean.UserBean;

public class PersistenceUtil {

    public static void userInfo(Context context, UserBean userBean){
        String phoneNum = userBean.getName();
        SharedPreferenceUtil.setString(context, Constant.PHONE_KEY, phoneNum);
    }

}
