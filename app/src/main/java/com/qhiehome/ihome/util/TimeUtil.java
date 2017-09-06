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

    public static final int TIME_INTERVAL = 30;

    private SimpleDateFormat mDateFormat;

    private int mTimeInterval;

    private TimeUtil(){
        timeMap = new ArrayMap<>();
        mDateFormat= new SimpleDateFormat("HH:mm", Locale.CHINA);
    }

    private static class TimeUtilHolder {
        private static final TimeUtil INSTANCE = new TimeUtil();
    }

    public static TimeUtil getInstance() {
        return TimeUtilHolder.INSTANCE;
    }

    public TimeUtil setTimeInterval(int timeInterval) {
        if (timeMap != null) {
            timeMap.clear();
            mTimeInterval = timeInterval;
            for (int i = getPassed(System.currentTimeMillis()); i < (24 * 60 / timeInterval); i++) {
                Date current = new Date(getTimeStamp(i));
                timeMap.put(mDateFormat.format(current), i);
                LogUtil.d(TAG, "first init: " + mDateFormat.format(current));
            }
        }
        return this;
    }

    /**
     *
     * @return oneDayTime 得到一天的时间
     */
    public List<String> getStartTime() {
        return new ArrayList<>(timeMap.keySet()).subList(0, timeMap.size() - 1);
    }

    public List<String> getEndTime() {
        return new ArrayList<>(timeMap.keySet()).subList(1, timeMap.size());
    }

    public long getTimeStamp(String str) {
        int passedTime = timeMap.get(str);
        return getTimeStamp(passedTime);
    }

    private int getPassed(long timeStamp) {
        return (int) (timeStamp - getZeroTimeStamp()) / (mTimeInterval * 60 * 1000) + 1;
    }

    private long getTimeStamp(int passTime) {
        return getZeroTimeStamp() + passTime * mTimeInterval * 60 * 1000;
    }

    /**
     * 获取当天 零点的时间戳【linux】
     * @return 0:00 timestamp of current day.
     */
    private long getZeroTimeStamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public Date millis2Date(final long millis) {
        return new Date(millis);
    }

}
