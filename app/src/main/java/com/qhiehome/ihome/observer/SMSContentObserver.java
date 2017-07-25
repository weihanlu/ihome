package com.qhiehome.ihome.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.LoginFilter;
import android.util.Log;

import com.qhiehome.ihome.activity.LoginActivity;
import com.qhiehome.ihome.util.Constant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YueMa on 2017/7/21.
 */

public class SMSContentObserver extends ContentObserver {

    //上下文
    private Context mcontext;
    private Handler mhandler; //更新線程
    private String code; //驗證碼

    //有參的構造方法
    public SMSContentObserver(Context context, Handler handler) {
        super(handler);
        mcontext = context;
        mhandler = handler;
    }
    /**
     * 回调函数, 当所监听的Uri发生改变时，就会回调此方法
     * 注意当收到短信的时候会回调两次
     * @param selfChange
     * 此值意义不大 一般情况下该回调值false
     */
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        //打印一下Log
        Log.e("===================", uri.toString());
        // 第一次回调 不是我们想要的 所以直接返回
        if(uri.toString().equals("content://sms/raw")){
            return ;
        }
        // 第二次回调 我們查询收件箱里的内容
        Uri inboxUri = Uri.parse("content://sms/inbox");

        // 按时间顺序排列数据库的短信
        Cursor c = mcontext.getContentResolver().query(inboxUri,
                null, null, null, "date desc");
        //判斷游標
        if(c!=null){
            //判斷是否為第一個
            if(c.moveToFirst()){
                //獲取手機號
                String address = c.getString(c.getColumnIndex("address")); //系統默認的
                // 获取短信内容
                String body = c.getString(c.getColumnIndex("body")); //系統默認的
                // 判断手机号是否为目的號碼
//                if (!address.equals("想要获得验证码的手机号码")) //目的號碼
//                {
//                    return;
//                }

                // 正则表达式截取短信中的6位验证码
                Pattern pattern = Pattern.compile("(\\d{" + Constant.VERIFY_NUM + "})");
                Matcher matcher = pattern.matcher(body);

                // 如果找到通过Handler发送给主线程
                if (matcher.find())
                {
                    code = matcher.group(0);
                    mhandler.obtainMessage(LoginActivity.GET_VERIFICATION, code).sendToTarget();
                    return;
                }
            }
        }
        c.close();
    }
}

