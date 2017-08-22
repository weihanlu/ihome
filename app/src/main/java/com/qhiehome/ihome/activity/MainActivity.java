package com.qhiehome.ihome.activity;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.SDKInitializer;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.fragment.ParkFragment;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.network.model.update.CheckUpdateResponse;
import com.qhiehome.ihome.network.service.update.PgyService;
import com.qhiehome.ihome.network.service.update.PgyServiceGenerator;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.SharedPreferenceUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.iv_avatar)
    CircleImageView ivAvatar;
    @BindView(R.id.bt_login)
    Button btLogin;
    @BindView(R.id.drawer)
    DrawerLayout drawer;

    private Context mContext;

    Fragment mParkFragment;
    Fragment mThisFragment;
    FragmentManager mFragmentManager;

    private boolean cancelUpdate;

    MaterialDialog mUpdateInfoDialog;

    MaterialDialog mUpdateProcessDialog;

    private String mSavedPath;

    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        initFragments(savedInstanceState);
        checkUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    private void checkLogin() {
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");
        if (!TextUtils.isEmpty(phoneNum)) {
            isLogin = true;
        }
        if (isLogin) {
            ivAvatar.setVisibility(View.VISIBLE);
            btLogin.setVisibility(View.INVISIBLE);
        }
    }

    private void checkUpdate() {
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
                        SharedPreferenceUtil.setBoolean(mContext, Constant.UPDATE_ENABLED, true);
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
                        SharedPreferenceUtil.setBoolean(mContext, Constant.UPDATE_ENABLED, false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckUpdateResponse> call, @NonNull Throwable t) {
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
        new DownloadAsyncTask().execute(downloadUrl);
    }

    private void initFragments(Bundle savedInstanceState) {
        mFragmentManager = getSupportFragmentManager();
        // First init load ParkFragment
        if (savedInstanceState == null) {
            mParkFragment = ParkFragment.newInstance();
            mFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, mParkFragment, ParkFragment.TAG).commit();
        } else {
            mParkFragment = mFragmentManager.findFragmentByTag(ParkFragment.TAG);
            mFragmentManager.beginTransaction()
                    .show(mParkFragment)
                    .commit();
        }
        mThisFragment = mParkFragment;
    }

//    private void switchContent(Fragment from, Fragment to) {
//        if (mThisFragment != to) {
//            mThisFragment = to;
//            if (!to.isAdded()) {
//                String tag = (to instanceof MeFragment) ? MeFragment.TAG: ParkFragment.TAG;
//                mFragmentManager.beginTransaction().hide(from).add(R.id.fragment_container, to, tag).commit();
//            } else {
//                mFragmentManager.beginTransaction().hide(from).show(to).commit();
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mParkFragment.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.ll_my_lock, R.id.ll_my_reserve, R.id.ll_my_publish, R.id.ll_setting, R.id.ll_quit,
            R.id.iv_avatar, R.id.bt_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_my_lock:
                if (isLogin) {
                    UserInfoActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.ll_my_reserve:
                if (isLogin) {
                    ReserveActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.ll_my_publish:
                if (isLogin) {
                    PublishParkingActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }

                break;
            case R.id.ll_setting:
                if (isLogin) {
                    SettingActivity.start(mContext);
                } else {
                    LoginActivity.start(mContext);
                }
                break;
            case R.id.ll_quit:
                ActivityManager.finishAll();
                break;
            case R.id.iv_avatar:
                break;
            case R.id.bt_login:
                LoginActivity.start(mContext);
                break;
        }
    }

    public void openDrawer() {
        if (drawer != null && !drawer.isDrawerOpen(Gravity.START)) {
            drawer.openDrawer(Gravity.START);
        }
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
}
