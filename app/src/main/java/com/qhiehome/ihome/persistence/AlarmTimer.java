package com.qhiehome.ihome.persistence;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by YueMa on 2017/7/27.
 */

public class AlarmTimer {
    public static void setRepeatAlarmTime(Context context, long firstTime, long cycTime, String action, int AlarmManagerType){
        Intent myIntent = new Intent();
        myIntent.setAction(action);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, myIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManagerType, firstTime, cycTime, sender);
    }
}
