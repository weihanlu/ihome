package com.qhiehome.ihome.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdRequest;
import com.qhiehome.ihome.network.model.lock.updatepwd.UpdateLockPwdResponse;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.network.service.lock.UpdateLockPwdService;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.QhLockConnectDialog;
import com.qhiehome.ihome.view.QhModifyPasswordDialog;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLockFragment extends Fragment {

    private Activity mActivity;

    private ArrayList<UserLockBean> mUserLocks;

    private long mCurrentTime;

    private StringBuilder mParkingIds;

    private ConnectLockReceiver mReceiver;

    MaterialDialog mProgressDialog;

    QhLockConnectDialog mControlLockDialog;

    private UserLockBeanDao mUserLockBeanDao;

    private Query<UserLockBean> mUserLockBeansQuery;

    Unbinder unbinder;

    @BindView(R.id.vs_user_locks)
    ViewStub mViewStub;

    View mView;

    public UserLockFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_user_lock, container, false);
        unbinder = ButterKnife.bind(this, mView);
        initData();
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new ConnectLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectLockService.BROADCAST_CONNECT);
        mActivity.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.unregisterReceiver(mReceiver);
    }

    private void initData() {
        DaoSession daoSession = ((IhomeApplication) getActivity().getApplicationContext()).getDaoSession();
        mUserLockBeanDao = daoSession.getUserLockBeanDao();
        mUserLockBeansQuery = mUserLockBeanDao.queryBuilder().orderAsc(UserLockBeanDao.Properties.Id).build();
        String phoneNum = SharedPreferenceUtil.getString(mActivity, Constant.PHONE_KEY, "");

        mUserLocks = new ArrayList<>();
        mParkingIds = new StringBuilder();

        inquiryOwnedParkings(phoneNum);
    }

    private void inquiryOwnedParkings(String phoneNum) {
        mCurrentTime = System.currentTimeMillis();
        if (NetworkUtils.isConnected(mActivity)) {
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
                            RecyclerView rvUserLocks = (RecyclerView) mView.findViewById(R.id.rv_user_locks);
                            rvUserLocks.setHasFixedSize(true);
                            LinearLayoutManager llm = new LinearLayoutManager(mActivity);
                            rvUserLocks.setLayoutManager(llm);
                            initLocks(estateList);
                            UserLockAdapter userLockAdapter = new UserLockAdapter(mActivity, mUserLocks);
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
                RecyclerView rvUserLocks = (RecyclerView) mView.findViewById(R.id.rv_user_locks);
                rvUserLocks.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(mActivity);
                rvUserLocks.setLayoutManager(llm);
                for (UserLockBean userLockBean : list) {
                    mUserLocks.add(userLockBean);
                }
                UserLockAdapter userLockAdapter = new UserLockAdapter(mActivity, mUserLocks);
                rvUserLocks.setAdapter(userLockAdapter);
                initListener(userLockAdapter);
            }
        }
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
        SharedPreferenceUtil.setString(mActivity, Constant.OWNED_PARKING_KEY, mParkingIds.deleteCharAt(mParkingIds.length() - 1).toString());
    }

    private void initListener(final UserLockAdapter userLockAdapter) {
        userLockAdapter.setOnItemClickListener(new UserLockAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int i) {
                UserLockBean userLockBean = mUserLocks.get(i);
                final String gatewayId = userLockBean.getGatewayId();
                final String lockMac = userLockBean.getLockMac();
                if (mProgressDialog == null) {
                    mProgressDialog = new MaterialDialog.Builder(mActivity)
                            .title("连接中")
                            .content("请等待...")
                            .progress(true, 0)
                            .showListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Intent connectLock = new Intent(mActivity, ConnectLockService.class);
                                    if (NetworkUtils.isConnected(mActivity)) {
                                        connectLock.setAction(ConnectLockService.ACTION_GATEWAY_CONNECT);
                                        connectLock.putExtra(ConnectLockService.EXTRA_GATEWAY_ID, gatewayId);
                                    } else {
                                        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                            ToastUtil.showToast(mActivity, "不支持蓝牙低能耗特性");
                                            dialog.dismiss();
                                        } else {
                                            connectLock.setAction(ConnectLockService.ACTION_BLUETOOTH_CONNECT);
                                            String password = SharedPreferenceUtil.getString(mActivity, Constant.LOCK_PASSWORD_KEY, Constant.DEFAULT_PASSWORD);
                                            connectLock.putExtra(ConnectLockService.EXTRA_LOCK_PWD, password);
                                        }
                                    }
                                    connectLock.putExtra(ConnectLockService.EXTRA_LOCK_MAC, lockMac);
                                    mActivity.startService(connectLock);
                                }
                            }).build();
                }
                mProgressDialog.show();

                if (mControlLockDialog == null) {
                    mControlLockDialog = new QhLockConnectDialog(getActivity());
                    mControlLockDialog.setOnItemClickListener(new QhLockConnectDialog.OnItemClickListener() {
                        @Override
                        public void onLockUp(View view) {
                            Intent upLock = new Intent(mActivity, ConnectLockService.class);
                            upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                            mActivity.startService(upLock);
                        }

                        @Override
                        public void onLockDown(View view) {
                            Intent downLock = new Intent(mActivity, ConnectLockService.class);
                            downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                            mActivity.startService(downLock);
                        }
                    });
                }
            }

            @Override
            public void onButtonClick(View view, int i) {
                final UserLockBean userLockBean = mUserLocks.get(i);
                QhModifyPasswordDialog dialog = new QhModifyPasswordDialog(mActivity);
                View customView = dialog.getCustomView();
                final EditText oldPassoword = (EditText) customView.findViewById(R.id.et_old_password);
                dialog.setOnItemClickListener(new QhModifyPasswordDialog.OnItemClickListener() {
                    @Override
                    public void onCofirm(String oldPassword, String newPassword, String confirmPassword) {
                        int parkingId = userLockBean.getParkingId();
                        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                            ToastUtil.showToast(mActivity, "密码输入框不能为空");
                        } else if (newPassword.equals(oldPassword)) {
                            ToastUtil.showToast(mActivity, "新密码与旧密码不能相同");
                        } else if (newPassword.length() != 6 || oldPassword.length() != 6) {
                            ToastUtil.showToast(mActivity, "新密码或旧密码不是6位");
                        } else if (!newPassword.equals(confirmPassword)) {
                            ToastUtil.showToast(mActivity, "确认密码与新密码不一致");
                        } else {
                            modifyLockPwd(parkingId, oldPassword, newPassword);
                        }
                    }
                });
                dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommonUtil.showSoftKeyboard(oldPassoword, getActivity());
                    }
                }, 100);
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
                    ToastUtil.showToast(mActivity, "修改密码成功");
                    SharedPreferenceUtil.setString(mActivity, Constant.LOCK_PASSWORD_KEY, newPwd);
                } else {
                    ToastUtil.showToast(mActivity, "密码错误或修改失败");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateLockPwdResponse> call, @NonNull Throwable t) {
                ToastUtil.showToast(mActivity, "请检查您的网络");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
