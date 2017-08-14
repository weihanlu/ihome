package com.qhiehome.ihome.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bigkoo.pickerview.OptionsPickerView;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.park.reserve.ReserveRequest;
import com.qhiehome.ihome.network.model.park.reserve.ReserveResponse;
import com.qhiehome.ihome.network.service.park.ReserveService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingListActivity extends BaseActivity {

    @BindView(R.id.tv_parking_guarfee)
    TextView mTvParkingGuarfee;
    @BindView(R.id.btn_parking_reserve)
    Button mBtnParkingReserve;

    private static enum ITEM_TYPE {
        ITEM_TYPE_BTN,
        ITEM_TYPE_NO_BTN
    }

    @BindView(R.id.tb_parking)
    Toolbar mTbParking;
    @BindView(R.id.rv_parking)
    RecyclerView mRvParking;
    private ParkingListAdapter mAdapter;
    private List<Map<String, String>> parking_data = new ArrayList<>();
    private ParkingResponse.DataBean.EstateBean mEstateBean;
    private Context mContext;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final String INTEGER_2 = "%02d";
    private static final String DECIMAL_2 = "%.2f";
    private static final long QUARTER_TIME = 15 * 60 * 1000;
    private static final int LIST_PARKING_INFO = 0;
    private static final int LIST_START_TIME = 1;
    private static final int LIST_END_TIME = 2;
    private static final int LIST_TOTAL_FEE = 3;
    private static final int LIST_ITEM_COUNT = 4;
    private float mPrice = 0;
    private float mUnitPrice = 0;
    private String mStartTime;
    private String mEndTime;
    private int mStartHourSelection = 0;
    private int mStartMinSelection = 0;
    private int mEndHourSelection = 0;
    private int mEndMinSelection = 0;
    private long mStartTimeMillis = 0;
    private long mEndTimeMillis = 0;
    private ArrayList<ArrayList<String>> mStartMinites = new ArrayList<>();
    private ArrayList<String> mStartHours = new ArrayList<>();
    private ArrayList<String> mMinute = new ArrayList<>();

    private ArrayList<ArrayList<String>> mEndMinites = new ArrayList<>();
    private ArrayList<String> mEndHours = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_list);
        ButterKnife.bind(this);
        mContext = this;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mEstateBean = (ParkingResponse.DataBean.EstateBean) bundle.getSerializable("estate");
        initToolbar();
        initData();
        initRecyclerView();
        mUnitPrice = (float) mEstateBean.getUnitPrice();
        mTvParkingGuarfee.setText("担保费：￥" + String.format(DECIMAL_2, (float) mEstateBean.getGuaranteeFee()));
        mPrice = mUnitPrice/4;
    }

    private void initToolbar() {
        setSupportActionBar(mTbParking);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbParking.setTitle(mEstateBean.getName());
        mTbParking.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbParking.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initData() {
        mStartTime = TIME_FORMAT.format(TimeUtil.getInstance().millis2Date(System.currentTimeMillis() + QUARTER_TIME));
        //初始化开始时间数据源
        Calendar calendar = Calendar.getInstance();
        //calendar.setTime(TimeUtil.getInstance().millis2Date(System.currentTimeMillis() + QUARTER_TIME));
        calendar.add(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        mStartTimeMillis = calendar.getTimeInMillis();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        ArrayList<String> minute = new ArrayList<>();

        minute.add(String.format(INTEGER_2, min));

        if (min > 0 && min < 15) {
            minute.add("15");
            minute.add("30");
            minute.add("45");
        } else if (min >= 15 && min < 30) {
            minute.add("30");
            minute.add("45");
        } else if (min >= 30 && min < 45) {
            minute.add("45");
        } else if (min >= 45 && min < 60) {
        }
        mStartMinites.add(minute);
        mStartHours.add(String.valueOf(hour));
        hour++;
        mMinute.add("00");
        mMinute.add("15");
        mMinute.add("30");
        mMinute.add("45");
        for (int h = hour; h < 24; h++) {
            mStartMinites.add(mMinute);
            mStartHours.add(String.valueOf(h));
        }
        //初始化结束时间数据源
        initEndTimeDataSourse(-1, -1, true);

    }

    private void initEndTimeDataSourse(int startHour, int startMin, boolean needChange){
        Calendar calendar = Calendar.getInstance();
        if (startHour == -1){
            calendar.setTime(TimeUtil.getInstance().millis2Date(System.currentTimeMillis() + QUARTER_TIME * 2));
        }else {
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.set(Calendar.MINUTE, startMin);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.MINUTE, 15);
            mEndMinites.clear();
            mEndHours.clear();
        }
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        if(needChange){
            mEndTimeMillis = calendar.getTimeInMillis();
            mEndTime = TIME_FORMAT.format(calendar.getTimeInMillis());
        }
        ArrayList<String> end_minute = new ArrayList<>();

        end_minute.add(String.format(INTEGER_2, min));

        if (min > 0 && min < 15) {
            end_minute.add("15");
            end_minute.add("30");
            end_minute.add("45");
        } else if (min >= 15 && min < 30) {
            end_minute.add("30");
            end_minute.add("45");
        } else if (min >= 30 && min < 45) {
            end_minute.add("45");
        }
        mEndMinites.add(end_minute);
        mEndHours.add(String.valueOf(hour));
        hour++;
        for (int h = hour; h < 24; h++) {
            mEndMinites.add(mMinute);
            mEndHours.add(String.valueOf(h));
        }
        mEndHours.add("24");
        ArrayList<String> lastMinute = new ArrayList<>();
        lastMinute.add("00");
        mEndMinites.add(lastMinute);
    }

    public void onItemClick(View view) {
        int position = mRvParking.getChildAdapterPosition(view);
        if (position == 1) {
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    //返回的分别是三个级别的选中位置
                    mStartHourSelection = options1;
                    mStartMinSelection = options2;
                    mStartTime = mStartHours.get(options1) + ":" + mStartMinites.get(options1).get(options2);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mStartHours.get(options1)));
                    calendar.set(Calendar.MINUTE, Integer.valueOf(mStartMinites.get(options1).get(options2)));
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    mStartTimeMillis = calendar.getTimeInMillis();
                    initEndTimeDataSourse(Integer.valueOf(mStartHours.get(options1)), Integer.valueOf(mStartMinites.get(options1).get(options2)), mEndTimeMillis <= mStartTimeMillis);
                    float mills = mEndTimeMillis - mStartTimeMillis;
                    float hours = mills/1000/3600;
                    mPrice = hours * mUnitPrice;
                    mAdapter.notifyDataSetChanged();
                }
            })
                    .setTitleText("开始时间")
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.GREEN)//设置分割线的颜色
                    .setSelectOptions(mStartHourSelection, mStartMinSelection)//默认选中项
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
            pvOptions.setPicker(mStartHours, mStartMinites);//二级选择器
            pvOptions.show();
        }
        if (position == 2) {
            OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    //返回的分别是三个级别的选中位置
                    mEndHourSelection = options1;
                    mEndMinSelection = options2;
                    mEndTime = mEndHours.get(options1) + ":" + mEndMinites.get(options1).get(options2);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mEndHours.get(options1)));
                    calendar.set(Calendar.MINUTE, Integer.valueOf(mEndMinites.get(options1).get(options2)));
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    mEndTimeMillis = calendar.getTimeInMillis();
                    float mills = mEndTimeMillis - mStartTimeMillis;
                    float hours = mills/1000/3600;
                    mPrice = hours * mUnitPrice;
                    mAdapter.notifyDataSetChanged();
                }
            })
                    .setTitleText("结束时间")
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.GREEN)//设置分割线的颜色
                    .setSelectOptions(mEndHourSelection, mEndMinSelection)//默认选中项
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
            pvOptions.setPicker(mEndHours, mEndMinites);//二级选择器
            pvOptions.show();
        }
    }


    private void initRecyclerView() {
        mRvParking.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ParkingListAdapter();
        mRvParking.setAdapter(mAdapter);
        Context context = ParkingListActivity.this;
        DividerItemDecoration did = new DividerItemDecoration(context, LinearLayoutManager.VERTICAL);
        mRvParking.addItemDecoration(did);
    }


    public class ParkingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_TYPE.ITEM_TYPE_BTN.ordinal()) {
                ParkingHolder viewHolder = new ParkingHolder(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list, parent, false));
                return viewHolder;
            }
            if (viewType == ITEM_TYPE.ITEM_TYPE_NO_BTN.ordinal()) {
                ParkingHolderNoBtn viewHolder = new ParkingHolderNoBtn(LayoutInflater.from(ParkingListActivity.this).inflate(R.layout.item_rv_parking_list_nobtn, parent, false));
                return viewHolder;
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == LIST_START_TIME || position == LIST_END_TIME) {
                return ITEM_TYPE.ITEM_TYPE_BTN.ordinal();
            } else {
                return ITEM_TYPE.ITEM_TYPE_NO_BTN.ordinal();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ParkingHolderNoBtn) {
                if (position == LIST_PARKING_INFO) {
                    ((ParkingHolderNoBtn) holder).tv_title.setText(mEstateBean.getName());
                    ((ParkingHolderNoBtn) holder).tv_content.setText("￥"+ String.format(DECIMAL_2, (float) mEstateBean.getUnitPrice()) +"/小时");
                }
                if (position == LIST_TOTAL_FEE) {
                    ((ParkingHolderNoBtn) holder).tv_title.setText("停车费");
                    ((ParkingHolderNoBtn) holder).tv_content.setText("￥" + String.format(DECIMAL_2, mPrice));
                }
            } else if (holder instanceof ParkingHolder) {
                if (position == LIST_START_TIME) {
                    ((ParkingHolder) holder).tv_title.setText("开始时间");
                    ((ParkingHolder) holder).tv_content.setText(mStartTime);
                }
                if (position == LIST_END_TIME) {
                    ((ParkingHolder) holder).tv_title.setText("结束时间");
                    ((ParkingHolder) holder).tv_content.setText(mEndTime);
                }
            }
        }

        @Override
        public int getItemCount() {
            return LIST_ITEM_COUNT;
        }

        class ParkingHolder extends RecyclerView.ViewHolder {
            private TextView tv_title;
            private TextView tv_content;
            private ImageView iv_arrow;

            private ParkingHolder(View view) {
                super(view);
                tv_title = (TextView) view.findViewById(R.id.tv_parking_title);
                tv_content = (TextView) view.findViewById(R.id.tv_parking_content);
                iv_arrow = (ImageView) view.findViewById(R.id.iv_parking_arrow);
            }
        }

        class ParkingHolderNoBtn extends RecyclerView.ViewHolder {
            private TextView tv_title;
            private TextView tv_content;

            private ParkingHolderNoBtn(View view) {
                super(view);
                tv_title = (TextView) view.findViewById(R.id.tv_parking_title_nobtn);
                tv_content = (TextView) view.findViewById(R.id.tv_parking_content_nobtn);
            }
        }
    }

    /*********预约按钮*********/
    @OnClick(R.id.btn_parking_reserve)
    public void onViewClicked() {
        String phoneNum = SharedPreferenceUtil.getString(mContext, Constant.PHONE_KEY, "");
        if (TextUtils.isEmpty(phoneNum)){
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
        }else {
            ReserveService reserveService = ServiceGenerator.createService(ReserveService.class);
            final ReserveRequest reserveRequest = new ReserveRequest(EncryptUtil.encrypt(SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, Constant.TEST_PHONE_NUM), EncryptUtil.ALGO.SHA_256), mEstateBean.getId(), mStartTimeMillis, mEndTimeMillis);
            Call<ReserveResponse> call = reserveService.reserve(reserveRequest);
            call.enqueue(new Callback<ReserveResponse>() {
                @Override
                public void onResponse(Call<ReserveResponse> call, Response<ReserveResponse> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                        // TODO: 2017/8/3 预约成功，跳转支付界面
                        Intent intent = new Intent(ParkingListActivity.this, PayActivity.class);
                        intent.putExtra("grauFee", mUnitPrice);
                        startActivity(intent);
                        //可停至XX：XX
                        ToastUtil.showToast(mContext, "预约成功");
                    }else {
                        final AlertDialog.Builder reserveFailedDialog = new AlertDialog.Builder(ParkingListActivity.this);
                        //reserveFailedDialog.setIcon(R.drawable.icon_dialog);
                        //可以增加APPlogo
                        reserveFailedDialog.setTitle("预约失败");
                        reserveFailedDialog.setMessage("没有合适的车位，请重新选择小区或时间");
                        reserveFailedDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        reserveFailedDialog.show();
                    }
                }

                @Override
                public void onFailure(Call<ReserveResponse> call, Throwable t) {
                    ToastUtil.showToast(ParkingListActivity.this, "网络连接异常");
                }
            });
        }

    }


}