package com.qhiehome.ihome.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.SettingMenuAdapter;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;
import com.qhiehome.ihome.network.service.update.PgyService;
import com.qhiehome.ihome.network.service.update.PgyServiceGenerator;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseActivity {

    private static final String TAG = "SettingActivity";

    @BindArray(R.array.setting_menu)
    String[] mSettingMenu;

    private Context mContext;

//    @BindView(R.id.toolbar)
//    Toolbar mToolbar;

    @BindView(R.id.rv_setting)
    RecyclerView mRvSetting;


    private boolean cancelUpdate;

    MaterialDialog mUpdateInfoDialog;

    MaterialDialog mUpdateProcessDialog;

    private String mSavedPath;

    private TextView mTvTitleToolbar;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_center);
        mTvTitleToolbar = (TextView) findViewById(R.id.tv_title_toolbar);
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
        boolean canUpdate = SharedPreferenceUtil.getBoolean(this, Constant.UPDATE_ENABLED, false);
        if (canUpdate) {
            mSettingMenu[0] = mSettingMenu[0] + ";可更新";
        } else {
            mSettingMenu[0] = mSettingMenu[0] + ";已是最新版本";
        }
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
                        AboutActivity.start(mContext);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void notification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_logo)
                .setContentTitle("爱车位")
                .setContentText("共享停车类app");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        Notification notification = mBuilder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void updateApp() {
        cancelUpdate = false;
        // 1. request versionCode on server
        PgyService pgyService = PgyServiceGenerator.createService(PgyService.class);
        Call<CheckUpdateResponse> call = pgyService.getLatestVersion(Constant.APK_UPDATE_UKEY, Constant.APK_UPDATE_API_KEY, Constant.APK_UPDATE_PAGE_NUM);
        call.enqueue(new Callback<CheckUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<CheckUpdateResponse> call, @NonNull Response<CheckUpdateResponse> response) {
                CheckUpdateResponse body = response.body();
                if (body != null && body.getCode().equals("0")) {
                    List<CheckUpdateResponse.DataBean.ListBean> list = body.getData().getList();
                    int onLineAppVersionNo = Integer.valueOf(list.get(0).getAppVersionNo());
                    if (onLineAppVersionNo > CommonUtil.getVersionCode()) {
                        final String appKey = list.get(0).getAppKey();
                        final String appUpdateDescription = list.get(0).getAppUpdateDescription();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mUpdateInfoDialog == null)
                                    buildUpdateInfoDialog(appUpdateDescription, appKey);
                                mUpdateInfoDialog.show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(mContext, "当前已是最新版本");
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckUpdateResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(mContext, "网络异常");
            }
        });
    }

    private void buildUpdateInfoDialog(String updateInfo, final String appKey) {
        String[] updateInfoItems = updateInfo.split(";");
        StringBuilder formatUpdateInfo = new StringBuilder();
        for (int i = 0; i < updateInfoItems.length; i++) {
            formatUpdateInfo.append(i + 1).append(". ").append(updateInfoItems[i]).append("\n");
        }
        formatUpdateInfo.deleteCharAt(formatUpdateInfo.length() - 1);
        mUpdateInfoDialog = new MaterialDialog.Builder(this)
                .title("新特性")
                .content(formatUpdateInfo.toString())
                .positiveText("立即更新")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mUpdateProcessDialog == null) buildUpdateProcessDialog();
                        mUpdateProcessDialog.show();
                        downloadApk(String.format(Constant.APK_UPDATE_URL_PATTERN, appKey));
                    }
                })
                .canceledOnTouchOutside(true).build();
    }

    private void buildUpdateProcessDialog() {
        mUpdateProcessDialog = new MaterialDialog.Builder(this)
                .title("正在更新")
                .content("下载进度")
                .progress(false, 100, true)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                        cancelUpdate = true;
                    }
                }).build();
    }

    private void downloadApk(String downloadUrl) {
//        LogUtil.d(TAG, "download url is " + downloadUrl);
        new DownloadAsyncTask().execute(downloadUrl);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvTitleToolbar.setText("系统设置");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    private void installApk() {
        File apkFile = new File(mSavedPath, "Ihome.apk");
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(this, "com.qhiehome.ihome.provider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
        Process.killProcess(Process.myPid());
    }

    private void switchAccount() {
        new MaterialDialog.Builder(mContext)
                .content("确定退出当前账号吗？")
                .positiveText("退出")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ActivityManager.finishAll();
                        SharedPreferenceUtil.setString(mContext, Constant.PHONE_KEY, "");
                        SharedPreferenceUtil.setInt(mContext, Constant.USER_TYPE, Constant.USER_TYPE_TEMP);
                        LoginActivity.start(mContext);
                    }
                }).show();
    }

    @OnClick(R.id.btn_switch_account)
    public void onSwitchAccount() {
        switchAccount();
    }

    private class DownloadAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavedPath = sdpath + "Download";
                    URL url = new URL(params[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    File file = new File(mSavedPath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavedPath, "Ihome.apk");
                    if (apkFile.exists()) {
                        apkFile.delete();
                    }
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte buf[] = new byte[1024];
                    do {
                        int numRead = is.read(buf);
                        count += numRead;
                        final int progress = (int) (((float) count / length) * 100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUpdateProcessDialog.setProgress(progress);
                            }
                        });
                        if (numRead <= 0) {
                            installApk();
                            break;
                        }
                        fos.write(buf, 0, numRead);
                    } while (!cancelUpdate);
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUpdateProcessDialog.dismiss();
                }
            });
            return null;
        }
    }

}
