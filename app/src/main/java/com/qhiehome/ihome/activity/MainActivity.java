package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.fragment.MeFragment;
import com.qhiehome.ihome.fragment.ParkFragment;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.util.Constant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mPhoneNum;

    private TextView mTvPark;
    private TextView mTvMe;

    private List<TextView> mTabTextIndicators = new ArrayList<>();

    Toolbar mToolbar;

    Fragment mParkFragment;
    Fragment mMeFragment;
    Fragment mThisFragment;
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initFragments(savedInstanceState);
        ActivityManager.add(this);
    }

    private void initData() {
        mPhoneNum = getIntent().getStringExtra(Constant.PHONE_PARAM);
    }

    private void initView() {
        initToolbar();
        mTvPark = (TextView) findViewById(R.id.tv_park);
        mTvMe = (TextView) findViewById(R.id.tv_me);
        mTabTextIndicators.add(mTvPark);
        mTabTextIndicators.add(mTvMe);

        RelativeLayout mRlPark = (RelativeLayout) findViewById(R.id.rl_park);
        RelativeLayout mRlMe = (RelativeLayout) findViewById(R.id.rl_me);

        mRlPark.setOnClickListener(this);
        mRlMe.setOnClickListener(this);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("附近");
        setSupportActionBar(mToolbar);

    }

    private void initFragments(Bundle savedInstanceState) {
        mFragmentManager = getSupportFragmentManager();
        // First init load ParkFragment
        if (savedInstanceState == null) {
            mParkFragment = ParkFragment.newInstance();
            mMeFragment = MeFragment.newInstance(mPhoneNum);
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, mParkFragment, ParkFragment.TAG).commit();
        } else {
            mParkFragment = mFragmentManager.findFragmentByTag(ParkFragment.TAG);
            mMeFragment = mFragmentManager.findFragmentByTag(MeFragment.TAG);
            mFragmentManager.beginTransaction()
                    .show(mParkFragment)
                    .hide(mMeFragment)
                    .commit();
        }
        mThisFragment = mParkFragment;
    }

    @Override
    public void onClick(View v) {
        resetOtherTabText();
        switch (v.getId()) {
            case R.id.rl_park:
                mTvPark.setTextColor(getResources().getColor(R.color.colorAccent));
                switchContent(mMeFragment, mParkFragment);
                break;
            case R.id.rl_me:
                mTvMe.setTextColor(getResources().getColor(R.color.colorAccent));
                switchContent(mParkFragment, mMeFragment);
                break;
            default:
                break;
        }
    }

    private void resetOtherTabText() {
        for (TextView textView: mTabTextIndicators) {
            textView.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void switchContent(Fragment from, Fragment to) {
        if (mThisFragment != to) {
            mThisFragment = to;
            if (!to.isAdded()) {
                String tag = (to instanceof MeFragment) ? MeFragment.TAG: ParkFragment.TAG;
                mFragmentManager.beginTransaction().hide(from).add(R.id.fragment_container, to, tag).commit();
            } else {
                mFragmentManager.beginTransaction().hide(from).show(to).commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public static void start(Context context, String phoneNum) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constant.PHONE_PARAM, phoneNum);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
    }

}
