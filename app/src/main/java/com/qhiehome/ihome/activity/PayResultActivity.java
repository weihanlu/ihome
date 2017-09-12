package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.util.Constant;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PayResultActivity extends BaseActivity {

    private static final String TAG = "PayResultActivity";

    private static final String KEY_ACCOUNT = "key_account";

    private int mPayState;

    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitle;

    @BindView(R.id.tv_pay_account)
    TextView mTvAccount;

    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_result);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        mPayState = getIntent().getIntExtra(PayActivity.PAY_STATE, 0);
    }

    private void initView() {
        initToolbar();
        mTvTitle.setText("支付结果");
        float account = getIntent().getFloatExtra(KEY_ACCOUNT, (float)0.00);
        mTvAccount.setText(String.format(Locale.CHINA, "%.2f元", account));
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public static void start(Context context, float account, int payState) {
        Intent intent = new Intent(context, PayResultActivity.class);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(PayActivity.PAY_STATE, payState);
        context.startActivity(intent);
    }


    @OnClick(R.id.btn_pay_accomplish)
    public void onViewClicked() {
        if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT || mPayState == Constant.PAY_STATE_TOTAL) {
            MainActivity.start(mContext);
        } else if (mPayState == Constant.PAY_STATE_GUARANTEE) {
            ReserveActivity.start(mContext);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.start(mContext);
    }
}
