package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.MeAdapter;
import com.qhiehome.ihome.adapter.SettingMenuAdapter;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;
import com.qhiehome.ihome.network.service.update.PgyService;
import com.qhiehome.ihome.network.service.update.PgyServiceGenerator;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.SharedPreferenceUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseActivity {

    private static final String TAG = "SettingActivity";

    @BindArray(R.array.setting_menu)
    String[] mSettingMenu;

    private Context mContext;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rv_setting)
    RecyclerView mRvSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initView();
        mContext = this;
    }

    private void initView() {
        initToolbar();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mRvSetting.setLayoutManager(new LinearLayoutManager(this));
        mRvSetting.setHasFixedSize(true);
        mRvSetting.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        SettingMenuAdapter settingMenuAdapter = new SettingMenuAdapter(this, mSettingMenu);
        initListener(settingMenuAdapter);
        mRvSetting.setAdapter(settingMenuAdapter);
    }

    private void initListener(SettingMenuAdapter settingMenuAdapter) {
        settingMenuAdapter.setOnItemClickListener(new SettingMenuAdapter.OnClickListener() {
            @Override
            public void onClick(int i) {
                switch (i) {
                    case 0:
                        updateApp();
                        break;
                    case 1:
                        FeedbackActivity.start(mContext);
                        break;
                    case 2:
                        ServiceContractActivity.start(mContext);
                        break;
                    case 3:
                        View aboutApp = LayoutInflater.from(mContext).inflate(R.layout.dialog_about_app, null);
                        new MaterialDialog.Builder(mContext)
                                .title("关于Ihome")
                                .customView(aboutApp ,false)
                                .show();
                        break;
                    case 4:
                        ActivityManager.finishAll();
                        SharedPreferenceUtil.setString(mContext, Constant.PHONE_KEY, "");
                        LoginActivity.start(mContext);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void updateApp() {
        // 1. request versionCode
        //请求参数
        Map<String, String> options = new HashMap<>();
        options.put("aKey","2ecfc4108f814c51dabbb1b21c80fe90");
        options.put("uKey","ce208a1437998ef0a8e5f60b7dc4aac1");
        options.put("_api_key","61bb58e6d87d6d2d6b84c7a44c237a7e");

        PgyService pgyService = PgyServiceGenerator.createService(PgyService.class);
        Call<CheckUpdateResponse> call = pgyService.checkUpdate(options);
        call.enqueue(new Callback<CheckUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<CheckUpdateResponse> call, @NonNull Response<CheckUpdateResponse> response) {

            }

            @Override
            public void onFailure(@NonNull Call<CheckUpdateResponse> call, @NonNull Throwable t) {

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
        mToolbar.setTitle("设置");
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

}
