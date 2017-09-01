//package com.qhiehome.ihome.activity;
//
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.ActionBar;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.Toolbar;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.afollestad.materialdialogs.DialogAction;
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.bigkoo.pickerview.OptionsPickerView;
//import com.qhiehome.ihome.R;
//import com.qhiehome.ihome.network.ServiceGenerator;
//import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
//import com.qhiehome.ihome.network.model.park.reserve.ReserveRequest;
//import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;
//import com.qhiehome.ihome.network.service.park.ReserveService;
//import com.qhiehome.ihome.util.CommonUtil;
//import com.qhiehome.ihome.util.Constant;
//import com.qhiehome.ihome.util.EncryptUtil;
//import com.qhiehome.ihome.util.SharedPreferenceUtil;
//import com.qhiehome.ihome.util.TimeUtil;
//import com.qhiehome.ihome.util.ToastUtil;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class ParkingListActivity extends BaseActivity {
//
//    @BindView(R.id.tv_parking_guaranteeFee_num)
//    TextView mTvParkingGuarfee;
//    @BindView(R.id.btn_parking_reserve)
//    Button mBtnParkingReserve;
//    @BindView(R.id.tv_parking_name_parkinglist)
//    TextView mTvParkingName;
//
//    private static enum ITEM_TYPE {
//        ITEM_TYPE_BTN,
//        ITEM_TYPE_NO_BTN
//    }
//
//    @BindView(R.id.tb_parking)
//    Toolbar mTbParking;
//    @BindView(R.id.rv_parking)
//    RecyclerView mRvParking;
//    private ParkingListAdapter mAdapter;
//    private List<Map<String, String>> parking_data = new ArrayList<>();
//    private ParkingEmptyResponse.DataBean.EstateBean mEstateBean;
//    private Context mContext;
//
//    private static final SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH", Locale.CHINA);
//    private static final SimpleDateFormat MIN_FORMAT = new SimpleDateFormat("mm", Locale.CHINA);
//    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.CHINA);
//    private static final String INTEGER_2 = "%02d";
//    private static final String DECIMAL_2 = "%.2f";
//    private static final long QUARTER_TIME = 15 * 60 * 1000;
//    private static final int LIST_PARKING_INFO = 0;
//    private static final int LIST_START_TIME = 1;
//    private static final int LIST_END_TIME = 2;
//    private static final int LIST_TOTAL_FEE = 3;
//    private static final int LIST_ITEM_COUNT = 4;
//    private float mPrice = 0;
//    private float mUnitPrice = 0;
//    private float mGuaranteeFee = 0;
//
//
//
//
//
//    private final static int UNPAY_ORDER = 300;
//    private final static int RESERVED_ORDER = 301;
//    private final static int RESERVE_ERROR = 203;
//
//
//    //configuration parameter
//    public int MIN_SHARING_PERIOD = 30;
//    public int MIN_CHARGING_PERIOD = 15;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        CommonUtil.setStatusBarGradient(this);
//        setContentView(R.layout.activity_parking_list);
//        ButterKnife.bind(this);
//        mContext = this;
//        Intent intent = this.getIntent();
//        Bundle bundle = intent.getExtras();
//        mEstateBean = (ParkingEmptyResponse.DataBean.EstateBean) bundle.getSerializable("estate");
//        initToolbar();
////        initData();
//        initTimePickerData();
//        initRecyclerView();
//        mUnitPrice = (float) mEstateBean.getUnitPrice();
//        mGuaranteeFee = (float) mEstateBean.getGuaranteeFee();
//        mTvParkingGuarfee.setText(String.format(Locale.CHINA, DECIMAL_2, mGuaranteeFee));
//        mPrice = mUnitPrice / 60 * MIN_SHARING_PERIOD;
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_parking_list, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_detail_parking_list) {
//            ShowAllParking();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void initToolbar() {
//        setSupportActionBar(mTbParking);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowTitleEnabled(false);
//        }
//        mTvParkingName.setText(mEstateBean.getName());
//        mTbParking.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
//        mTbParking.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//    }
//
//    private void initTimePickerData() {
//
//    }
//
//
//
//    public void onItemClick(View view) {
//        int position = mRvParking.getChildAdapterPosition(view);
//        if (position == 1) {
//            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
//                @Override
//                public void onOptionsSelect(int options1, int options2, int options3, View v) {
//                    // TODO: 2017/9/1 根据选择改变显示内容
//                    mAdapter.notifyDataSetChanged();
//                }
//            })
//                    .setTitleText("选择时间")
//                    .setContentTextSize(20)//设置滚轮文字大小
//                    .setDividerColor(Color.GREEN)//设置分割线的颜色
//                    .setSelectOptions(mStartHourSelection, mStartMinSelection)//默认选中项
//                    .setBgColor(Color.BLACK)
//                    .setTitleBgColor(Color.DKGRAY)
//                    .setTitleColor(Color.LTGRAY)
//                    .setCancelColor(Color.YELLOW)
//                    .setSubmitColor(Color.YELLOW)
//                    .setTextColorCenter(Color.LTGRAY)
//                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
////                    .setLabels("省", "市", "区")
//                    .setBackgroundId(0x66000000) //设置外部遮罩颜色
//                    .build();
//            //pvOptions.setSelectOptions(1,1);
//            pvOptions.setPicker(mStartHours, mStartMinites);//二级选择器
//            pvOptions.show();
//        }
//        if (position == 2) {
//            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
//                @Override
//                public void onOptionsSelect(int options1, int options2, int options3, View v) {
//                    //返回的分别是三个级别的选中位置
//
//                    // TODO: 2017/9/1 根据选择改变显示内容
//                    mAdapter.notifyDataSetChanged();
//                }
//            })
//                    .setTitleText("选择时间")
//                    .setContentTextSize(20)//设置滚轮文字大小
//                    .setDividerColor(Color.GREEN)//设置分割线的颜色
//                    .setSelectOptions(mEndHourSelection, mEndMinSelection)//默认选中项
//                    .setBgColor(Color.BLACK)
//                    .setTitleBgColor(Color.DKGRAY)
//                    .setTitleColor(Color.LTGRAY)
//                    .setCancelColor(Color.YELLOW)
//                    .setSubmitColor(Color.YELLOW)
//                    .setTextColorCenter(Color.LTGRAY)
//                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//                    //.setLabels("省", "市", "区")
//                    .setBackgroundId(0x66000000) //设置外部遮罩颜色
//                    .build();
//            //pvOptions.setSelectOptions(1,1);
//            pvOptions.setPicker(mStartTimes, mEndTimes);//二级选择器
//            pvOptions.show();
//        }
//    }
//
//
//    private void initRecyclerView() {
//        mRvParking.setLayoutManager(new LinearLayoutManager(this));
//        mAdapter = new ParkingListAdapter();
//        mRvParking.setAdapter(mAdapter);
//        Context context = ParkingListActivity.this;
//        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
//        mRvParking.addItemDecoration(did);
//    }
//
//
//    public class ParkingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            if (viewType == ITEM_TYPE.ITEM_TYPE_BTN.ordinal()) {
//                ParkingHolder viewHolder = new ParkingHolder(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list, parent, false));
//                return viewHolder;
//            }
//            if (viewType == ITEM_TYPE.ITEM_TYPE_NO_BTN.ordinal()) {
//                ParkingHolderNoBtn viewHolder = new ParkingHolderNoBtn(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list_nobtn, parent, false));
//                return viewHolder;
//            }
//            return null;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if (position == LIST_START_TIME || position == LIST_END_TIME) {
//                return ITEM_TYPE.ITEM_TYPE_BTN.ordinal();
//            } else {
//                return ITEM_TYPE.ITEM_TYPE_NO_BTN.ordinal();
//            }
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            if (holder instanceof ParkingHolderNoBtn) {
//                if (position == LIST_PARKING_INFO) {
//                    ((ParkingHolderNoBtn) holder).tv_title.setText(mEstateBean.getName());
//                    ((ParkingHolderNoBtn) holder).tv_content.setText("￥" + String.format(DECIMAL_2, (float) mEstateBean.getUnitPrice()) + "/小时");
//                }
//                if (position == LIST_TOTAL_FEE) {
//                    ((ParkingHolderNoBtn) holder).tv_title.setText("预计停车费");
//                    ((ParkingHolderNoBtn) holder).tv_content.setText("￥" + String.format(DECIMAL_2, mPrice));
//                }
//            } else if (holder instanceof ParkingHolder) {
//                if (position == LIST_START_TIME) {
//                    ((ParkingHolder) holder).tv_title.setText("开始时间");
//                    ((ParkingHolder) holder).tv_content.setText(mStartTime);
//                }
//                if (position == LIST_END_TIME) {
//                    ((ParkingHolder) holder).tv_title.setText("结束时间");
//                    ((ParkingHolder) holder).tv_content.setText(mEndTime);
//                }
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return LIST_ITEM_COUNT;
//        }
//
//        class ParkingHolder extends RecyclerView.ViewHolder {
//            private TextView tv_title;
//            private TextView tv_content;
//            private ImageView iv_arrow;
//
//            private ParkingHolder(View view) {
//                super(view);
//                tv_title = (TextView) view.findViewById(R.id.tv_parking_title);
//                tv_content = (TextView) view.findViewById(R.id.tv_parking_content);
//                iv_arrow = (ImageView) view.findViewById(R.id.iv_parking_arrow);
//            }
//        }
//
//        class ParkingHolderNoBtn extends RecyclerView.ViewHolder {
//            private TextView tv_title;
//            private TextView tv_content;
//
//            private ParkingHolderNoBtn(View view) {
//                super(view);
//                tv_title = (TextView) view.findViewById(R.id.tv_parking_title_nobtn);
//                tv_content = (TextView) view.findViewById(R.id.tv_parking_content_nobtn);
//            }
//        }
//    }
//
//    /*********预约按钮*********/
//    @OnClick(R.id.btn_parking_reserve)
//    public void onViewClicked() {
//        String phoneNum = SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, "");
//        if (TextUtils.isEmpty(phoneNum)) {
//            new MaterialDialog.Builder(mContext)
//                    .title("去登录")
//                    .content("确定登录吗？")
//                    .positiveText("登录")
//                    .negativeText("取消")
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            LoginActivity.start(mContext);
//                        }
//                    })
//                    .show();
//        } else {
//            ReserveService reserveService = ServiceGenerator.createService(ReserveService.class);
//            final ReserveRequest reserveRequest = new ReserveRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM), EncryptUtil.ALGO.SHA_256), mEstateBean.getId(), mStartTimeMillis, mEndTimeMillis);
//            Call<ReserveResponse> call = reserveService.reserve(reserveRequest);
//            call.enqueue(new Callback<ReserveResponse>() {
//                @Override
//                public void onResponse(Call<ReserveResponse> call, Response<ReserveResponse> response) {
//                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
//                        Intent intent = new Intent(ParkingListActivity.this, PayActivity.class);
//                        intent.putExtra("fee", mGuaranteeFee);
//                        intent.putExtra("payState", Constant.PAY_STATE_GUARANTEE);
//                        intent.putExtra("orderId", response.body().getData().getOrder().getId());
//                        SharedPreferenceUtil.setLong(mContext, Constant.ORDER_CREATE_TIME, System.currentTimeMillis());
//                        startActivity(intent);
//                        ToastUtil.showToast(mContext, "预约成功");
//                    } else if (response.body().getErrcode() == UNPAY_ORDER) {
//                        new MaterialDialog.Builder(mContext)
//                                .title("预约失败")
//                                .content("您有尚未支付的订单，请先完成支付")
//                                .positiveText("去支付")
//                                .negativeText("取消")
//                                .canceledOnTouchOutside(false)
//                                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                                    @Override
//                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                        Intent intent = new Intent(ParkingListActivity.this, ReserveActivity.class);
//                                        startActivity(intent);
//                                    }
//                                })
//                                .show();
//                    } else if (response.body().getErrcode() == RESERVED_ORDER) {
//                        new MaterialDialog.Builder(mContext)
//                                .title("预约失败")
//                                .content("您已有预约的订单")
//                                .positiveText("去停车")
//                                .negativeText("取消")
//                                .canceledOnTouchOutside(false)
//                                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                                    @Override
//                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                        Intent intent = new Intent(ParkingListActivity.this, ReserveActivity.class);
//                                        startActivity(intent);
//                                    }
//                                })
//                                .show();
//                    } else if (response.body().getErrcode() == RESERVE_ERROR) {
//                        new MaterialDialog.Builder(mContext)
//                                .title("预约失败")
//                                .content("没有满足条件的车位")
//                                .positiveText("查看全部车位")
//                                .negativeText("取消")
//                                .canceledOnTouchOutside(false)
//                                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                                    @Override
//                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                        ShowAllParking();
//                                    }
//                                })
//                                .show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ReserveResponse> call, Throwable t) {
//                    ToastUtil.showToast(ParkingListActivity.this, "网络连接异常");
//                }
//            });
//        }
//
//    }
//
//    private void ShowAllParking() {
//        Intent intent = new Intent(ParkingListActivity.this, ParkingTimelineActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("estate", mEstateBean);
//        intent.putExtras(bundle);
//        startActivity(intent);
//    }
//
//
//}