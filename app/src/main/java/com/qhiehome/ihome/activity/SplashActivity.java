package com.qhiehome.ihome.activity;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.LogUtil;

import me.shihao.library.XStatusBarHelper;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.start(SplashActivity.this);
                LogUtil.d(TAG, "SplashActivity finished");
                finish();
            }
        }, SPLASH_DURATION);
        XStatusBarHelper.immersiveStatusBar(this);
    }

}
