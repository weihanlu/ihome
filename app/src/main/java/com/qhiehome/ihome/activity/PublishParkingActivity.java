package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.park.publish.PublishparkRequest;
import com.qhiehome.ihome.network.model.park.publish.PublishparkResponse;
import com.qhiehome.ihome.network.model.signin.SigninRequest;
import com.qhiehome.ihome.network.model.signin.SigninResponse;
import com.qhiehome.ihome.network.service.park.PublishParkService;
import com.qhiehome.ihome.network.service.signin.SigninService;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishParkingActivity extends AppCompatActivity {

    private static final String TAG = PublishParkingActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.srf_publish)
    SwipeRefreshLayout mSrfPublish;

    @BindView(R.id.rv_publish)
    RecyclerView mRvPublish;

    private boolean hasParkingId;

    private List<String> parkingIdList;

    private Context mContext;

    private int mPeriodTimes;

    AppCompatSpinner mParkSpinner;
    AppCompatSpinner mStartSpinner;
    AppCompatSpinner mEndSpinner;

    ArrayList<TimePeriod> mTimePeriods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_parking);
        ButterKnife.bind(this);
        initData();
        initView();
        ActivityManager.add(this);
        mContext = this;
    }

    private void initData() {
        hasParkingId = true;
        parkingIdList = new ArrayList<>();
        parkingIdList.add("123456");
        parkingIdList.add("123457");
        parkingIdList.add("123458");
        parkingIdList.add("123459");

        mTimePeriods = new ArrayList<>();
    }

    private void initView() {
        initToolbar();
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
                final LinearLayout container = (LinearLayout) customView.findViewById(R.id.container_period);
                Button addBtn = (Button) customView.findViewById(R.id.btn_add);
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPeriodTimes >= Constant.TIME_PERIOD_LIMIT) {
                            ToastUtil.showToast(mContext, "已达上限");
                        } else {
                            View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_publish_parking, null);
                            mStartSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_start);
                            ArrayAdapter<String> startAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, TimeUtil.getInstance().getOnedayTime());
                            startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mStartSpinner.setAdapter(startAdapter);
                            mEndSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_end);
                            ArrayAdapter<String> endAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, TimeUtil.getInstance().getOnedayTime());
                            endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mEndSpinner.setAdapter(endAdapter);
                            container.addView(itemContainer);
                            mPeriodTimes++;
                            TimePeriod timePeriod = new TimePeriod(TimeUtil.getInstance().getTimeStamp(mStartSpinner.getSelectedItem().toString()),
                                    TimeUtil.getInstance().getTimeStamp(mEndSpinner.getSelectedItem().toString()));
                            mTimePeriods.add(timePeriod);
                        }
                    }
                });
            }
            dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // publish parking info
                    publishParking();
                }
            }).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // do nothing
                }
            });
            dialog.show();
        } else {
            // TODO: 2017/7/21 跳转到设置车位的页面
        }
    }

    private void publishParking() {
        PublishParkService publishParkService = ServiceGenerator.createService(PublishParkService.class);
        PublishparkRequest publishparkRequest = new PublishparkRequest();
        publishparkRequest.setParking_id(Long.valueOf(mParkSpinner.getSelectedItem().toString()));
        publishparkRequest.setPassword(EncryptUtil.encrypt(Constant.DEFAULT_PASSWORD, EncryptUtil.ALGO.SHA_256));
        for (TimePeriod timePeriod: mTimePeriods) {
            publishparkRequest.setStart_time(timePeriod.getStartTime());
            publishparkRequest.setEnd_time(timePeriod.getEndTime());
        }
        Call<PublishparkResponse> call = publishParkService.publish(publishparkRequest);
        call.enqueue(new Callback<PublishparkResponse>() {
            @Override
            public void onResponse(Call<PublishparkResponse> call, Response<PublishparkResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    ToastUtil.showToast(PublishParkingActivity.this, "发布成功");
                }
            }
            @Override
            public void onFailure(Call<PublishparkResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(PublishParkingActivity.this, "网络异常");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, PublishParkingActivity.class);
        context.startActivity(intent);
    }

    private class TimePeriod {
        private long startTime;
        private long endTime;
        public TimePeriod(long startTime, long endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }
    }
}
