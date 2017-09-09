package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.view.ProgressWebView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.qhiehome.ihome.util.Constant.CONTRACT_URL;

public class ServiceContractActivity extends BaseActivity {

    private static final String TAG = ServiceContractActivity.class.getSimpleName();


    @BindView(R.id.pwv_web_contract)
    ProgressWebView mProgressWebView;
    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_contract);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initServiceContract();
    }

    private void initServiceContract() {
        mProgressWebView.loadUrl(CONTRACT_URL);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setTitle("");
        mTvTitleToolbar.setText("服务协议");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ServiceContractActivity.class);
        context.startActivity(intent);
    }

}
