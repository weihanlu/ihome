package com.qhiehome.ihome.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alipay.sdk.app.PayTask;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.alipay.AliPayRequest;
import com.qhiehome.ihome.network.model.alipay.AliPayResponse;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceRequest;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceResponse;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeRequest;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeResponse;
import com.qhiehome.ihome.network.service.alipay.AliPayService;
import com.qhiehome.ihome.network.service.pay.AccountBalanceService;
import com.qhiehome.ihome.network.service.pay.PayGuaranteeService;
import com.qhiehome.ihome.pay.AliPay.PayResult;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.pay.AliPay.OrderInfoUtil2_0;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayActivity extends AppCompatActivity {


    @BindView(R.id.rv_pay)
    RecyclerView mRvPay;
    @BindView(R.id.btn_pay)
    Button mBtnPay;
    @BindView(R.id.tv_pay)
    TextView mTvPay;
    @BindView(R.id.layout_title_add)
    RelativeLayout mLayoutTitleAdd;
    @BindView(R.id.layout_detail_add)
    RelativeLayout mLayoutDetailAdd;
    @BindView(R.id.layout_detail_pay)
    RelativeLayout mLayoutDetailPay;
    @BindView(R.id.btn_add_balance_1)
    Button mBtnAddBalance1;
    @BindView(R.id.btn_add_balance_2)
    Button mBtnAddBalance2;
    @BindView(R.id.btn_add_balance_3)
    Button mBtnAddBalance3;
    @BindView(R.id.btn_add_balance_4)
    Button mBtnAddBalance4;
    @BindArray(R.array.add_balance)
    String[] mPriceList;
    @BindView(R.id.toolbar_center)
    Toolbar mTbPay;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;

    private Context mContext;
    private PayListAdapter mAdapter;
    private boolean[] mSelectedNum = {false, false, false}; //支付宝、微信、余额
    private float mFee;
    private int mPayState;
    private int mButtonClicked = 1;
    private int mOrderId = 0;
    private List<Button> mBtnList = new ArrayList<>();
    private boolean mIsFirstLoad;
    private double mAccountBalance = 0.0;

    private static final int ALI_PAY = 0;
    private static final int WECHAT_PAY = 1;
    private static final int ACCOUNT_BALANCE = 2;
    private static final String DECIMAL_2 = "%.2f";

    private static final int MSG_ALIPAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.setStatusBarGradient(this);
        setContentView(R.layout.activity_pay);
        ButterKnife.bind(this);
        Intent intent = this.getIntent();
        mPayState = intent.getIntExtra("payState", 0);
        mOrderId = intent.getIntExtra("orderId", 0);
        mContext = this;
        mSelectedNum[0] = true;
        mIsFirstLoad = true;
        initToolbar();
        initRecyclerView();

        //支付
        switch (mPayState) {
            case Constant.PAY_STATE_ADD_ACCOUNT:    //充值
                mLayoutDetailPay.setVisibility(View.GONE);
                mBtnList.add(mBtnAddBalance1);
                mBtnList.add(mBtnAddBalance2);
                mBtnList.add(mBtnAddBalance3);
                mBtnList.add(mBtnAddBalance4);
                mButtonClicked = 1;
                mBtnAddBalance1.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked - 1] + "元");
                break;
            case Constant.PAY_STATE_GUARANTEE:      //支付担保费
                mLayoutTitleAdd.setVisibility(View.GONE);
                mLayoutDetailAdd.setVisibility(View.GONE);
                mFee = intent.getFloatExtra("fee", 0);
                mTvPay.setText(String.format(Locale.CHINA, DECIMAL_2, mFee));
                mBtnPay.setText("确认支付：" + String.format(Locale.CHINA, DECIMAL_2, mFee) + "元");
                break;
            case Constant.PAY_STATE_TOTAL:          //支付停车费
                mLayoutTitleAdd.setVisibility(View.GONE);
                mLayoutDetailAdd.setVisibility(View.GONE);
                mFee = intent.getFloatExtra("fee", 0);
                mTvPay.setText(String.format(Locale.CHINA, DECIMAL_2, mFee));
                mBtnPay.setText("确认支付：" + String.format(Locale.CHINA, DECIMAL_2, mFee) + "元");
                break;
            default:
                break;
        }


    }

    private void initToolbar() {
        setSupportActionBar(mTbPay);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbPay.setTitle("");
        mTvTitleToolbar.setText("支付订单");
        mTbPay.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        mRvPay.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PayListAdapter();
        mAdapter.setOnItemClickListener(new OnClickListener() {
            @Override
            public void onClick(View view, int i) {
                if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT) {//充值
                    if (i == 1) {
                        mSelectedNum[1] = true;
                        mSelectedNum[0] = false;
                    }
                    if (i == 0) {
                        mSelectedNum[0] = true;
                        mSelectedNum[1] = false;
                    }
                } else {//支付
                    if (i == 2 && mAccountBalance >= mFee) {//账户余额足够支付，完全用余额支付
                        mSelectedNum[0] = false;
                        mSelectedNum[1] = false;
                        mSelectedNum[2] = true;
                    }
                    if (i == 2 && mAccountBalance < mFee && mAccountBalance != 0) {//账户余额不为0且不够支付，选择是否要使用余额
                        mSelectedNum[2] = !mSelectedNum[2];
                    }
                    if (i != 2 && mAccountBalance >= mFee) {//账户余额足够支付，选择支付宝或微信支付
                        mSelectedNum[2] = false;
                        mSelectedNum[1] = false;
                        mSelectedNum[0] = false;
                        mSelectedNum[i] = true;
                    }
                    if (i != 2 && mAccountBalance < mFee) {//账户余额不够支付，选择支付方式
                        mSelectedNum[1] = false;
                        mSelectedNum[0] = false;
                        mSelectedNum[i] = true;
                    }
                    if (mSelectedNum[2]) {
                        mBtnPay.setText("确认支付：" + String.format(Locale.CHINA, DECIMAL_2, mFee >= mAccountBalance ? (mFee - mAccountBalance) : 0.0) + "元");
                    } else {
                        mBtnPay.setText("确认支付：" + String.format(Locale.CHINA, DECIMAL_2, mFee) + "元");
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        mRvPay.setAdapter(mAdapter);
        DividerItemDecoration did = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        mRvPay.addItemDecoration(did);

    }

    @OnClick(R.id.btn_pay)
    public void onViewClicked() {

//        if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT) {
//            int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
//            new MaterialDialog.Builder(mContext)
//                    .title("确认支付？")
//                    .titleColor(red)
//                    .content("接口还没好，假装支付一波ㄟ( ▔, ▔ )ㄏ")
//                    .contentColor(red)
//                    .positiveText("假装支付好了")
//                    .positiveColor(red)
//                    .negativeText("取消")
//                    .canceledOnTouchOutside(false)
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
//                            AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256), Double.valueOf(mPriceList[mButtonClicked - 1]));
//                            Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
//                            call.enqueue(new Callback<AccountBalanceResponse>() {
//                                @Override
//                                public void onResponse(@NonNull Call<AccountBalanceResponse> call, @NonNull Response<AccountBalanceResponse> response) {
//                                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                                        ToastUtil.showToast(mContext, "充值成功");
//                                        PayActivity.this.finish();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(@NCall<AccountBalanceResponse> call, Throwable t) {
//                                    ToastUtil.showToast(mContext, "网络连接异常");
//                                }
//                            });
//                        }
//                    })
//                    .show();
//        }
//
//        // TODO: 2017/8/14 根据选择方式调用支付接口
//        if (mPayState == Constant.PAY_STATE_GUARANTEE) {
//            int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
//            new MaterialDialog.Builder(mContext)
//                    .title("确认支付？")
//                    .titleColor(red)
//                    .content("接口还没好，假装支付一波ㄟ( ▔, ▔ )ㄏ")
//                    .contentColor(red)
//                    .positiveText("假装支付好了")
//                    .positiveColor(red)
//                    .negativeText("取消")
//                    .canceledOnTouchOutside(false)
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            PayGuaranteeService payGuaranteeService = ServiceGenerator.createService(PayGuaranteeService.class);
//                            PayGuaranteeRequest payGuaranteeRequest = new PayGuaranteeRequest(mOrderId);
//                            Call<PayGuaranteeResponse> call = payGuaranteeService.payGuarantee(payGuaranteeRequest);
//                            call.enqueue(new Callback<PayGuaranteeResponse>() {
//                                @Override
//                                public void onResponse(Call<PayGuaranteeResponse> call, Response<PayGuaranteeResponse> response) {
//                                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                                        SharedPreferenceUtil.setLong(mContext, Constant.PARKING_START_TIME, response.body().getData().getEstate().getParking().getShare().getStartTime());
//                                        SharedPreferenceUtil.setLong(mContext, Constant.PARKING_END_TIME, response.body().getData().getEstate().getParking().getShare().getEndTime());
//                                        SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_MAC, response.body().getData().getEstate().getParking().getLockMac());
//                                        SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_PWD, response.body().getData().getEstate().getParking().getPassword());
//                                        SharedPreferenceUtil.setString(mContext, Constant.RESERVE_GATEWAY_ID, response.body().getData().getEstate().getParking().getGatewayId());
//                                        SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, 31);
//                                        SharedPreferenceUtil.setString(mContext, Constant.ESTATE_NAME, response.body().getData().getEstate().getName());
//                                        SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LONGITUDE, (float) response.body().getData().getEstate().getX());
//                                        SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LATITUDE, (float) response.body().getData().getEstate().getY());
//                                        Intent intent = new Intent(PayActivity.this, ReserveActivity.class);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                        startActivity(intent);
//                                        PayActivity.this.finish();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<PayGuaranteeResponse> call, Throwable t) {
//                                    ToastUtil.showToast(mContext, "网络连接异常");
//                                }
//                            });
//
//                        }
//                    })
//                    .show();
//        }

        // TODO: 2017/9/5 私钥加签和订单信息从服务端获取
        /** 支付宝支付业务：入参app_id */
//        String APPID = "2017082508375687";


        /** 商户私钥，pkcs8格式 */
        /** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
        /** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
        /** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
        /** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
        /** 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1 */
//        String RSA2_PRIVATE = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCA+d0SUdqKkBDn0MvkLvEekmQf/4EtTDZmdJG+qcDDypo5tNwuUUcmXwEB3mpEIH2/vj/T6yOU10r5elhcRgh7iOqCWmZHTdoX+kcw2GZnrtrHfR/1k58xeMVOYAX505KohFJnDQ3r5t3tFZw9rfyY4WmqHIBW3jd2/IU3mhimp7Ns/sAqh4bvM6/uMDaoTEptSEVGrE3VugO8Tzh1rb7a8seURT1p4V4fuoZT5V2VJi7x7qfdWf+0EhuTcg4DmfG82llKzinow1SJsOaLKlUIBa9Kvra6GO1oXEp3JPfjxqPJMcJ2Xh6VICLXLTzbeFNzb5lDWfWEc++wkQ0qE1PHAgMBAAECggEAL16VqVLS1y1OaDWxjN8Iw9e0WmQ3B3IEUODjXoluOPrCZgtdCs3jOd6Ouib8FIVyaefv/V9RNCtWaAZdSZaXKvgAWVvmUK3xOfk8CF6STeZUiAwWntVXFI5suPpfd4ATTz06HosW39ttCtRzC9xI98ViT44kPMNkz5izPNal0x8jJvunewGF0/k3/fbaE2uDILbWThZgPu9Sj+WtwERmVkr+Ek6jpVB95vJc5Ey9SbACk8UdHHwMhDS9VA6ZdkS4TNmVELOISB+NrxlrR9wkZ3lSL7qy5lpgoNNUsumgvxs9qHMre7UyyWa94FDp77wlz4NWML/mqZmKZrz3n1FAMQKBgQC7b1BZw8Nnf3ag3iweWMTX1J5QqlhwSQYRp52BKAxY+k8Z56UsUzdqkrj9bYEVMcbyqqJ+UnuNpce3gZZVe5WATTkCsAI2f7h7tEPh8u95wEqYOV5fdHa3NufJEr1tf0cAXsfeAN3UI5dqIubEFmlZrrXeydfrVfQAyDQ+mA9wNQKBgQCwKBD8zcBDigo7QHEWsO9Jy6w98bl/AYz7YnsS5eOGI4COPifn3YGmjgC/26HjiVPUy8dFaYDR5sw27HFelknYZMh6dw33abmM2H+a4k18xqNlwq81SmKD14VAQcV0/GAw9Uj8h1ydE3o43658ViC4gwGbdc5IRyE2T/tRFoariwKBgQCSx7cKtK0/Tagejh3KngV4Z36a+OtM80KXbMWBMVWKEGsFhEvrDDfnc4L+o1RkvphnzIx3lCxBXsOpxwdtZdxLny24FxGEkDxuU1qdhNtYYueHkdV/tvqIu6yD3/ML3pJBjffCuLb+u+iFK1O/1zUlEBZIo5Q9LRBp1F5lbjsYyQKBgHKiICvjWPKaqf3U+cLicVV8jSHiY+wafjw44g5yO5XXFJl8KUviAbT5Q9OWgcsoWr1nvs2U0pfFsa8sPrpm4rdHHo9TWmtfCbh2StPn4LUKLtrRzmLHfUR+w+AE7RIsCgzSEiUDkWlGe4r3RPz0r2ZjGnCoQQ0X/Kzzb4BdQFXxAoGBAJzhU5YkISiyAPxhWLKCr6U2AR+JDwEz5e/NgoiKlb1Q/uionylRBIlnFPwd+v+DQ3xM11HQzoMmB09g1j408IZdMhDxSWgAifVAykTLcSzKGiu78ikYOjt+2JXERzzr9vBUmXYs2hLuchTPZ1w/udsCAXXLw669ce3ZDmlzHfLt";
//        String RSA_PRIVATE = "";
//
//        if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
//            new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialoginterface, int i) {
//                            //
//                            finish();
//                        }
//                    }).show();
//            return;
//        }

        /**
         * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
         * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
         * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
         *
         * orderInfo的获取必须来自服务端；
         */
//        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
//        Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2);
//        String orderParam = OrderInfoUtil2_0.buildOrderParam(params);
//
//        String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
//        String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
//        final String orderInfo = orderParam + "&" + sign;
        mOrderId = new Random().nextInt(100) + 1;
        AliPayService aliPayService = ServiceGenerator.createService(AliPayService.class);
        AliPayRequest aliPayRequest = new AliPayRequest(mOrderId);
        Call<AliPayResponse> call = aliPayService.aliPay(aliPayRequest);
        call.enqueue(new Callback<AliPayResponse>() {
            @Override
            public void onResponse(@NonNull Call<AliPayResponse> call, @NonNull Response<AliPayResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    final String orderInfo = response.body().getData().getOrderInfo();
                    Runnable payRunnable = new Runnable() {
                        @Override
                        public void run() {
                            PayTask alipay = new PayTask(PayActivity.this);
                            Map<String, String> result = alipay.payV2(orderInfo, true);
                            Log.i("msp", result.toString());

                            Message msg = new Message();
                            msg.what = MSG_ALIPAY;
                            msg.obj = result;
                            mHandler.sendMessage(msg);
                        }
                    };

                    Thread payThread = new Thread(payRunnable);
                    payThread.start();
                } else {
                    ToastUtil.showToast(mContext, "支付失败");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AliPayResponse> call,@NonNull Throwable t) {
                ToastUtil.showToast(mContext, "支付失败（服务器繁忙）");
            }
        });

    }

    @OnClick({R.id.btn_add_balance_1, R.id.btn_add_balance_2, R.id.btn_add_balance_3, R.id.btn_add_balance_4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add_balance_1:
                if (mButtonClicked != 1) {
                    mBtnList.get(mButtonClicked - 1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mBtnList.get(mButtonClicked - 1).setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button));
                    mButtonClicked = 1;
                    mBtnAddBalance1.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnAddBalance1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button_selected));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked - 1] + "元");
                }
                break;
            case R.id.btn_add_balance_2:
                if (mButtonClicked != 2) {
                    mBtnList.get(mButtonClicked - 1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mBtnList.get(mButtonClicked - 1).setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button));
                    mButtonClicked = 2;
                    mBtnAddBalance2.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnAddBalance2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button_selected));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked - 1] + "元");
                }
                break;
            case R.id.btn_add_balance_3:
                if (mButtonClicked != 3) {
                    mBtnList.get(mButtonClicked - 1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mBtnList.get(mButtonClicked - 1).setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button));
                    mButtonClicked = 3;
                    mBtnAddBalance3.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnAddBalance3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button_selected));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked - 1] + "元");
                }
                break;
            case R.id.btn_add_balance_4:
                if (mButtonClicked != 4) {
                    mBtnList.get(mButtonClicked - 1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mBtnList.get(mButtonClicked - 1).setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button));
                    mButtonClicked = 4;
                    mBtnAddBalance4.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnAddBalance4.setBackground(ContextCompat.getDrawable(mContext, R.drawable.pay_button_selected));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked - 1] + "元");
                }
                break;
        }
    }

    private class PayListAdapter extends RecyclerView.Adapter<PayListAdapter.PayListHolder> {
        @Override
        public PayListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PayListHolder(LayoutInflater.from(mContext).inflate(R.layout.item_pay_list, parent, false));
        }

        @Override
        public void onBindViewHolder(final PayListHolder holder, int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onClick(holder.itemView, holder.getLayoutPosition());
                    }
                }
            });
            switch (position) {
                case ALI_PAY:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_alipay));
                    holder.tv_pay.setText("支付宝");
                    holder.tv_pay_info.setText("数亿用户都在用，安全可托付");
                    if (mSelectedNum[0]) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                case WECHAT_PAY:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_wechat));
                    holder.tv_pay.setText("微信");
                    holder.tv_pay_info.setText("推荐安装微信5.0以上版本的用户使用");
                    if (mSelectedNum[1]) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                case ACCOUNT_BALANCE:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_pay_account));
                    if (mIsFirstLoad) {
                        holder.tv_pay.setText("账户余额");
                        holder.tv_pay_info.setText("正在获取账户余额");
                        getAccountBalance(holder);
                        mIsFirstLoad = false;
                    }

                    if (mSelectedNum[2]) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getItemCount() {
            if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT) {
                return 2;
            } else {
                return 3;
            }
        }

        class PayListHolder extends RecyclerView.ViewHolder {
            private ImageView iv_pay;
            private TextView tv_pay;
            private TextView tv_pay_info;
            private ImageView iv_pay_select;

            private PayListHolder(View view) {
                super(view);
                iv_pay = (ImageView) view.findViewById(R.id.iv_pay);
                tv_pay = (TextView) view.findViewById(R.id.tv_pay);
                tv_pay_info = (TextView) view.findViewById(R.id.tv_pay_info);
                iv_pay_select = (ImageView) view.findViewById(R.id.iv_pay_select);
            }
        }

        public void setOnItemClickListener(OnClickListener listener) {
            this.onClickListener = listener;
        }

        private OnClickListener onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, int i);
    }

    private void getAccountBalance(final PayListAdapter.PayListHolder holder) {
        AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256), 0.0);
        Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
        call.enqueue(new Callback<AccountBalanceResponse>() {
            @Override
            public void onResponse(Call<AccountBalanceResponse> call, Response<AccountBalanceResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    String pay_info = "账户余额：";
                    mAccountBalance = response.body().getData().getAccount();
                    pay_info += String.format(Locale.CHINA, DECIMAL_2, response.body().getData().getAccount());
                    pay_info += "元";
                    holder.tv_pay_info.setText(pay_info);
                }
            }

            @Override
            public void onFailure(Call<AccountBalanceResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ALIPAY: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(mContext, "支付成功", Toast.LENGTH_SHORT).show();
                        PayGuaranteeService payGuaranteeService = ServiceGenerator.createService(PayGuaranteeService.class);
                        PayGuaranteeRequest payGuaranteeRequest = new PayGuaranteeRequest(mOrderId);
                        Call<PayGuaranteeResponse> call = payGuaranteeService.payGuarantee(payGuaranteeRequest);
                        call.enqueue(new retrofit2.Callback<PayGuaranteeResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<PayGuaranteeResponse> call, @NonNull Response<PayGuaranteeResponse> response) {
                                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {

                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<PayGuaranteeResponse> call, @NonNull Throwable t) {

                            }
                        });
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(mContext, "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                default:
                    break;
            }
        };
    };

}
