package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.fragment.OrderOwnerFragment;
import com.qhiehome.ihome.fragment.UserLockFragment;
import com.qhiehome.ihome.util.CommonUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserLockActivity extends BaseActivity {

    private static final String TAG = UserLockActivity.class.getSimpleName();
    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;

    private Context mContext;

    @BindView(R.id.tl_user_lock)
    TabLayout mTlUserLock;
    @BindView(R.id.vp_user_lock)
    ViewPager mVpUserLock;

    private ArrayList<String> mTitles;
    private ArrayList<Fragment> mFragments;
    private TabLayoutAdapter mAdapter;

    UserLockFragment mUserLockFragment;
    OrderOwnerFragment mOrderOwnerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.setStatusBarGradient(this);
        setContentView(R.layout.activity_user_lock);
        ButterKnife.bind(this);
        mContext = this;
        initData();
        initView();
    }

    private void initData() {
        mTitles = new ArrayList<String>() {{
            add("我的车锁");
            add("使用历史");
        }};
        mUserLockFragment = new UserLockFragment();
        mOrderOwnerFragment = new OrderOwnerFragment();
        mFragments = new ArrayList<Fragment>() {{
            add(mUserLockFragment);
            add(mOrderOwnerFragment);
        }};
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UserLockActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        initToolbar();
        initTabLayout();
    }

    private void initTabLayout() {
        mAdapter = new TabLayoutAdapter(getSupportFragmentManager(), mTitles, mFragments);
        mVpUserLock.setAdapter(mAdapter);
        mTlUserLock.setupWithViewPager(mVpUserLock);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
        mTvTitleToolbar.setText("我的车位");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUserLockFragment.onActivityResult(requestCode, resultCode, data);
    }

    private class TabLayoutAdapter extends FragmentPagerAdapter {

        private ArrayList<String> titles;
        private ArrayList<Fragment> fragments;

        public TabLayoutAdapter(FragmentManager fm, ArrayList<String> titles, ArrayList<Fragment> fragments) {
            super(fm);
            this.titles = titles;
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
