package com.qhiehome.ihome.util;

import android.util.ArrayMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeUtil {

    private ArrayMap<String, Integer> timeMap;

    private TimeUtil(){
        timeMap = new ArrayMap<>();
        for (int i = getPassedHour(System.currentTimeMillis()); i <= 24; i++) {
            timeMap.put(i + ":00", i);
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

    public long getTimeStamp(String str) {
        int passedHour = timeMap.get(str);
        return getTimeStamp(passedHour);
    }

    public int getPassedHour(long timeStamp) {
        return (int)(timeStamp - getTimesmorning()) / (3600 * 1000) + 1;
    }

    public long getTimeStamp(int passHours) {
        return getTimesmorning() + passHours * 3600 * 1000;
    }

    public Date millis2Date(final long millis) {
        return new Date(millis);
    }

}
