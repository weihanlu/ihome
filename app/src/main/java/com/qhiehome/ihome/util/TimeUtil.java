package com.qhiehome.ihome.util;

import android.content.Context;
import android.util.ArrayMap;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeUtil {

    private static final String TAG = "TimeUtil";

    private ArrayMap<String, Integer> timeMap;

    private TimeUtil(){
        timeMap = new ArrayMap<>();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.CHINA);
        for (int i = getPassedHalfHour(System.currentTimeMillis()); i <= 47; i++) {
            Date current = new Date(getTimeStamp(i));
            timeMap.put(format.format(current), i);
            LogUtil.d(TAG, "first init: " + format.format(current));
        }
    }

    private static class TimeUtilHolder {
        private static final TimeUtil INSTANCE = new TimeUtil();
    }

    public static TimeUtil getInstance() {
        return TimeUtilHolder.INSTANCE;
    }

    /**
     *
     * @return oneDayTime 得到一天的时间
     */
    public List<String> getOnedayTime() {
        return new ArrayList<>(timeMap.keySet());
    }

    /**
     * 获取当天 零点的时间戳【linux】
     * @return 0:00 timestamp of current day.
     */
    private long getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public void update() {
        if (timeMap != null) {
            timeMap.clear();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.CHINA);
            for (int i = getPassedHalfHour(System.currentTimeMillis()); i <= 47; i++) {
                Date current = new Date(getTimeStamp(i));
                timeMap.put(format.format(current), i);
                LogUtil.d(TAG, "update: " + format.format(current));
            }
        }
    }

    public long getTimeStamp(String str) {
        int passHalfHour = timeMap.get(str);
        return getTimeStamp(passHalfHour);
    }

    public int getPassedHalfHour(long timeStamp) {
        return (int)(timeStamp - getTimesmorning()) / (1800 * 1000) + 1;
    }

    private long getTimeStamp(int passHalfHour) {
        return getTimesmorning() + passHalfHour * 1800 * 1000;
    }

    public Date millis2Date(final long millis) {
        return new Date(millis);
    }

    public void recordTime(Context context, boolean isStart){
        SharedPreferenceUtil.setLong(context, isStart ? Constant.PARKING_START_TIME : Constant.PARKING_END_TIME, System.currentTimeMillis());
    }

}
