package com.qhiehome.ihome.util;

import android.util.Log;

/**
 * Log utility class to encapsulate android log function
 */

public class LogUtil {

    private static final boolean DEBUG_MODE = true;

    public static void v(String filter, String msg) {
        if (DEBUG_MODE) {
            Log.d(filter, msg);
        }
    }

    public static void d(String filter, String msg) {
        if (DEBUG_MODE) {
            Log.d(filter, msg);
        }
    }

    public static void i(String filter, String msg) {
        if (DEBUG_MODE) {
            Log.i(filter, msg);
        }
    }

    public static void w(String filter, String msg) {
        if (DEBUG_MODE) {
            Log.w(filter, msg);
        }
    }

    public static void e(String filter, String msg) {
        if (DEBUG_MODE) {
            Log.e(filter, msg);
        }
    }

}
