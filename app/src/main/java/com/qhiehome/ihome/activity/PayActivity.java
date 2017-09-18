package com.qhiehome.ihome.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.qhiehome.ihome.util.OrderUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayActivity extends BaseActivity {

    public static final String PAY_STATE = "payState";
    public static final String ORDER_ID = "orderId";
    public static final String FEE = "fee";

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

    private float mCurrentAccount;

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

    private IWXAPI mIwxApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        // 初始化mIwxApi
        mIwxApi = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
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
        mTvTitleToolbar.setText(mPayState == Constant.PAY_STATE_ADD_ACCOUNT? "充值": "支付订单");
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
                /*  账户余额可以混合支付
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
                }*/
                if (!(i == 2 && mAccountBalance < mFee)){
                    mSelectedNum[0] = false;
                    mSelectedNum[1] = false;
                    mSelectedNum[2] = false;
                    mSelectedNum[i] = true;
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
        switch (mPayState) {
            case Constant.PAY_STATE_ADD_ACCOUNT:
                mCurrentAccount = Float.valueOf(mPriceList[mButtonClicked - 1]);
                if (mSelectedNum[0]) {
                    AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
                    AccountBalanceRequest accountBalanceRequest =
                            new AccountBalanceRequest(EncryptUtil.
                                    encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""),
                                            EncryptUtil.ALGO.RSA), Double.valueOf(mPriceList[mButtonClicked - 1]));
                    Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
                    call.enqueue(new Callback<AccountBalanceResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<AccountBalanceResponse> call, @NonNull Response<AccountBalanceResponse> response) {
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
                                ToastUtil.showToast(mContext, "充值失败");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<AccountBalanceResponse> call, @NonNull Throwable t) {

                        }
                    });
                } else {
                    // TODO: 2017/9/17 微信支付

                }
                break;
            case Constant.PAY_STATE_GUARANTEE:
            case Constant.PAY_STATE_TOTAL:
                mCurrentAccount = mFee;
                if (mSelectedNum[0]) {
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
                } else if (mSelectedNum[1]){

                } else {
                    payWithAccount();
                }
            default:
                break;
        }

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
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_pay_alipay));
                    holder.tv_pay.setText("支付宝");
                    holder.tv_pay_info.setText("数亿用户都在用，安全可托付");
                    if (mSelectedNum[0]) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                case WECHAT_PAY:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_pay_wechat));
                    holder.tv_pay.setText("微信");
                    holder.tv_pay_info.setText("推荐安装微信5.0以上版本的用户使用");
                    if (mSelectedNum[1]) {
                        holder.iv_pay_select.setVisibility(View.VISIBLE);
                    } else {
                        holder.iv_pay_select.setVisibility(View.INVISIBLE);
                    }
                    break;
                case ACCOUNT_BALANCE:
                    holder.iv_pay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ic_wallet));
                    if (mIsFirstLoad) {
                        holder.tv_pay.setText("账户余额");
                        holder.tv_pay_info.setText("正在获取账户余额");
                        changeAccountBalance(holder, 0.0);
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

    /**
     * for add balance
     * @param holder    to show balance
     * @param change    0 or positive num
     */
    private void changeAccountBalance(final PayListAdapter.PayListHolder holder, final double change) {
        AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.RSA), change);
        Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
        call.enqueue(new Callback<AccountBalanceResponse>() {
            @Override
            public void onResponse(Call<AccountBalanceResponse> call, Response<AccountBalanceResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    String pay_info = "账户余额：";
                    mAccountBalance = response.body().getData().getAccount();
                    if (holder != null){
                        pay_info += String.format(Locale.CHINA, DECIMAL_2, response.body().getData().getAccount());
                        pay_info += "元";
                        holder.tv_pay_info.setText(pay_info);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountBalanceResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    private void payWithAccount(){
        AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.RSA), -mFee, mOrderId);
        Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
        call.enqueue(new Callback<AccountBalanceResponse>() {
            @Override
            public void onResponse(Call<AccountBalanceResponse> call, Response<AccountBalanceResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    if (mPayState == Constant.PAY_STATE_GUARANTEE){
                        PayGuaranteeFee();
                    }else {
                        PayResultActivity.start(mContext, mCurrentAccount, mPayState, getPayMethod());
                    }
                }
            }
            @Override
            public void onFailure(Call<AccountBalanceResponse> call, Throwable t) {

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
//                        ToastUtil.showToast(mContext, "支付成功");
                        if (mPayState == Constant.PAY_STATE_GUARANTEE) {
                            PayGuaranteeFee();
                        } else if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT) {
                            PayResultActivity.start(mContext, mCurrentAccount, mPayState, getPayMethod());
                        } else if (mPayState == Constant.PAY_STATE_TOTAL) {
                            // TODO: 2017/9/12 有无发送什么命令
                            PayResultActivity.start(mContext, mCurrentAccount, mPayState, getPayMethod());
                        }
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        ToastUtil.showToast(mContext, "支付失败");
                    }
                    break;
                }

                default:
                    break;
            }
        };
    };

    private void PayGuaranteeFee(){
        PayGuaranteeService payGuaranteeService = ServiceGenerator.createService(PayGuaranteeService.class);
        PayGuaranteeRequest payGuaranteeRequest = new PayGuaranteeRequest(mOrderId);
        Call<PayGuaranteeResponse> call = payGuaranteeService.payGuarantee(payGuaranteeRequest);
        call.enqueue(new Callback<PayGuaranteeResponse>() {
            @Override
            public void onResponse(Call<PayGuaranteeResponse> call, Response<PayGuaranteeResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                    SharedPreferenceUtil.setLong(mContext, Constant.PARKING_START_TIME, response.body().getData().getEstate().getParking().getShare().getStartTime());
//                    SharedPreferenceUtil.setLong(mContext, Constant.PARKING_END_TIME, response.body().getData().getEstate().getParking().getShare().getEndTime());
//                    SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_MAC, response.body().getData().getEstate().getParking().getLockMac());
//                    SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_PWD, response.body().getData().getEstate().getParking().getPassword());
//                    SharedPreferenceUtil.setString(mContext, Constant.RESERVE_GATEWAY_ID, response.body().getData().getEstate().getParking().getGatewayId());
//                    SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, Constant.ORDER_STATE_RESERVED);
//                    SharedPreferenceUtil.setString(mContext, Constant.ESTATE_NAME, response.body().getData().getEstate().getName());
//                    SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LONGITUDE, (float) response.body().getData().getEstate().getX());
//                    SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LATITUDE, (float) response.body().getData().getEstate().getY());
                    PayGuaranteeResponse.DataBean.EstateBean estate = response.body().getData().getEstate();
                    PayGuaranteeResponse.DataBean.EstateBean.ParkingBean parking = estate.getParking();
                    PayGuaranteeResponse.DataBean.EstateBean.ParkingBean.ShareBean share = parking.getShare();
                    OrderUtil.getInstance().setOrderInfo(mContext, mOrderId, Constant.ORDER_STATE_RESERVED,
                            share.getStartTime(),
                            share.getEndTime(),
                            parking.getName(),
                            parking.getLockMac(),
                            parking.getPassword(),
                            parking.getGatewayId(),
                            estate.getName(),
                            estate.getX(),
                            estate.getY());
                    PayResultActivity.start(mContext, mCurrentAccount, mPayState, getPayMethod());
                }
            }

            @Override
            public void onFailure(Call<PayGuaranteeResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    private int getPayMethod(){
        for (int i = 0; i<mSelectedNum.length; i++){
            if (mSelectedNum[i]){
                return i;
            }
        }
        return -1;
    }

}
