package com.qhiehome.ihome.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.BaseActivity;
import com.qhiehome.ihome.activity.PayActivity;
import com.qhiehome.ihome.activity.PayResultActivity;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.ToastUtil;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WXPayEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;
    @BindView(R.id.tv_title_toolbar)
    TextView mToolbarTitle;
    @BindView(R.id.iv_wxpay_result)
    ImageView mIvWxpayResult;
    @BindView(R.id.tv_wxpay_result)
    TextView mTvWxpayResult;
    @BindView(R.id.tv_wxpay_account)
    TextView mTvWxpayAccount;
    @BindView(R.id.btn_wxpay_accomplish)
    Button mBtnWxpayAccomplish;

    private IWXAPI api;

    public static final String ACTION = "pay_result";
    public static final String RESP_ERRCODE = "resp_errcode";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        api = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        initToolbar();
    }

    private void initToolbar(){
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbarTitle.setText("支付结果");
    }

    @Override
    public void onReq(BaseReq req) {

    }


    @Override
    public void onResp(BaseResp resp) {
        Intent intent = new Intent();
        intent.setAction(ACTION);
        intent.putExtra(RESP_ERRCODE, resp.errCode);
        WXPayEntryActivity.this.sendBroadcast(intent);
    }

    @OnClick(R.id.btn_wxpay_accomplish)
    public void onViewClicked() {
        finish();
    }
}
