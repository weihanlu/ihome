package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceRequest;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceResponse;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeRequest;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeResponse;
import com.qhiehome.ihome.network.service.pay.AccountBalanceService;
import com.qhiehome.ihome.network.service.pay.PayGuaranteeService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.shihao.library.XStatusBarHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayActivity extends AppCompatActivity {

    @BindView(R.id.tb_pay)
    Toolbar mTbPay;
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
        switch (mPayState){
            case Constant.PAY_STATE_ADD_ACCOUNT:    //充值
                mLayoutDetailPay.setVisibility(View.GONE);
                mBtnList.add(mBtnAddBalance1);
                mBtnList.add(mBtnAddBalance2);
                mBtnList.add(mBtnAddBalance3);
                mBtnList.add(mBtnAddBalance4);
                mButtonClicked = 1;
                mBtnAddBalance1.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
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
        mTbPay.setTitle("支付订单");
        mTbPay.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
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
                if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT){//充值
                    if (i == 1){
                        mSelectedNum[1] = true;
                        mSelectedNum[0] = false;
                    }
                    if (i == 0){
                        mSelectedNum[0] = true;
                        mSelectedNum[1] = false;
                    }
                }else {//支付
                    if (i == 2 && mAccountBalance >= mFee){//账户余额足够支付，完全用余额支付
                        mSelectedNum[0] = false;
                        mSelectedNum[1] = false;
                        mSelectedNum[2] = true;
                    }
                    if (i == 2 && mAccountBalance < mFee){//账户余额不够支付，选择是否要使用余额
                        mSelectedNum[2] = !mSelectedNum[2];
                    }
                    if (i != 2 && mAccountBalance >= mFee){//账户余额足够支付，选择支付宝或微信支付
                        mSelectedNum[2] = false;
                        mSelectedNum[1] = false;
                        mSelectedNum[0] = false;
                        mSelectedNum[i] = true;
                    }
                    if (i != 2 && mAccountBalance < mFee){//账户余额不够支付，选择支付方式
                        mSelectedNum[1] = false;
                        mSelectedNum[0] = false;
                        mSelectedNum[i] = true;
                    }
                    if (mSelectedNum[2]){
                        mBtnPay.setText("确认支付：" + String.format(Locale.CHINA, DECIMAL_2, mFee>=mAccountBalance?(mFee - mAccountBalance):0.0) + "元");
                    }else {
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

        if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT){
            int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
            new MaterialDialog.Builder(mContext)
                    .title("确认支付？")
                    .titleColor(red)
                    .content("接口还没好，假装支付一波ㄟ( ▔, ▔ )ㄏ")
                    .contentColor(red)
                    .positiveText("假装支付好了")
                    .positiveColor(red)
                    .negativeText("取消")
                    .canceledOnTouchOutside(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
                            AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.SHA_256), Double.valueOf(mPriceList[mButtonClicked-1]));
                            Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
                            call.enqueue(new Callback<AccountBalanceResponse>() {
                                @Override
                                public void onResponse(Call<AccountBalanceResponse> call, Response<AccountBalanceResponse> response) {
                                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                                        ToastUtil.showToast(mContext, "充值成功");
                                        PayActivity.this.finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<AccountBalanceResponse> call, Throwable t) {
                                    ToastUtil.showToast(mContext, "网络连接异常");
                                }
                            });
                        }
                    })
                    .show();
        }

        // TODO: 2017/8/14 根据选择方式调用支付接口
        if (mPayState == Constant.PAY_STATE_GUARANTEE){
            int red = ContextCompat.getColor(mContext, android.R.color.holo_red_light);
            new MaterialDialog.Builder(mContext)
                    .title("确认支付？")
                    .titleColor(red)
                    .content("接口还没好，假装支付一波ㄟ( ▔, ▔ )ㄏ")
                    .contentColor(red)
                    .positiveText("假装支付好了")
                    .positiveColor(red)
                    .negativeText("取消")
                    .canceledOnTouchOutside(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            PayGuaranteeService payGuaranteeService = ServiceGenerator.createService(PayGuaranteeService.class);
                            PayGuaranteeRequest payGuaranteeRequest = new PayGuaranteeRequest(mOrderId);
                            Call<PayGuaranteeResponse> call = payGuaranteeService.payGuarantee(payGuaranteeRequest);
                            call.enqueue(new Callback<PayGuaranteeResponse>() {
                                @Override
                                public void onResponse(Call<PayGuaranteeResponse> call, Response<PayGuaranteeResponse> response) {
                                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                                        SharedPreferenceUtil.setLong(mContext, Constant.PARKING_START_TIME, response.body().getData().getEstate().getParking().getShare().getStartTime());
                                        SharedPreferenceUtil.setLong(mContext, Constant.PARKING_END_TIME, response.body().getData().getEstate().getParking().getShare().getEndTime());
                                        SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_MAC, response.body().getData().getEstate().getParking().getLockMac());
                                        SharedPreferenceUtil.setString(mContext, Constant.RESERVE_LOCK_PWD, response.body().getData().getEstate().getParking().getPassword());
                                        SharedPreferenceUtil.setString(mContext, Constant.RESERVE_GATEWAY_ID, response.body().getData().getEstate().getParking().getGatewayId());
                                        SharedPreferenceUtil.setInt(mContext, Constant.ORDER_STATE, 31);
                                        SharedPreferenceUtil.setString(mContext, Constant.ESTATE_NAME, response.body().getData().getEstate().getName());
                                        SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LONGITUDE, (float) response.body().getData().getEstate().getX());
                                        SharedPreferenceUtil.setFloat(mContext, Constant.ESTATE_LATITUDE, (float) response.body().getData().getEstate().getY());
                                        Intent intent = new Intent(PayActivity.this, ReserveActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        PayActivity.this.finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<PayGuaranteeResponse> call, Throwable t) {
                                    ToastUtil.showToast(mContext, "网络连接异常");
                                }
                            });

                        }
                    })
                    .show();
        }


    }

    @OnClick({R.id.btn_add_balance_1, R.id.btn_add_balance_2, R.id.btn_add_balance_3, R.id.btn_add_balance_4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add_balance_1:
                if (mButtonClicked != 1){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 1;
                    mBtnAddBalance1.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
            case R.id.btn_add_balance_2:
                if (mButtonClicked != 2){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 2;
                    mBtnAddBalance2.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
            case R.id.btn_add_balance_3:
                if (mButtonClicked != 3){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 3;
                    mBtnAddBalance3.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
                }
                break;
            case R.id.btn_add_balance_4:
                if (mButtonClicked != 4){
                    mBtnList.get(mButtonClicked-1).setTextColor(ContextCompat.getColor(mContext, R.color.black));
                    mButtonClicked = 4;
                    mBtnAddBalance4.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    mBtnPay.setText("确认支付：" + mPriceList[mButtonClicked-1] + "元");
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
                    if (mIsFirstLoad){
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
            if (mPayState == Constant.PAY_STATE_ADD_ACCOUNT){
                return 2;
            }else {
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

    private void getAccountBalance(final PayListAdapter.PayListHolder holder){
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

}
