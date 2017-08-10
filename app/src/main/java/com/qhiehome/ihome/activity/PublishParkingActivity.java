package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.PublishParkingAdapter;
import com.qhiehome.ihome.bean.PublishBean;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;
import com.qhiehome.ihome.network.model.park.publish.PublishparkResponse;
import com.qhiehome.ihome.network.model.park.publishcancel.PublishCancelRequest;
import com.qhiehome.ihome.network.model.park.publishcancel.PublishCancelResponse;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.park.PublishCallbackService;
import com.qhiehome.ihome.network.service.park.PublishParkService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.RecyclerViewEmptySupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishParkingActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = PublishParkingActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.srf_publish)
    SwipeRefreshLayout mSrfPublish;

    @BindView(R.id.rv_publish)
    RecyclerViewEmptySupport mRvPublish;

    private List<String> mParkingIdList;

    private Context mContext;

    private int mPeriodTimes;

    LinearLayout mContainer;

    AppCompatSpinner mParkSpinner;

    private ArrayList<TimePeriod> mTimePeriods;

    private ArrayList<PublishBean> mPublishList;

    private PublishParkingAdapter mPublishAdapter;

    private AppCompatSpinner mEndSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_parking);
        ButterKnife.bind(this);
        initSwiperRefreshLayout();
        initData();
        initView();
        mContext = this;
    }

    private void initSwiperRefreshLayout() {
        mSrfPublish.setOnRefreshListener(this);
        mSrfPublish.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mSrfPublish.setRefreshing(true);
    }

    private void initData() {
        mParkingIdList = new ArrayList<>();
        mPublishList = new ArrayList<>();
        mTimePeriods = new ArrayList<>();
        inquiryParkingInfo();
    }

    private void inquiryParkingInfo() {
        mParkingIdList.clear();
        mPublishList.clear();
        ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
        ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(Constant.TEST_PHONE_NUM);
        Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
        call.enqueue(new Callback<ParkingOwnedResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkingOwnedResponse> call, @NonNull Response<ParkingOwnedResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    // first step get the parking ids, then get share info
                    List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
                    for (ParkingResponse.DataBean.EstateBean estateBean: estateList) {
                        List<ParkingResponse.DataBean.EstateBean.ParkingBean> parkingList = estateBean.getParking();
                        for (ParkingResponse.DataBean.EstateBean.ParkingBean parkingBean: parkingList) {
                            mParkingIdList.add(parkingBean.getId() + "");
                            List<ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean> shareList = parkingBean.getShare();
                            for (ParkingResponse.DataBean.EstateBean.ParkingBean.ShareBean shareBean: shareList) {
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
        TextView tvListEmpty = (TextView) findViewById(R.id.tv_publish_empty);
        rv.setEmptyView(tvListEmpty);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mPublishAdapter = new PublishParkingAdapter(this, mPublishList);
        initListener(mPublishAdapter);
        rv.setAdapter(mPublishAdapter);
    }

    private void initListener(final PublishParkingAdapter mPublishAdapter) {
       mPublishAdapter.setOnItemLongClickListener(new PublishParkingAdapter.OnItemClickListener() {
           @Override
           public void onItemClick(View view, int position) {
               ToastUtil.showToast(PublishParkingActivity.this, "click this");
           }

           @Override
           public void onItemLongClick(View view, final int position) {
               final PublishBean publishBean = mPublishList.get(position);
               final int shareId = publishBean.getShareId();
               new MaterialDialog.Builder(mContext)
                       .title("Warning")
                       .content("确定取消发布吗？")
                       .positiveText("确定")
                       .negativeText("取消")
                       .canceledOnTouchOutside(false)
                       .onPositive(new MaterialDialog.SingleButtonCallback() {
                           @Override
                           public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                               PublishCallbackService publishCallbackService = ServiceGenerator.createService(PublishCallbackService.class);
                               PublishCancelRequest publishCancelRequest = new PublishCancelRequest(shareId, Constant.DEFAULT_PASSWORD);
                               Call<PublishCancelResponse> call = publishCallbackService.callback(publishCancelRequest);
                               call.enqueue(new Callback<PublishCancelResponse>() {
                                   @Override
                                   public void onResponse(@NonNull Call<PublishCancelResponse> call,@NonNull Response<PublishCancelResponse> response) {
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
                       })
                       .show();
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
        mToolbar.setTitle("发布车位");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @OnClick(R.id.fab)
    public void addPublish() {
        showPublishDialog();
    }

    private void showPublishDialog() {
        mPeriodTimes = 0;
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this);
        dialogBuilder.title("发布车位").customView(R.layout.dialog_publish_parking, true)
                .positiveText("确定").negativeText("取消");
        MaterialDialog dialog = dialogBuilder.build();
        View customView = dialog.getCustomView();
        if (customView != null) {
            mParkSpinner = (AppCompatSpinner) customView.findViewById(R.id.spinner_dialog);
            ArrayAdapter<String> parkAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, mParkingIdList);
            parkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mParkSpinner.setAdapter(parkAdapter);
            mContainer = (LinearLayout) customView.findViewById(R.id.container_period);
            final Button addBtn = (Button) customView.findViewById(R.id.btn_add);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_publish_parking, null);
                    AppCompatSpinner startSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_start);
                    List<String> startData = TimeUtil.getInstance().getOnedayTime();
                    ArrayAdapter<String> startAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, startData);
                    startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    startSpinner.setAdapter(startAdapter);
                    mEndSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_end);
                    mContainer.addView(itemContainer);
                    startSpinner.setSelection(0);
                    startSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            List<String> endData = TimeUtil.getInstance().getOnedayTime();
                            endData.remove(0);
                            ArrayAdapter<String> endAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, endData);
                            endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mEndSpinner.setAdapter(endAdapter);
                            for (int j = 0; j < i; j++){
                                endData.remove(0);
                            }
                            endAdapter.notifyDataSetChanged();
                            mEndSpinner.setSelection(0);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    mPeriodTimes++;
                    if (mPeriodTimes == Constant.TIME_PERIOD_LIMIT) {
                        addBtn.setVisibility(View.GONE);
                    }
                }
            });
            addBtn.callOnClick();
        }
        dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                getSelectedPeriod();
                publishParking();
            }
        }).canceledOnTouchOutside(false).show();
    }

    private void getSelectedPeriod() {
        mTimePeriods.clear();
        for (int i = 0; i < mContainer.getChildCount(); i++) {
            View item = mContainer.getChildAt(i);
            AppCompatSpinner startSpinner = (AppCompatSpinner) item.findViewById(R.id.spinner_start);
            AppCompatSpinner endSpinner = (AppCompatSpinner) item.findViewById(R.id.spinner_end);
            TimePeriod timePeriod = new TimePeriod(TimeUtil.getInstance().getTimeStamp(startSpinner.getSelectedItem().toString()),
                    TimeUtil.getInstance().getTimeStamp(endSpinner.getSelectedItem().toString()));
            mTimePeriods.add(timePeriod);
        }
    }

    private void publishParking() {
        long parkingId = Long.valueOf(mParkSpinner.getSelectedItem().toString());
        PublishParkService publishParkService = ServiceGenerator.createService(PublishParkService.class);
        PublishparkRequest publishparkRequest = new PublishparkRequest();
        publishparkRequest.setParkingId(parkingId);
        publishparkRequest.setPassword(EncryptUtil.encrypt(Constant.DEFAULT_PASSWORD, EncryptUtil.ALGO.SHA_256));
        List<PublishparkRequest.ShareBean> share = new ArrayList<>();
        for (TimePeriod timePeriod: mTimePeriods) {
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
            public void onResponse(@NonNull  Call<PublishparkResponse> call, @NonNull  Response<PublishparkResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    // get sharedId
                    List<Integer> shareIdList = response.body().getData().getShareId();
                    for (int i = 0; i < shareIdList.size(); i++) {
                        PublishBean publishBean = mPublishList.get(i);
                        publishBean.setShareId(shareIdList.get(i));
                    }
                    mPublishAdapter.notifyDataSetChanged();
                } else {
                    inquiryParkingInfo();
                    ToastUtil.showToast(mContext, "发布失败");
                }
            }
            @Override
            public void onFailure(@NonNull Call<PublishparkResponse> call,@NonNull Throwable t) {
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
