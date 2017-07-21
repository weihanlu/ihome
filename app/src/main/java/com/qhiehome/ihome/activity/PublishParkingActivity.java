package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
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
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.TimeUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PublishParkingActivity extends AppCompatActivity {

    private static final String TAG = PublishParkingActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private boolean hasParkingId;

    private List<String> parkingIdList;

    private Context mContext;

    private int mPeriodTimes;

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
        parkingIdList.add("车位号：123456");
        parkingIdList.add("车位号：123457");
        parkingIdList.add("车位号：123458");
        parkingIdList.add("车位好：123459");
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
        showEditDialog();
    }

    private void showEditDialog() {
        if (hasParkingId) {
            mPeriodTimes = 0;
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this);
            dialogBuilder.title("发布车位").customView(R.layout.dialog_publish_parking, true)
                    .positiveText("确定").negativeText("取消");
            MaterialDialog dialog = dialogBuilder.build();
            View customView = dialog.getCustomView();
            if (customView != null) {
                AppCompatSpinner parkSpinner = (AppCompatSpinner) customView.findViewById(R.id.spinner_dialog);
                ArrayAdapter<String> parkAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, parkingIdList);
                parkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                parkSpinner.setAdapter(parkAdapter);
                final LinearLayout container = (LinearLayout) customView.findViewById(R.id.container_period);
                Button addBtn = (Button) customView.findViewById(R.id.btn_add);
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPeriodTimes >= Constant.TIME_PERIOD_LIMIT) {
                            ToastUtil.showToast(mContext, "can't add more");
                        } else {
                            View itemContainer = LayoutInflater.from(mContext).inflate(R.layout.item_publish_parking, null);
                            AppCompatSpinner startSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_start);
                            ArrayAdapter<String> startAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, TimeUtil.getInstance().getOnedayTime());
                            startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            startSpinner.setAdapter(startAdapter);
                            AppCompatSpinner endSpinner = (AppCompatSpinner) itemContainer.findViewById(R.id.spinner_end);
                            ArrayAdapter<String> endAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, TimeUtil.getInstance().getOnedayTime());
                            endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            endSpinner.setAdapter(endAdapter);
                            container.addView(itemContainer);
                            mPeriodTimes++;
                        }
                    }
                });
            }
            dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    ToastUtil.showToast(mContext, "positive");
                }
            }).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    ToastUtil.showToast(mContext, "negative");
                }
            });
            dialog.show();
        } else {
            // TODO: 2017/7/21 跳转到设置车位的页面
        }
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
}
