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
import android.view.Menu;
import android.view.MenuItem;
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
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.model.park.reserve.ReserveRequest;
import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceRequest;
import com.qhiehome.ihome.network.model.pay.accountbalance.AccountBalanceResponse;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeRequest;
import com.qhiehome.ihome.network.model.pay.guarantee.PayGuaranteeResponse;
import com.qhiehome.ihome.network.service.park.ReserveService;
import com.qhiehome.ihome.network.service.pay.AccountBalanceService;
import com.qhiehome.ihome.network.service.pay.PayGuaranteeService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.OrderUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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


    //configuration parameter
    private int MIN_SHARING_PERIOD;
    private int MIN_CHARGING_PERIOD;
    private int FREE_CANCELLATION_TIME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_list);
        ButterKnife.bind(this);
        mContext = this;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mEstateBean = (ParkingEmptyResponse.DataBean.EstateBean) bundle.getSerializable("estate");
        MIN_SHARING_PERIOD = bundle.getInt(Constant.MIN_SHARING_PERIOD);
        MIN_CHARGING_PERIOD = bundle.getInt(Constant.MIN_CHARGING_PERIOD);
        FREE_CANCELLATION_TIME = bundle.getInt(Constant.FREE_CANCELLATION_TIME);
        initView();
        mUnitPrice = (float) mEstateBean.getUnitPrice();
        mGuaranteeFee = (float) mEstateBean.getGuaranteeFee();
        mPrice = mUnitPrice / 60 * MIN_SHARING_PERIOD;

        mTvParkingGuarfee.setText(String.format(Locale.CHINA, DECIMAL_2, mGuaranteeFee));
        updateParkingFee();
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

        SpannableString sp = new SpannableString("3.了解更多计费信息请阅读《担保费与计费说明》");
        sp.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.theme_start_color)),
                13, 23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvTipThree.setText(sp);
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
        int m_start = m_total - (m_total % MIN_CHARGING_PERIOD) + MIN_CHARGING_PERIOD;
        int m_end = m_start + MIN_SHARING_PERIOD;
        while (m_start <= 24 * 60 - MIN_SHARING_PERIOD) {

            m = m_start % 60;
            h = m_start / 60;
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            mStartSelectionMillis.add(calendar.getTimeInMillis());
            mStartSelectionStr.add(TIME_FORMAT.format(calendar.getTimeInMillis()));

            m_start += MIN_CHARGING_PERIOD;
        }

        while (m_end <= 24 * 60) {

            m = m_end % 60;
            h = m_end / 60;
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            mEndSelectionMillis.add(calendar.getTimeInMillis());
            mEndSelectionStr.add(TIME_FORMAT.format(calendar.getTimeInMillis()));

            m_end += MIN_CHARGING_PERIOD;
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
                    .setTitleText("选择时间")
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.GREEN)//设置分割线的颜色
                    .setSelectOptions(mStartSelectIndex)//默认选中项
                    .setBgColor(Color.BLACK)
                    .setTitleBgColor(Color.DKGRAY)
                    .setTitleColor(Color.LTGRAY)
                    .setCancelColor(Color.YELLOW)
                    .setSubmitColor(Color.YELLOW)
                    .setTextColorCenter(Color.LTGRAY)
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
                    .setTitleText("选择时间")
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.GREEN)//设置分割线的颜色
                    .setSelectOptions(mEndSelectIndex)//默认选中项
                    .setBgColor(Color.BLACK)
                    .setTitleBgColor(Color.DKGRAY)
                    .setTitleColor(Color.LTGRAY)
                    .setCancelColor(Color.YELLOW)
                    .setSubmitColor(Color.YELLOW)
                    .setTextColorCenter(Color.LTGRAY)
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    //.setLabels("省", "市", "区")
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

    /*********预约按钮*********/
    @OnClick(R.id.tv_parking_reserve)
    public void onViewClicked() {
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
            final ReserveRequest reserveRequest = new ReserveRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM), EncryptUtil.ALGO.RSA), mEstateBean.getId(), mStartSelectionMillis.get(mStartSelectIndex), mEndSelectionMillis.get(mEndSelectIndex));
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

    private void payGuarFeeWithWallet(final int orderId){
        AccountBalanceService accountBalanceService = ServiceGenerator.createService(AccountBalanceService.class);
        AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, ""), EncryptUtil.ALGO.RSA), -mGuaranteeFee, orderId);
        Call<AccountBalanceResponse> call = accountBalanceService.account(accountBalanceRequest);
        call.enqueue(new Callback<AccountBalanceResponse>() {
            @Override
            public void onResponse(Call<AccountBalanceResponse> call, Response<AccountBalanceResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    PayGuaranteeFee(orderId);
                }else {
                    Intent intent = new Intent(ParkingListActivity.this, PayActivity.class);
                    intent.putExtra("fee", mGuaranteeFee);
                    intent.putExtra("payState", Constant.PAY_STATE_GUARANTEE);
                    intent.putExtra("orderId", orderId);
                    SharedPreferenceUtil.setLong(mContext, Constant.ORDER_CREATE_TIME, System.currentTimeMillis());
                    SharedPreferenceUtil.setInt(mContext, Constant.FREE_CANCELLATION_TIME, FREE_CANCELLATION_TIME);
                    startActivity(intent);
                    ToastUtil.showToast(mContext, "预约成功");
                }
            }
            @Override
            public void onFailure(Call<AccountBalanceResponse> call, Throwable t) {

            }
        });
    }

    private void PayGuaranteeFee(final int orderId){
        PayGuaranteeService payGuaranteeService = ServiceGenerator.createService(PayGuaranteeService.class);
        PayGuaranteeRequest payGuaranteeRequest = new PayGuaranteeRequest(orderId);
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
                    OrderUtil.getInstance().setOrderInfo(mContext, orderId, Constant.ORDER_STATE_RESERVED,
                            response.body().getData().getEstate().getParking().getShare().getStartTime(),
                            response.body().getData().getEstate().getParking().getShare().getEndTime(),
                            response.body().getData().getEstate().getParking().getLockMac(),
                            response.body().getData().getEstate().getParking().getPassword(),
                            response.body().getData().getEstate().getParking().getGatewayId(),
                            response.body().getData().getEstate().getName(),
                            response.body().getData().getEstate().getX(),
                            response.body().getData().getEstate().getY());
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