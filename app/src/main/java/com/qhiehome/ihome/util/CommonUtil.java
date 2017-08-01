package com.qhiehome.ihome.util;


import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class CommonUtil {

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * if softkey is visible, make it invisible and vice versa.
     * @param context context
     */
    public static void toggleKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
