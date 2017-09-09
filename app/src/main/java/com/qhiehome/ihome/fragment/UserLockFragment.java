package com.qhiehome.ihome.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import com.qhiehome.ihome.lock.ble.CommunicationManager;
import com.qhiehome.ihome.lock.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.lock.ble.profile.HostAppService;
import com.qhiehome.ihome.lock.bluetooth.BluetoothClient;
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
import com.qhiehome.ihome.util.LogUtil;
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

    private static final String TAG = "UserLockFragment";

    private static final int REQUEST_ENABLE_BT = 1;

    private Activity mActivity;

    private ArrayList<UserLockBean> mUserLocks;

    private long mCurrentTime;

    private StringBuilder mParkingIds;

    private ConnectLockReceiver mReceiver;

    MaterialDialog mProgressDialog;

    QhLockConnectDialog mControlLockDialog;

    private UserLockBeanDao mUserLockBeanDao;

    private Query<UserLockBean> mUserLockBeansQuery;

    private BluetoothAdapter mBluetoothAdapter;

    Unbinder unbinder;

    private String mGateWayId;
    private String mLockMac;
    private String mLockName;

    @BindView(R.id.vs_user_locks)
    ViewStub mViewStub;

    View mView;

    private int mLockState;

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
        intentFilter.addAction(BLECommandIntent.RX_CURRENT_STATUS);
        intentFilter.addAction(BLECommandIntent.RX_PASSWORD_RESULT);
        intentFilter.addAction(CommunicationManager.ACTION_CONNECTION_STATE_CHANGE);
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RESULT);
        // rf
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RF_START_UP);
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RF_STOP_UP);
        intentFilter.addAction(BLECommandIntent.RX_LOCK_RF_LOCK_STATE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            showProgressDialog();
        }
    }

    private void initListener(final UserLockAdapter userLockAdapter) {
        userLockAdapter.setOnItemClickListener(new UserLockAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int i) {
                UserLockBean userLockBean = mUserLocks.get(i);
                mGateWayId = userLockBean.getGatewayId();
                mLockMac = userLockBean.getLockMac();
                mLockName = userLockBean.getParkingName();
                if (!NetworkUtils.isConnected(mActivity)) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        ToastUtil.showToast(mActivity, "初始化蓝牙失败");
                    } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        } else {
                            showProgressDialog();
                        }
                    }
                } else {
                    showProgressDialog();
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
            switch (intent.getAction()) {
                case BLECommandIntent.RX_CURRENT_STATUS:
                    // EVENT_TYPE_CURRENT_STATUS
                    // passwordState: 1: set ; 0: not set
                    // battery:       1
                    // lockState:    1: low ; 2: mid 3: high
                    LogUtil.d(TAG, "password state is " + intent.getIntExtra(BLECommandIntent.EXTRA_MM_SET_ALREADY, -1));
                    LogUtil.d(TAG, "battery state is " + intent.getIntExtra(BLECommandIntent.EXTRA_BATTERY_LEVEL, -1));
                    LogUtil.d(TAG, "lock state is " + intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, -1));
                    // send password
                    Bundle data = new Bundle();
                    int[] password = new int[6];
                    for (int i = 0; i < 6; i++) {
                        password[i] = Integer.
                                valueOf(SharedPreferenceUtil
                                        .getString(mActivity, Constant.LOCK_PASSWORD_KEY, Constant.DEFAULT_PASSWORD)
                                        .substring(i, i + 1));
                    }
                    data.putIntArray(BLECommandIntent.EXTRA_PASSWORD, password);
                    data.putInt(BLECommandIntent.EXTRA_ROLE,
                            UserLockBean.LOCK_ROLE.OWNER.ordinal());
                    CommunicationManager.getInstance().sendBLEEvent(getActivity(), BLECommandIntent.SETTING_PASSWORD, data);

                    int state = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, 3);
                    if (state == 1) {
                        mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    }

                    break;
                case ConnectLockService.BROADCAST_CONNECT:
                    String info = intent.getStringExtra("info");
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        if (info != null) {
                            ToastUtil.showToast(getActivity(), info);
                        } else {
                            showControlLockDialog();
                        }
                    }
                    break;
                case BLECommandIntent.RX_PASSWORD_RESULT:
                    int actionId = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_ACTION, -1);
                    int result = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_RESULT, -1);
                    LogUtil.d(TAG, "actionId is " + actionId + ", result is " + result);
                    if (actionId == 0x01) {
                        if (result == 0x10) {
                            // MM 代表密码
                            // go main
                            LogUtil.d(TAG, "password is "
                                    + SharedPreferenceUtil.getString(mActivity, Constant.LOCK_PASSWORD_KEY, Constant.DEFAULT_PASSWORD));
                            Intent normal = new Intent(ConnectLockService.BROADCAST_CONNECT);
                            mActivity.sendBroadcast(normal);
                        } else {
                            Intent errPassword = new Intent(ConnectLockService.BROADCAST_CONNECT);
                            errPassword.putExtra("info", "密码错误");
                            mActivity.sendBroadcast(errPassword);
                        }
                    }
                    break;
                case CommunicationManager.ACTION_CONNECTION_STATE_CHANGE:
                    int status = intent.getIntExtra(
                            CommunicationManager.EXTRA_CONNECTION_STATE_NEW,
                            CommunicationManager.STATE_INIT);
                    if (status == CommunicationManager.BLE_DISCONNECTTED) {
                        if (mControlLockDialog != null && mControlLockDialog.isShowing()) {
                            mControlLockDialog.dismiss();
                        }
                    }
                    break;
                case BLECommandIntent.RX_LOCK_RESULT:
                    int lockResult = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_RESULT, 10);
                    if (lockResult == 0x10 || lockResult == 0x20) {
                        if (mLockState == BluetoothClient.LOCK_STATE.UPPING.ordinal()) {
                            mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                        } else {
                            mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                        }
                    } else if (lockResult == 0x30) {
                        mLockState = BluetoothClient.LOCK_STATE.ERROR.ordinal();
                        if (mLockState == BluetoothClient.LOCK_STATE.DOWNING.ordinal()) {
                            ToastUtil.showToast(mActivity, "降锁错误");
                        } else {
                           ToastUtil.showToast(mActivity, "升锁错误");
                        }
                    }
                    LogUtil.d(TAG, "RX_LOCK_RESULT is " + lockResult);
                    break;
                case BLECommandIntent.RX_LOCK_RF_START_UP:
                    mLockState = BluetoothClient.LOCK_STATE.UPPING.ordinal();
                    break;
                case BLECommandIntent.RX_LOCK_RF_STOP_UP:
                    boolean success = intent.getBooleanExtra(BLECommandIntent.EXTRA_LOCK_STOP_UP, false);
                    if (success) {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.ERROR.ordinal();
                    }
                    break;
                case BLECommandIntent.RX_LOCK_RF_LOCK_STATE:
                    boolean isDown = intent.getBooleanExtra(BLECommandIntent.EXTRA_LOCK_RF_LOCK_STATE, false);
                    if (isDown) {
                        mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    }
                    break;
            }
        }
    }

    private void showProgressDialog() {
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
                                connectLock.putExtra(ConnectLockService.EXTRA_GATEWAY_ID, mGateWayId);
                                connectLock.putExtra(ConnectLockService.EXTRA_LOCK_MAC, mLockMac);
                            } else {
                                if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                    ToastUtil.showToast(mActivity, "不支持蓝牙低能耗特性");
                                    dialog.dismiss();
                                } else {
                                    connectLock.setAction(ConnectLockService.ACTION_BLUETOOTH_CONNECT);
                                    connectLock.putExtra(ConnectLockService.EXTRA_LOCK_NAME, mLockName);
                                }
                            }
                            mActivity.startService(connectLock);
                        }
                    }).build();
        }
        mProgressDialog.show();
    }

    private void showControlLockDialog() {
        if (mControlLockDialog == null) {
            mControlLockDialog = new QhLockConnectDialog(getActivity());
            mControlLockDialog.setOnItemClickListener(new QhLockConnectDialog.OnItemClickListener() {
                @Override
                public void onLockUp(View view) {
                    Intent upLock = new Intent(mActivity, ConnectLockService.class);
                    upLock.setAction(ConnectLockService.ACTION_UP_LOCK);
                    LogUtil.d(TAG,"lockState is " + mLockState);
                    upLock.putExtra(ConnectLockService.ACTION_LOCK_STATE, mLockState);
                    mActivity.startService(upLock);
                }

                @Override
                public void onLockDown(View view) {
                    Intent downLock = new Intent(mActivity, ConnectLockService.class);
                    downLock.setAction(ConnectLockService.ACTION_DOWN_LOCK);
                    LogUtil.d(TAG,"lockState is " + mLockState);
                    downLock.putExtra(ConnectLockService.ACTION_LOCK_STATE, mLockState);
                    mActivity.startService(downLock);
                }
            });
            mControlLockDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Intent disConnectLock = new Intent(mActivity, ConnectLockService.class);
                    disConnectLock.setAction(ConnectLockService.ACTION_DISCONNECT);
                    mActivity.startService(disConnectLock);
                }
            });
        }
        mControlLockDialog.show();
    }
}
