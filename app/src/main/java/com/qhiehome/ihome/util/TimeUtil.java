package com.qhiehome.ihome.util;

import android.util.ArrayMap;
import android.util.SparseArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TimeUtil {

    private ArrayMap<String, Integer> timeMap;

    private TimeUtil(){
        timeMap = new ArrayMap<>();
        for (int i = 0; i < 24; i++) {
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
        return getTimesmorning() + passedHour * 3600 * 1000;
    }


    private static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static Date millis2Date(final long millis) {
        return new Date(millis);
    }


}
