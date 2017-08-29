package com.qhiehome.ihome.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.UserLockAdapter;
import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.bean.UserLockBean;
import com.qhiehome.ihome.bean.UserLockBeanDao;
import com.qhiehome.ihome.lock.ConnectLockService;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.avatar.UploadAvatarResponse;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdRequest;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdResponse;
import com.qhiehome.ihome.network.service.avatar.DownloadAvatarService;
import com.qhiehome.ihome.network.service.avatar.UploadAvatarService;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.lock.UpdateLockPwdService;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.FileUtils;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;

import org.greenrobot.greendao.query.Query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends BaseActivity {

    private static final String TAG = UserInfoActivity.class.getSimpleName();

    @BindView(R.id.tb_userinfo)
    Toolbar mTbUserInfo;

    @BindView(R.id.vs_user_locks)
    ViewStub mViewStub;

    private Context mContext;

    private ArrayList<UserLockBean> mUserLocks;

    private long mCurrentTime;

    private StringBuilder mParkingIds;

    private ConnectLockReceiver mReceiver;

    EditText mEtOldPwd;
    EditText mEtNewPwd;
    EditText mEtReassurePwd;

    MaterialDialog mProgressDialog;

    MaterialDialog mControlLockDialog;

    private UserLockBeanDao mUserLockBeanDao;

    private Query<UserLockBean> mUserLockBeansQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
        mContext = this;
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new ConnectLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectLockService.BROADCAST_CONNECT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void initData() {
        DaoSession daoSession = ((IhomeApplication)getApplication()).getDaoSession();
        mUserLockBeanDao = daoSession.getUserLockBeanDao();
        mUserLockBeansQuery = mUserLockBeanDao.queryBuilder().orderAsc(UserLockBeanDao.Properties.Id).build();
        String phoneNum = SharedPreferenceUtil.getString(this, Constant.PHONE_KEY, "");

        mUserLocks = new ArrayList<>();
        mParkingIds = new StringBuilder();

        inquiryOwnedParkings(phoneNum);
    }

    private void inquiryOwnedParkings(String phoneNum) {
        mCurrentTime = System.currentTimeMillis();
        if (NetworkUtils.isConnected(this)) {
            ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
            ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(phoneNum);
            Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
            call.enqueue(new Callback<ParkingOwnedResponse>() {
                @Override
                public void onResponse(@NonNull Call<ParkingOwnedResponse> call, @NonNull Response<ParkingOwnedResponse> response) {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        // success and then inflate ViewStub
                        List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
                        if (estateList.size() != 0) {
                            mViewStub.inflate();
                            RecyclerView rvUserLocks = (RecyclerView) findViewById(R.id.rv_user_locks);
                            rvUserLocks.setHasFixedSize(true);
                            LinearLayoutManager llm = new LinearLayoutManager(mContext);
                            rvUserLocks.setLayoutManager(llm);
                            initLocks(estateList);
                            UserLockAdapter userLockAdapter = new UserLockAdapter(mContext, mUserLocks);
                            rvUserLocks.setAdapter(userLockAdapter);
                            initListener(userLockAdapter);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ParkingOwnedResponse> call, @NonNull Throwable t) {

                }
            });
        } else {
            List<UserLockBean> list = mUserLockBeansQuery.list();
            if (list != null && list.size() > 0) {
                mViewStub.inflate();
                RecyclerView rvUserLocks = (RecyclerView) findViewById(R.id.rv_user_locks);
                rvUserLocks.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                rvUserLocks.setLayoutManager(llm);
                for (UserLockBean userLockBean: list) {
                    mUserLocks.add(userLockBean);
                }
                UserLockAdapter userLockAdapter = new UserLockAdapter(mContext, mUserLocks);
                rvUserLocks.setAdapter(userLockAdapter);
                initListener(userLockAdapter);
            }
        }
    }

    private void initListener(final UserLockAdapter userLockAdapter) {
        userLockAdapter.setOnItemClickListener(new UserLockAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int i) {
                UserLockBean userLockBean = mUserLocks.get(i);
                final String gatewayId = userLockBean.getGatewayId();
                final String lockMac = userLockBean.getLockMac();
                if (mProgressDialog == null) {
                    mProgressDialog = new MaterialDialog.Builder(mContext)
                            .title("连接中")
                            .content("请等待...")
                            .progress(true, 0)
                            .showListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Intent connectLock = new Intent(mContext, ConnectLockService.class);
                                    if (NetworkUtils.isConnected(mContext)) {
                                        connectLock.setAction(ConnectLockService.ACTION_GATEWAY_CONNECT);
                                        connectLock.putExtra(ConnectLockService.EXTRA_GATEWAY_ID, gatewayId);
                                    } else {
                                        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                            ToastUtil.showToast(mContext, "不支持蓝牙低能耗特性");
                                            dialog.dismiss();
                                        } else {
                                            connectLock.setAction(ConnectLockService.ACTION_BLUETOOTH_CONNECT);
                                            String password = SharedPreferenceUtil.getString(mContext, Constant.LOCK_PASSWORD_KEY, Constant.DEFAULT_PASSWORD);
                                            connectLock.putExtra(ConnectLockService.EXTRA_LOCK_PWD, password);
                                        }
                                    }
                                    connectLock.putExtra(ConnectLockService.EXTRA_LOCK_MAC, lockMac);
                                    startService(connectLock);
                                }
                            }).build();
                }
                mProgressDialog.show();


                View controlLock = LayoutInflater.from(mContext).inflate(R.layout.dialog_control_lock, null);
                ImageView imgUpLock = (ImageView) controlLock.findViewById(R.id.img_up_lock);
                ImageView imgDownLock = (ImageView) controlLock.findViewById(R.id.img_down_Lock);
                imgUpLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent upLock = new Intent(mContext, ConnectLockService.class);
                        upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                        startService(upLock);
                    }
                });
                imgDownLock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent downLock = new Intent(mContext, ConnectLockService.class);
                        downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                        startService(downLock);
                    }
                });
                if (mControlLockDialog == null) {
                    mControlLockDialog = new MaterialDialog.Builder(mContext)
                            .title("已连接").titleGravity(GravityEnum.CENTER)
                            .customView(controlLock, false)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    Intent disconnect = new Intent(mContext, ConnectLockService.class);
                                    disconnect.setAction(ConnectLockService.ACTION_DISCONNECT);
                                    startService(disconnect);
                                }
                            })
                            .build();
                }
            }

            @Override
            public void onButtonClick(View view, int i) {
                final UserLockBean userLockBean = mUserLocks.get(i);
                MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .customView(R.layout.dialog_modify_pwd, false)
                        .positiveText("确定")
                        .negativeText("取消")
                        .build();
                View customView = dialog.getCustomView();
                if (customView != null) {
                    mEtOldPwd = (EditText) customView.findViewById(R.id.et_old_pwd);
                    mEtNewPwd = (EditText) customView.findViewById(R.id.et_new_pwd);
                    mEtReassurePwd = (EditText) customView.findViewById(R.id.et_reassure_new_pwd);
                }
                dialog.getBuilder()
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                CommonUtil.showSoftKeyboard(mEtOldPwd, mContext);
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                int parkingId = userLockBean.getParkingId();
                                String oldPwd = mEtOldPwd.getText().toString();
                                String newPwd = mEtNewPwd.getText().toString();
                                String reAssurePwd = mEtReassurePwd.getText().toString();
                                if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd) || TextUtils.isEmpty(reAssurePwd)) {
                                    ToastUtil.showToast(mContext, "密码输入框不能为空");
                                } else if (newPwd.equals(oldPwd)){
                                    ToastUtil.showToast(mContext, "新密码与旧密码不能相同");
                                } else if (newPwd.length() != 6 || oldPwd.length() != 6){
                                    ToastUtil.showToast(mContext, "新密码或旧密码不是6位");
                                } else if (! newPwd.equals(reAssurePwd)){
                                    ToastUtil.showToast(mContext, "确认密码与新密码不一致");
                                } else {
                                    modifyLockPwd(parkingId, mEtOldPwd.getText().toString(), mEtNewPwd.getText().toString());
                                }
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();
            }
        });
    }

    private void modifyLockPwd(int parkingId, String oldPwd, final String newPwd) {
        UpdateLockPwdService updateLockPwdService = ServiceGenerator.createService(UpdateLockPwdService.class);
        UpdateLockPwdRequest updateLockPwdRequest = new UpdateLockPwdRequest(parkingId, oldPwd, newPwd);
        Call<UpdateLockPwdResponse> call = updateLockPwdService.updateLockPwd(updateLockPwdRequest);
        call.enqueue(new Callback<UpdateLockPwdResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Response<UpdateLockPwdResponse> response) {
                if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                    ToastUtil.showToast(mContext, "修改密码成功");
                    SharedPreferenceUtil.setString(mContext, Constant.LOCK_PASSWORD_KEY, newPwd);
                } else {
                    ToastUtil.showToast(mContext, "密码错误或修改失败");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(mContext, "请检查您的网络");
            }
        });
    }


    private void initLocks(List<ParkingResponse.DataBean.EstateBean> estateList) {
        mUserLocks.clear();
        UserLockBean userLockBean;
        boolean isRented = false;
        for (ParkingResponse.DataBean.EstateBean estate : estateList) {
            for (ParkingResponse.DataBean.EstateBean.ParkingListBean parkingBean : estate.getParkingList()) {
                List<ParkingResponse.DataBean.EstateBean.ParkingListBean.ShareListBean> share = parkingBean.getShareList();
                for (int i = 0; i < share.size(); i++) {
                    ParkingResponse.DataBean.EstateBean.ParkingListBean.ShareListBean shareBean = share.get(i);
                    long startTime = shareBean.getStartTime();
                    long endTime = shareBean.getEndTime();
                    if (mCurrentTime >= startTime && mCurrentTime <= endTime) {
                        isRented = true;
                    }
                }
                userLockBean = new UserLockBean(null, estate.getName(), parkingBean.getName(), parkingBean.getId(), parkingBean.getGatewayId(),
                        parkingBean.getLockMac(), isRented);
                mUserLocks.add(userLockBean);
                mUserLockBeanDao.insertOrReplace(userLockBean);
                mParkingIds.append(parkingBean.getId()).append(",");
            }
        }
        SharedPreferenceUtil.setString(this, Constant.OWNED_PARKING_KEY, mParkingIds.deleteCharAt(mParkingIds.length() - 1).toString());
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        context.startActivity(intent);
    }

    private void initView() {
        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(mTbUserInfo);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTbUserInfo.setTitle("我的车位");
        mTbUserInfo.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        mTbUserInfo.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private class ConnectLockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                if (mControlLockDialog != null && !mControlLockDialog.isShowing()) {
                    mControlLockDialog.show();
                }
            }
        }
    }

}
