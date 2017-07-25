package com.qhiehome.ihome.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.qhiehome.ihome.manager.ActivityManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
    }
}
