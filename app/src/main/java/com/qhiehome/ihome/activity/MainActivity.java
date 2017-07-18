package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.fragment.EditFragment;
import com.qhiehome.ihome.fragment.MeFragment;
import com.qhiehome.ihome.fragment.ParkFragment;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int VIEWPAGER_OFF_LIMIT = 2;

    private ViewPager mViewPager;
    private List<Fragment> mTabs = new ArrayList<>();

    private TextView mTvPark;
    private TextView mTvEdit;
    private TextView mTvMe;

    private List<TextView> mTabTextIndicators = new ArrayList<>();

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        initFragments();
        ActivityManager.add(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        initToolbar();

        mTvPark = (TextView) findViewById(R.id.tv_park);
        mTvEdit = (TextView) findViewById(R.id.tv_edit);
        mTvMe = (TextView) findViewById(R.id.tv_me);
        mTabTextIndicators.add(mTvPark);
        mTabTextIndicators.add(mTvEdit);
        mTabTextIndicators.add(mTvMe);

        RelativeLayout mRlPark = (RelativeLayout) findViewById(R.id.rl_park);
        RelativeLayout mRlEdit = (RelativeLayout) findViewById(R.id.rl_edit);
        RelativeLayout mRlMe = (RelativeLayout) findViewById(R.id.rl_me);

        mRlPark.setOnClickListener(this);
        mRlEdit.setOnClickListener(this);
        mRlMe.setOnClickListener(this);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("附近");
        setSupportActionBar(mToolbar);

        initMenu(mToolbar);
    }

    private void initMenu(Toolbar mToolbar) {
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_scan) {
                    ToastUtil.showToast(MainActivity.this, "扫描二维码");
                }
                return true;
            }
        });
    }

    private void initFragments() {
        mTabs.add(ParkFragment.newInstance());
        mTabs.add(EditFragment.newInstance("", ""));
        mTabs.add(MeFragment.newInstance());

        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mTabs.get(position);
            }

            @Override
            public int getCount() {
                return mTabs.size();
            }
        };

        mViewPager.setOffscreenPageLimit(VIEWPAGER_OFF_LIMIT);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        resetOtherTabText();
        switch (position) {
            case 0:
                mToolbar.setTitle("附近");
                mTvPark.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case 1:
                mToolbar.setTitle("发布");
                mTvEdit.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case 2:
                mToolbar.setTitle("个人");
                mTvMe.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        resetOtherTabText();
        switch (v.getId()) {
            case R.id.rl_park:
                mViewPager.setCurrentItem(0, false);
                mTvPark.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.rl_edit:
                mViewPager.setCurrentItem(1, false);
                mTvEdit.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
            case R.id.rl_me:
                mViewPager.setCurrentItem(2, false);
                mTvMe.setTextColor(getResources().getColor(R.color.colorAccent));
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
    }

}
