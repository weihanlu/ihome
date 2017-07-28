package com.qhiehome.ihome.persistence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.ToastUtil;

/**
 * Created by YueMa on 2017/7/27.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == Constant.TIMER_ACTION){
            ParkingSQLHelper parkingSQLHelper = new ParkingSQLHelper(context);
            //ToastUtil.showToast(context,"清除数据库");
        }
    }

}
