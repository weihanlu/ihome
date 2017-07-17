package com.qhiehome.ihome.manager;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * used to manager all activities.
 */

public class ActivityManager {

    private static List<Activity> activities = new ArrayList<>();

    public static void add(Activity activity) {
        if (!activities.contains(activity)) {
            activities.add(activity);
        }
    }

    public static void remove(Activity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);
        }
    }

    public static void finishAll() {
        for (Activity activity: activities) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
    }

}
