package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
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

    private static final String PAY_METHOD = "pay_method";

    private static final int PAY_METHOD_ALI = 0;
    private static final int PAY_METHOD_WECHAT = 1;
    private static final int PAY_METHOD_WALLET = 2;

    private int mPayState;

    private int mPayMethod;

    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitle;

    @BindView(R.id.tv_pay_account)
    TextView mTvAccount;

    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;

    @BindView(R.id.iv_pay_result)
    ImageView mIvResult;

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
        mPayMethod = getIntent().getIntExtra(PAY_METHOD, -1);
    }

    private void initView() {
        initToolbar();
        mTvTitle.setText("支付结果");
        float account = getIntent().getFloatExtra(KEY_ACCOUNT, (float)0.00);
        mTvAccount.setText(String.format(Locale.CHINA, "%.2f元", account));
        switch (mPayMethod){
            case 0:
                mIvResult.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_ali));
                break;
            case 1:
                mIvResult.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_wechat));
                break;
            case 2:
                mIvResult.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_wallet));
                break;
            default:
                break;
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public static void start(Context context, float account, int payState, int payMethod) {
        Intent intent = new Intent(context, PayResultActivity.class);
        intent.putExtra(PAY_METHOD, payMethod);
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
