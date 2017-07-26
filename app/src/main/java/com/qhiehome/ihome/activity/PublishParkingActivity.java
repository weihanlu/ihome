package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
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
import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;
import com.qhiehome.ihome.network.model.park.publish.PublishparkResponse;
import com.qhiehome.ihome.network.service.park.PublishParkService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
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

public class PublishParkingActivity extends BaseActivity {

    private static final String TAG = PublishParkingActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.srf_publish)
    SwipeRefreshLayout mSrfPublish;

    @BindView(R.id.rv_publish)
    RecyclerViewEmptySupport mRvPublish;

    private boolean hasParkingId;

    private List<String> parkingIdList;

    private Context mContext;

    private int mPeriodTimes;

    LinearLayout mContainer;

    AppCompatSpinner mParkSpinner;

    private ArrayList<TimePeriod> mTimePeriods;

    private ArrayList<PublishBean> mPublishList;

    private PublishParkingAdapter mPublishAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_parking);
        ButterKnife.bind(this);
        initData();
        initView();
        mContext = this;
    }

    private void initData() {
        hasParkingId = true;
        parkingIdList = new ArrayList<>();
        parkingIdList.add("123456");
        parkingIdList.add("123457");
        parkingIdList.add("123458");
        parkingIdList.add("123459");
        mPublishList = new ArrayList<>();
        mTimePeriods = new ArrayList<>();
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

    private void initListener(PublishParkingAdapter mPublishAdapter) {
       mPublishAdapter.setOnItemClickListener(new PublishParkingAdapter.OnClickListener() {
           @Override
           public void onClick(int i) {
               PublishBean publishBean = mPublishList.get(i);
               ToastUtil.showToast(mContext, publishBean.getParkingId());
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
        if (hasParkingId) {
            mPeriodTimes = 0;
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this);
            dialogBuilder.title("发布车位").customView(R.layout.dialog_publish_parking, true)
                    .positiveText("确定").negativeText("取消");
            MaterialDialog dialog = dialogBuilder.build();
            View customView = dialog.getCustomView();
            if (customView != null) {
                mParkSpinner = (AppCompatSpinner) customView.findViewById(R.id.spinner_dialog);
                ArrayAdapter<String> parkAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, parkingIdList);
                parkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mParkSpinner.setAdapter(parkAdapter);
                mContainer = (LinearLayout) customView.findViewById(R.id.container_period);
                final Button addBtn = (Button) customView.findViewById(R.id.btn_add);
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_publish_parking, null);
                        AppCompatSpinner startSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_start);
                        ArrayAdapter<String> startAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, TimeUtil.getInstance().getOnedayTime());
                        startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        startSpinner.setAdapter(startAdapter);
                        AppCompatSpinner endSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_end);
                        ArrayAdapter<String> endAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, TimeUtil.getInstance().getOnedayTime());
                        endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        endSpinner.setAdapter(endAdapter);
                        mContainer.addView(itemContainer);
                        mPeriodTimes++;
                        if (mPeriodTimes == Constant.TIME_PERIOD_LIMIT) {
                            addBtn.setVisibility(View.GONE);
                        }
                    }
                });
            }
            dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    getSelectedPeriod();
                    publishParking();
                }
            }).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // do nothing
                }
            }).canceledOnTouchOutside(false)
                    .show();
        } else {
            // TODO: 2017/7/21 跳转到设置车位的页面
        }
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
        publishparkRequest.setParking_id(parkingId);
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
            public void onResponse(Call<PublishparkResponse> call, Response<PublishparkResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    ToastUtil.showToast(mContext, "发布成功");
                    mPublishAdapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showToast(mContext, "发布失败");
                    mPublishList.clear();
                }
            }
            @Override
            public void onFailure(Call<PublishparkResponse> call, Throwable t) {
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
