package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.DialogParkAdapter;
import com.qhiehome.ihome.adapter.PublishParkingAdapter;
import com.qhiehome.ihome.bean.ParkingItem;
import com.qhiehome.ihome.bean.PublishBean;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigRequest;
import com.qhiehome.ihome.network.model.configuration.city.CityConfigResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingempty.ParkingEmptyResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;
import com.qhiehome.ihome.network.model.park.publish.PublishparkResponse;
import com.qhiehome.ihome.network.model.park.publishcancel.PublishCancelRequest;
import com.qhiehome.ihome.network.model.park.publishcancel.PublishCancelResponse;
import com.qhiehome.ihome.network.service.configuration.CityConfigService;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.park.PublishCallbackService;
import com.qhiehome.ihome.network.service.park.PublishParkService;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.QhDeleteItemDialog;
import com.qhiehome.ihome.view.QhPublishParkingDialog;
import com.qhiehome.ihome.view.RecyclerViewEmptySupport;
import com.qhiehome.ihome.view.WeekPickView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishParkingActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = PublishParkingActivity.class.getSimpleName();

    @BindView(R.id.iv_fab)
    ImageView mFab;

    @BindView(R.id.srf_publish)
    SwipeRefreshLayout mSrfPublish;
    @BindView(R.id.toolbar_center)
    Toolbar mToolbar;
    @BindView(R.id.tv_title_toolbar)
    TextView mTvTitleToolbar;
    @BindView(R.id.ll_publish_empty)
    LinearLayout mLlPublishEmpty;

    private ArrayList<ParkingItem> mParkingItems;
    private ArrayList<Boolean> mSelected;

    private Context mContext;

    private int mPeriodTimes;

    LinearLayout mContainer;

    RecyclerView mRvPark;

    private ArrayList<TimePeriod> mTimePeriods;

    private ArrayList<PublishBean> mPublishList;

    private PublishParkingAdapter mPublishAdapter;

    private String mPhoneNum;

    private int selectedPosition;

    private ArrayAdapter<String> mStartAdapter;
    private ArrayAdapter<String> mEndAdapter;

    private List<String> mStartData;
    private List<String> mEndData;

    private SparseBooleanArray mCheckedArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_parking);
        ButterKnife.bind(this);
        mContext = this;
        initSwipeRefreshLayout();
        initData();
        initView();
    }

    private void initSwipeRefreshLayout() {
        mSrfPublish.setOnRefreshListener(this);
        mSrfPublish.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSrfPublish.setRefreshing(true);
    }

    private void initData() {
        mParkingItems = new ArrayList<>();
        mSelected = new ArrayList<>();
        mPublishList = new ArrayList<>();
        mTimePeriods = new ArrayList<>();
        mCheckedArray = new SparseBooleanArray();
        mPhoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
        initSpinner();
        inquiryParkingInfo();
    }

    private void initSpinner() {
        mStartData = TimeUtil.getInstance().setTimeInterval(TimeUtil.TIME_INTERVAL).getStartTime();
        mEndData = TimeUtil.getInstance().setTimeInterval(TimeUtil.TIME_INTERVAL).getEndTime();
        if (mEndData.size() > 1) {
            mEndData.remove(0);
        }
        mStartAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, mStartData);
        mStartAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEndAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, mEndData);
        mEndAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void inquiryParkingInfo() {
        mParkingItems.clear();
        mPublishList.clear();
        ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
        ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(EncryptUtil.encrypt(mPhoneNum, EncryptUtil.ALGO.RSA));
        Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
        call.enqueue(new Callback<ParkingOwnedResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkingOwnedResponse> call, @NonNull Response<ParkingOwnedResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    // first step get the parking ids, then get share info
                    List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
                    int estateId = 0;
                    for (ParkingResponse.DataBean.EstateBean estateBean : estateList) {
                        estateId = estateBean.getId();
                        List<ParkingResponse.DataBean.EstateBean.ParkingListBean> parkingList = estateBean.getParkingList();
                        for (ParkingResponse.DataBean.EstateBean.ParkingListBean parkingBean : parkingList) {
                            mParkingItems.add(new ParkingItem(parkingBean.getId() + "", estateId));
                            mSelected.add(false);
                            List<ParkingResponse.DataBean.EstateBean.ParkingListBean.ShareListBean> shareList = parkingBean.getShareList();
                            for (ParkingResponse.DataBean.EstateBean.ParkingListBean.ShareListBean shareBean : shareList) {
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
                                PublishBean publishBean = new PublishBean(parkingBean.getId() + "",
                                        timeFormat.format(TimeUtil.getInstance().millis2Date(shareBean.getStartTime())),
                                        timeFormat.format(TimeUtil.getInstance().millis2Date(shareBean.getEndTime())));
                                publishBean.setShareId(shareBean.getId());
                                mPublishList.add(publishBean);
                            }
                        }
                    }
                    if (mPublishAdapter != null) {
                        Collections.sort(mPublishList);
                        mPublishAdapter.notifyDataSetChanged();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSrfPublish.setRefreshing(false);
                            mFab.setClickable(true);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ParkingOwnedResponse> call, @NonNull Throwable t) {
                LogUtil.d(TAG, "request failure");
                mSrfPublish.setRefreshing(false);
            }
        });
    }

    private void initView() {
        initToolbar();
        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerViewEmptySupport rv = (RecyclerViewEmptySupport) findViewById(R.id.rv_publish);
        rv.setEmptyView(mLlPublishEmpty);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mPublishAdapter = new PublishParkingAdapter(this, mPublishList);
        initListener(rv);
        rv.setAdapter(mPublishAdapter);
    }

    private void initListener(final RecyclerViewEmptySupport rv) {
        rv.setOnItemClickListneer(new PublishParkingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SwitchCompat switcher = (SwitchCompat) view.findViewById(R.id.sc_republish);
                switcher.setChecked(!switcher.isChecked());
            }

            @Override
            public void onCallbackPublish(View view, final int position) {
                final PublishBean publishBean = mPublishList.get(position);
                final int shareId = publishBean.getShareId();
                QhDeleteItemDialog dialog = new QhDeleteItemDialog(mContext);
                dialog.setOnSureCallbackListener(new QhDeleteItemDialog.OnSureCallbackListener() {
                    @Override
                    public void onSure(View view) {
                        PublishCallbackService publishCallbackService = ServiceGenerator.createService(PublishCallbackService.class);
                                PublishCancelRequest publishCancelRequest = new PublishCancelRequest(shareId, Constant.DEFAULT_PASSWORD);
                                Call<PublishCancelResponse> call = publishCallbackService.callback(publishCancelRequest);
                                call.enqueue(new Callback<PublishCancelResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<PublishCancelResponse> call, @NonNull Response<PublishCancelResponse> response) {
                                        if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                                            mPublishAdapter.removeItem(position);
                                            if (mPublishList.size() == 0) {
                                                mPublishAdapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            ToastUtil.showToast(mContext, "取消失败");
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<PublishCancelResponse> call, @NonNull Throwable t) {

                                    }
                                });
                    }
                });
                dialog.show();
            }

            @Override
            public void onToggleRepublish(final View switcher, boolean isChecked, final int position, final TextView textView) {
                // republish
                if (isChecked) {
                    View customView = LayoutInflater.from(mContext).inflate(R.layout.dialog_select_days, null);
                    final WeekPickView weekPickView = (WeekPickView) customView.findViewById(R.id.dpv_selected);
                    new MaterialDialog.Builder(mContext)
                            .title("选择")
                            .customView(customView, false)
                            .positiveText("确定")
                            .negativeText("取消")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    boolean invalid = weekPickView.isInvalid();
                                    if (invalid) {
                                        ((SwitchCompat) switcher).setChecked(false);
                                    } else {
                                        textView.setText(String.format(getString(R.string.republish_format), weekPickView.getSelectDayInfo()));
                                    }
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    ((SwitchCompat) switcher).setChecked(false);
                                }
                            })
                            .canceledOnTouchOutside(false)
                            .show();
                } else {
                    textView.setText("仅一次");
                }
            }
        });
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
        mTvTitleToolbar.setText("发布车位");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @OnClick(R.id.iv_fab)
    public void addPublish() {
        showPublishDialog();
    }

    private void showPublishDialog() {
        try {
            selectedPosition = 0;
            for (int i = 0; i < mSelected.size(); i++) {
                mSelected.set(i, false);
            }
            mPeriodTimes = 0;
            QhPublishParkingDialog dialog = new QhPublishParkingDialog(mContext);
            View customView = dialog.getCustomView();
            if (customView != null) {
                mRvPark = (RecyclerView) customView.findViewById(R.id.rv_dialog);
                mRvPark.setHasFixedSize(true);
                mRvPark.setLayoutManager(new GridLayoutManager(this, 3));
                if (mSelected.size() > 0) {
                    mSelected.set(0, true);
                }
                DialogParkAdapter dialogParkAdapter = new DialogParkAdapter(this, mParkingItems, mSelected);
                dialogParkAdapter.setOnItemClickListener(new DialogParkAdapter.OnClickListener() {
                    @Override
                    public void onClick(View view, int i) {
                        selectedPosition = i;
                        updateTimeInterval(i);
                    }
                });
                mRvPark.setAdapter(dialogParkAdapter);
                mContainer = (LinearLayout) customView.findViewById(R.id.container_period);
                final Button addBtn = (Button) customView.findViewById(R.id.btn_add);
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_publish_parking, null);
                        AppCompatSpinner startSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_start);
                        startSpinner.setAdapter(mStartAdapter);
                        final AppCompatSpinner endSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_end);
                        endSpinner.setAdapter(mEndAdapter);
                        startSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                endSpinner.setSelection(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // do nothing
                            }
                        });
                        mContainer.addView(itemContainer);
                        if (++mPeriodTimes == Constant.TIME_PERIOD_LIMIT) {
                            addBtn.setVisibility(View.GONE);
                        }
                    }
                });
                addBtn.callOnClick();
                updateTimeInterval(0);
            }
            dialog.setOnSurePublishListener(new QhPublishParkingDialog.OnSurePublishListener() {
                @Override
                public void onSure(View view) {
                    getSelectedPeriod();
                    publishParking();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(mContext, "获取车位锁发生错误");
        }
    }

    private void updateTimeInterval(int position) {
        int estateId = mParkingItems.get(position).getEstateId();
        CityConfigService cityConfigService = ServiceGenerator.createService(CityConfigService.class);
        CityConfigRequest cityConfigRequest = new CityConfigRequest(estateId);
        Call<CityConfigResponse> call = cityConfigService.queryCityConfig(cityConfigRequest);
        call.enqueue(new Callback<CityConfigResponse>() {
            @Override
            public void onResponse(@NonNull Call<CityConfigResponse> call, @NonNull Response<CityConfigResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE){
                    int minSharingPeriod = response.body().getData().getMinSharingPeriod();
                    mStartData.clear();
                    mStartData.addAll(TimeUtil.getInstance().setTimeInterval(minSharingPeriod).getStartTime());
                    mStartAdapter.notifyDataSetChanged();

                    mEndData.clear();
                    mEndData.addAll(TimeUtil.getInstance().setTimeInterval(minSharingPeriod).getEndTime());
                    mEndAdapter.notifyDataSetChanged();
                }else {
                    ToastUtil.showToast(mContext, "服务器繁忙，请稍后再试");
                }
            }

            @Override
            public void onFailure(@NonNull Call<CityConfigResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(mContext, "网络连接异常");
            }
        });
    }

    private void getSelectedPeriod() {
        mTimePeriods.clear();
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            View item = mContainer.getChildAt(i);
            AppCompatSpinner startSpinner = (AppCompatSpinner) item.findViewById(R.id.spinner_start);
            AppCompatSpinner endSpinner = (AppCompatSpinner) item.findViewById(R.id.spinner_end);
            if (endSpinner.getSelectedItem() != null) {
                TimePeriod timePeriod = new TimePeriod(TimeUtil.getInstance().getTimeStamp(startSpinner.getSelectedItem().toString()),
                        TimeUtil.getInstance().getTimeStamp(endSpinner.getSelectedItem().toString()));
                mTimePeriods.add(timePeriod);
            } else {
                ToastUtil.showToast(mContext, "开始时间不可设为23:30");
            }
        }
    }

    private void publishParking() {
        long parkingId = Long.valueOf(mParkingItems.get(selectedPosition).getParkingId());
        PublishParkService publishParkService = ServiceGenerator.createService(PublishParkService.class);
        PublishparkRequest publishparkRequest = new PublishparkRequest();
        publishparkRequest.setParkingId(parkingId);
        publishparkRequest.setPassword(EncryptUtil.encrypt(Constant.DEFAULT_PASSWORD, EncryptUtil.ALGO.RSA));
        List<PublishparkRequest.ShareBean> share = new ArrayList<>();
        for (TimePeriod timePeriod : mTimePeriods) {
            PublishparkRequest.ShareBean shareBean = new PublishparkRequest.ShareBean();
            long startTime = timePeriod.getStartTime();
            long endTime = timePeriod.getEndTime();
            shareBean.setStartTime(startTime);
            shareBean.setEndTime(endTime);
            share.add(shareBean);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            PublishBean publishBean = new PublishBean(parkingId + "", timeFormat.format(TimeUtil.getInstance().millis2Date(startTime)), timeFormat.format(TimeUtil.getInstance().millis2Date(endTime)));
            mPublishList.add(publishBean);
        }
        publishparkRequest.setShare(share);
        Call<PublishparkResponse> call = publishParkService.publish(publishparkRequest);
        call.enqueue(new Callback<PublishparkResponse>() {
            @Override
            public void onResponse(@NonNull Call<PublishparkResponse> call, @NonNull Response<PublishparkResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    // get sharedId
                    List<Integer> shareIdList = response.body().getData().getShareId();
                    int N = shareIdList.size();
                    for (int j = 0; j < N; j++) {
                        PublishBean publishBean = mPublishList.get(mPublishList.size() - 1 - j);
                        publishBean.setShareId(shareIdList.get(N - 1 - j));
                    }
                    Collections.sort(mPublishList);
                    mPublishAdapter.notifyDataSetChanged();
                } else {
                    inquiryParkingInfo();
                    ToastUtil.showToast(mContext, "请勿重复发布同一时间段");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PublishparkResponse> call, @NonNull Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(mContext, "网络异常");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, PublishParkingActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onRefresh() {
        inquiryParkingInfo();
    }

    private class TimePeriod {
        private long startTime;
        private long endTime;

        private TimePeriod(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        private long getStartTime() {
            return startTime;
        }

        private long getEndTime() {
            return endTime;
        }
    }
}
