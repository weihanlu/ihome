package com.qhiehome.ihome.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xiang on 2017/7/3
 */

public class ToastUtil {

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
