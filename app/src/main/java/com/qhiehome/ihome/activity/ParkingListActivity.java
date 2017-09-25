package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bigkoo.pickerview.OptionsPickerView;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.persistence.OrderInfoBean;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.model.park.reserve.ReserveRequest;
import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;
import com.qhiehome.ihome.network.model.pay.PayChannel;
import com.qhiehome.ihome.network.model.pay.PayRequest;
import com.qhiehome.ihome.network.model.pay.PayResponse;
import com.qhiehome.ihome.network.model.pay.account.AccountRequest;
import com.qhiehome.ihome.network.model.pay.account.AccountResponse;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceRequest;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceResponse;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeRequest;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeResponse;
import com.qhiehome.ihome.network.service.park.ReserveService;
import com.qhiehome.ihome.network.service.pay.AccountBalanceService;
import com.qhiehome.ihome.network.service.pay.AccountService;
import com.qhiehome.ihome.network.service.pay.PayGuaranteeService;
import com.qhiehome.ihome.network.service.pay.PayService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.PersistenceUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class ParkingListActivity extends BaseActivity {

    private static final int ITEM_NUM = 2;

    private String[] titleArray = {"开始时间", "结束时间"};

    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitle;
    @BindView(R.id.tv_subtitle_toolbar)
    TextView mTvSubTitle;
    @BindView(R.id.tv_parking_guaranteeFee_num)
    TextView mTvParkingGuarfee;
    @BindView(R.id.tv_parking_fee_num)
    TextView mTvParkingFee;
    @BindView(R.id.rl_parking_list)
    RelativeLayout mRlParkingList;
    @BindView(R.id.tv_tip_two)
    TextView mTvTipTwo;
    @BindView(R.id.tv_tip_three)
    TextView mTvTipThree;

    @BindView(R.id.toolbar_center)
    Toolbar mTbParking;
    @BindView(R.id.rv_parking)
    RecyclerView mRvParking;

    private ParkingListAdapter mAdapter;
    private List<Map<String, String>> parking_data = new ArrayList<>();
    private ParkingEmptyResponse.DataBean.EstateBean mEstateBean;
    private Context mContext;

    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH", Locale.CHINA);
    private static final SimpleDateFormat MIN_FORMAT = new SimpleDateFormat("mm", Locale.CHINA);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private static final String INTEGER_2 = "%02d";
    private static final String DECIMAL_2 = "¥%.2f";
    private static final long QUARTER_TIME = 15 * 60 * 1000;
    private static final int LIST_ITEM_COUNT = 4;
    private float mPrice = 0;
    private float mUnitPrice = 0;
    private float mGuaranteeFee = 0;

    private String[] timeArray = new String[2];
    private ArrayList<String> mStartSelectionStr = new ArrayList<>();  //起始时间轴 显示
    private ArrayList<String> mEndSelectionStr = new ArrayList<>();    //终止时间轴 显示

    private ArrayList<Long> mStartSelectionMillis = new ArrayList<>();  //起始时间轴 时间戳
    private ArrayList<Long> mEndSelectionMillis = new ArrayList<>();     //终止时间轴 时间戳

    private int mStartSelectIndex = 0;
    private int mEndSelectIndex = 0;


    private final static int UNPAY_ORDER = 300;
    private final static int RESERVED_ORDER = 301;
    private final static int RESERVE_ERROR = 203;
    private final static int ERROR_CODE_INSUFFICIENT = 302;


    //configuration parameter
    private int mMinSharingTime;
    private int mMinChargeTime;
    private int mFreeCancellationTime;

    private MaterialDialog mShowFeeIntroDialog;

    private String mFeeIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_list);
        ButterKnife.bind(this);
        mContext = this;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mEstateBean = (ParkingEmptyResponse.DataBean.EstateBean) bundle.getSerializable("estate");
        mMinSharingTime = bundle.getInt(Constant.MIN_SHARING_PERIOD);
        mMinChargeTime = bundle.getInt(Constant.MIN_CHARGING_PERIOD);
        mFreeCancellationTime = bundle.getInt(Constant.FREE_CANCELLATION_TIME);
        initView();
        mUnitPrice = (float) mEstateBean.getUnitPrice();
        mGuaranteeFee = (float) mEstateBean.getGuaranteeFee();
        mPrice = mUnitPrice / 60 * mMinSharingTime;

        mTvParkingGuarfee.setText(String.format(Locale.CHINA, DECIMAL_2, mGuaranteeFee));
        updateParkingFee();

        initIntroString();
    }

    private void initIntroString() {
        String tipTwo = "2、%d分钟未入场，订单自动取消";
        mTvTipTwo.setText(String.format(Locale.CHINA, tipTwo, mFreeCancellationTime));

        SpannableString sp = new SpannableString("3、了解更多计费信息请阅读《担保费与计费说明》");
        sp.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.theme_start_color)),
                13, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvTipThree.setText(sp);

        StringBuilder stringBuilder = new StringBuilder();
        String[] mFeeIntroArray = {
                "预约开始时间后%d分钟内可以免费取消订单，担保费退还至账户余额；",
                "车辆进场开始计费，担保费可抵扣停车费；",
                "如未按时进场车位最多保留%d分钟，超过%d分钟，订单自动取消，担保费不退还；",
                "停车时间不足%d分钟按%d分钟计费；",
                "超过预约时间未离开，超出部分将收取双倍停车费。"
        };
        mFeeIntroArray[0] = String.format(Locale.CHINA, mFeeIntroArray[0], mFreeCancellationTime);
        mFeeIntroArray[2] = String.format(Locale.CHINA, mFeeIntroArray[2], mFreeCancellationTime, mFreeCancellationTime);
        mFeeIntroArray[3] = String.format(Locale.CHINA, mFeeIntroArray[3], mMinChargeTime, mMinChargeTime);
        for (int i = 0; i < mFeeIntroArray.length; i++) {
            stringBuilder.append(i + 1).append("、").append(mFeeIntroArray[i]).append("\n");
        }
        mFeeIntro = stringBuilder.toString();
    }

    private void updateParkingFee() {
        if (mTvParkingFee != null) {
            mTvParkingFee.setText(String.format(Locale.CHINA, DECIMAL_2, mPrice));
        }
    }

    private void initView() {
        initToolbar();
        initTimePickerData();
        initRecyclerView();
    }

    @OnClick(R.id.iv_time_axis)
    public void onTimeAxisClick() {
        ShowAllParking();
    }

    private void initToolbar() {
        setSupportActionBar(mTbParking);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTvTitle.setText(mEstateBean.getName());
        mTvSubTitle.setText(String.format(Locale.CHINA, DECIMAL_2, (float) mEstateBean.getUnitPrice()) + "元/小时");
        mTbParking.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initTimePickerData() {

        mStartSelectionStr.clear();
        mEndSelectionStr.clear();
        mStartSelectionMillis.clear();
        mEndSelectionMillis.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int m = calendar.get(Calendar.MINUTE);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m_total = m + h * 60;
        int m_start = m_total - (m_total % mMinChargeTime) + mMinChargeTime;
        int m_end = m_start + mMinSharingTime;
        while (m_start <= 24 * 60 - mMinSharingTime) {

            m = m_start % 60;
            h = m_start / 60;
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            mStartSelectionMillis.add(calendar.getTimeInMillis());
            mStartSelectionStr.add(TIME_FORMAT.format(calendar.getTimeInMillis()));

            m_start += mMinChargeTime;
        }

        while (m_end <= 24 * 60) {

            m = m_end % 60;
            h = m_end / 60;
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            mEndSelectionMillis.add(calendar.getTimeInMillis());
            mEndSelectionStr.add(TIME_FORMAT.format(calendar.getTimeInMillis()));

            m_end += mMinChargeTime;
        }

        for (int i = 0; i < mStartSelectIndex; i++) {
            mEndSelectionStr.remove(0);
            mEndSelectionMillis.remove(0);
            mEndSelectIndex = 0;
        }
        updateTimeArray();
    }


    public void onItemClick(View view) {
        int position = mRvParking.getChildAdapterPosition(view);
        if (position == 0) {
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    mStartSelectIndex = options1;
                    updateTimeArray();
                    initTimePickerData();
                    mPrice = mUnitPrice * (mEndSelectionMillis.get(mEndSelectIndex) - mStartSelectionMillis.get(mStartSelectIndex)) / 1000 / 60 / 60;
                    updateParkingFee();
                    mAdapter.notifyDataSetChanged();
                }
            })
                    .setBgColor(Color.WHITE)
                    .setTitleBgColor(Color.WHITE)
                    .setTitleText("选择时间")
                    .setTitleColor(ContextCompat.getColor(mContext, R.color.major_text))
                    .setTextColorCenter(ContextCompat.getColor(mContext, R.color.major_text))
                    .setSubmitColor(ContextCompat.getColor(mContext, R.color.theme_start_color))
                    .setDividerColor(ContextCompat.getColor(mContext, R.color.theme_start_color))//设置分割线的颜色
                    .setCancelColor(R.color.button_bord)
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setSelectOptions(mStartSelectIndex)//默认选中项
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                    .setLabels("省", "市", "区")
                    .setBackgroundId(0x66000000) //设置外部遮罩颜色
                    .build();
            //pvOptions.setSelectOptions(1,1);
            pvOptions.setPicker(mStartSelectionStr);//二级选择器
            pvOptions.show();
        }
        if (position == 1) {
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    //返回的分别是三个级别的选中位置
                    mEndSelectIndex = options1;
                    updateTimeArray();
                    mPrice = mUnitPrice * (mEndSelectionMillis.get(mEndSelectIndex) - mStartSelectionMillis.get(mStartSelectIndex)) / 1000 / 60 / 60;
                    updateParkingFee();
                    mAdapter.notifyDataSetChanged();
                }
            })
                    .setBgColor(Color.WHITE)
                    .setTitleBgColor(Color.WHITE)
                    .setTitleText("选择时间")
                    .setTitleColor(ContextCompat.getColor(mContext, R.color.major_text))
                    .setTextColorCenter(ContextCompat.getColor(mContext, R.color.major_text))
                    .setSubmitColor(ContextCompat.getColor(mContext, R.color.theme_start_color))
                    .setDividerColor(ContextCompat.getColor(mContext, R.color.theme_start_color))//设置分割线的颜色
                    .setCancelColor(R.color.button_bord)
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setSelectOptions(mEndSelectIndex)//默认选中项
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                    .setLabels("省", "市", "区")
                    .setBackgroundId(0x66000000) //设置外部遮罩颜色
                    .build();
            //pvOptions.setSelectOptions(1,1);
            pvOptions.setPicker(mEndSelectionStr);//二级选择器
            pvOptions.show();
        }
    }

    private void updateTimeArray() {
        timeArray[0] = mStartSelectionStr.get(mStartSelectIndex);
        timeArray[1] = mEndSelectionStr.get(mEndSelectIndex);
    }


    private void initRecyclerView() {
        mRvParking.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ParkingListAdapter();
        mRvParking.setAdapter(mAdapter);
        Context context = ParkingListActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        mRvParking.addItemDecoration(did);
    }

    public class ParkingListAdapter extends RecyclerView.Adapter<ParkingListAdapter.ParkingHolder> {


        @Override
        public ParkingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ParkingHolder(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list, parent, false));
        }

        @Override
        public void onBindViewHolder(ParkingHolder holder, int position) {
            holder.tv_title.setText(titleArray[position]);
            holder.tv_content.setText(timeArray[position]);
        }

        @Override
        public int getItemCount() {
            return ITEM_NUM;
        }

        class ParkingHolder extends RecyclerView.ViewHolder {
            TextView tv_title;
            TextView tv_content;

            private ParkingHolder(View view) {
                super(view);
                tv_title = (TextView) view.findViewById(R.id.tv_parking_title);
                tv_content = (TextView) view.findViewById(R.id.tv_parking_content);
            }
        }
    }

    @OnClick(R.id.tv_tip_three)
    public void onShowFeeIntro() {
        showFeeIntroDialog();
    }

    private void showFeeIntroDialog() {
        if (mShowFeeIntroDialog == null) {
            mShowFeeIntroDialog = new MaterialDialog.Builder(this)
                            .title("担保费与计费说明")
                            .titleColor(ContextCompat.getColor(mContext, R.color.theme_start_color))
                            .content(mFeeIntro)
                            .contentColor(ContextCompat.getColor(mContext, R.color.major_text))
                            .positiveText("确定")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .canceledOnTouchOutside(true).build();
        }
        mShowFeeIntroDialog.show();
    }

    /*********预约按钮*********/
    @OnClick(R.id.tv_parking_reserve)
    public void onParkingReserve() {
        String phoneNum = SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, "");
        if (TextUtils.isEmpty(phoneNum)) {
            new MaterialDialog.Builder(mContext)
                    .title("去登录")
                    .content("确定登录吗？")
                    .positiveText("登录")
                    .negativeText("取消")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            LoginActivity.start(mContext);
                        }
                    })
                    .show();
        } else {
            ReserveService reserveService = ServiceGenerator.createService(ReserveService.class);
            final ReserveRequest reserveRequest = new ReserveRequest(EncryptUtil.rsaEncrypt(PersistenceUtil.getUserInfo(this).getPhoneNum()), mEstateBean.getId(), mStartSelectionMillis.get(mStartSelectIndex), mEndSelectionMillis.get(mEndSelectIndex));
            Call<ReserveResponse> call = reserveService.reserve(reserveRequest);
            call.enqueue(new Callback<ReserveResponse>() {
                @Override
                public void onResponse(Call<ReserveResponse> call, Response<ReserveResponse> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        payGuarFeeWithWallet(response.body().getData().getOrder().getId());
                    } else if (response.body().getErrcode() == UNPAY_ORDER) {
                        new MaterialDialog.Builder(mContext)
                                .title("预约失败")
                                .content("您有尚未支付的订单，请先完成支付")
                                .positiveText("去支付")
                                .negativeText("取消")
                                .canceledOnTouchOutside(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Intent intent = new Intent(ParkingListActivity.this, ReserveActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    } else if (response.body().getErrcode() == RESERVED_ORDER) {
                        new MaterialDialog.Builder(mContext)
                                .title("预约失败")
                                .content("您已有预约的订单")
                                .positiveText("去停车")
                                .negativeText("取消")
                                .canceledOnTouchOutside(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        Intent intent = new Intent(ParkingListActivity.this, ReserveActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    } else if (response.body().getErrcode() == RESERVE_ERROR) {
                        new MaterialDialog.Builder(mContext)
                                .title("预约失败")
                                .content("没有满足条件的车位")
                                .positiveText("查看全部车位")
                                .negativeText("取消")
                                .canceledOnTouchOutside(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ShowAllParking();
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ReserveResponse> call, Throwable t) {
                    ToastUtil.showToast(ParkingListActivity.this, "网络连接异常");
                }
            });
        }

    }

    private void ShowAllParking() {
        Intent intent = new Intent(ParkingListActivity.this, ParkingTimelineActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("estate", mEstateBean);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void payGuarFeeWithWallet(final int orderId) {

        PayService payService = ServiceGenerator.createService(PayService.class);
        PayRequest payRequest = new PayRequest(orderId, PayChannel.WALLET.ordinal(), mGuaranteeFee);
        Call<PayResponse> call = payService.pay(payRequest);
        call.enqueue(new Callback<PayResponse>() {
            @Override
            public void onResponse(Call<PayResponse> call, Response<PayResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    PayGuaranteeFee(orderId);
                }else if (response.body().getErrcode() == ERROR_CODE_INSUFFICIENT){
                    Intent intent = new Intent(ParkingListActivity.this, PayActivity.class);
                    intent.putExtra("fee", mGuaranteeFee);
                    intent.putExtra("payState", Constant.PAY_STATE_GUARANTEE);
                    intent.putExtra("orderId", orderId);
                    SharedPreferenceUtil.setLong(mContext, Constant.ORDER_CREATE_TIME, System.currentTimeMillis());
                    SharedPreferenceUtil.setInt(mContext, Constant.FREE_CANCELLATION_TIME, mFreeCancellationTime);
                    startActivity(intent);
                    ToastUtil.showToast(mContext, "预约成功");
                }
            }
            @Override
            public void onFailure(Call<PayResponse> call, Throwable t) {

            }
        });
    }

    private void PayGuaranteeFee(final int orderId) {
        PayGuaranteeService payGuaranteeService = ServiceGenerator.createService(PayGuaranteeService.class);
        PayGuaranteeRequest payGuaranteeRequest = new PayGuaranteeRequest(orderId);
        Call<PayGuaranteeResponse> call = payGuaranteeService.payGuarantee(payGuaranteeRequest);
        call.enqueue(new Callback<PayGuaranteeResponse>() {
            @Override
            public void onResponse(Call<PayGuaranteeResponse> call, Response<PayGuaranteeResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    PayGuaranteeResponse.DataBean.EstateBean estateBean = response.body().getData().getEstate();
                    PayGuaranteeResponse.DataBean.EstateBean.ParkingBean parkingBean = estateBean.getParking();
                    PayGuaranteeResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean = parkingBean.getShare();

                    OrderInfoBean orderInfoBean = new OrderInfoBean(orderId, Constant.ORDER_STATE_RESERVED, shareBean.getStartTime(),
                                                    shareBean.getEndTime(), parkingBean.getName(), parkingBean.getLockMac(),
                                                    parkingBean.getPassword(), parkingBean.getGatewayId(), estateBean.getName(),
                                                    (float) estateBean.getX(), (float)estateBean.getY());

                    PersistenceUtil.setOrderInfo(mContext, orderInfoBean);
                    Intent intent = new Intent(ParkingListActivity.this, ReserveActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PayGuaranteeResponse> call, Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }
}