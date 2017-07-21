package com.qhiehome.ihome.util;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class TimeUtil {

    private SparseArray<String> timeTable;

    private TimeUtil(){
        timeTable = new SparseArray<>();
        for (int i = 0; i < 24; i++) {
            timeTable.put(i, i + ":00");
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
        List<String> oneDayTime = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            oneDayTime.add(timeTable.get(i));
        }
        return oneDayTime;
    }

}
