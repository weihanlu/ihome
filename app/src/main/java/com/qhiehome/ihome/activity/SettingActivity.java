package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.MeAdapter;
import com.qhiehome.ihome.adapter.SettingMenuAdapter;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;
import com.qhiehome.ihome.network.service.update.PgyService;
import com.qhiehome.ihome.network.service.update.PgyServiceGenerator;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.SharedPreferenceUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

    private static final String APK_UPDATE_URL = "http://www.pgyer.com/apiv1/app/install?aKey=2ecfc4108f814c51dabbb1b21c80fe90&_api_key=61bb58e6d87d6d2d6b84c7a44c237a7e&password=ihome";

    @BindArray(R.array.setting_menu)
    String[] mSettingMenu;

    private Context mContext;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.rv_setting)
    RecyclerView mRvSetting;

    private boolean cancelUpdate;

    MaterialDialog mDialog;

    private String mSavedPath;

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
        cancelUpdate = false;
        // 1. request versionCode on server
        // 2. get local app version
        int serverCode = 2;
        int versionCode = CommonUtil.getVersionCode();
        if (serverCode > versionCode) {
            mDialog = new MaterialDialog.Builder(this)
                    .title("正在更新")
                    .content("下载进度")
                    .progress(false, 100, true)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialog.dismiss();
                            cancelUpdate = true;
                        }
                    })
                    .show();
            // download apk
            downloadApk();
        }
    }

    private void downloadApk() {
        new DownloadAsyncTask().execute(APK_UPDATE_URL);
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

    private void installApk() {
        File apkFile = new File(mSavedPath, "Ihome.apk");
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private class DownloadAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavedPath = sdpath + "download";
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
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    byte buf[] = new byte[1024];
                    do {
                        int numRead = is.read(buf);
                        count += numRead;
                        final int progress = (int)(((float)count / length) * 100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.setProgress(progress);
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
                    mDialog.dismiss();
                }
            });
            return null;
        }

    }

}
