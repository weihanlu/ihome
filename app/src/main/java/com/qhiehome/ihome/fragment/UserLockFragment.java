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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import com.afollestad.materialdialogs.MaterialDialog;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.LoginActivity;
import com.qhiehome.ihome.adapter.UserLockAdapter;
import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.lock.LockController;
import com.qhiehome.ihome.lock.gateway.MqttManagerService;
import com.qhiehome.ihome.network.ServiceGenerator;
import com.qhiehome.ihome.network.model.base.ParkingResponse;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedRequest;
import com.qhiehome.ihome.network.model.inquiry.parkingowned.ParkingOwnedResponse;
import com.qhiehome.ihome.network.service.inquiry.ParkingOwnedService;
import com.qhiehome.ihome.persistence.DaoSession;
import com.qhiehome.ihome.persistence.UserLockBean;
import com.qhiehome.ihome.persistence.UserLockBeanDao;
import com.qhiehome.ihome.lock.bluetooth.BluetoothManagerService;
import com.qhiehome.ihome.lock.ble.CommunicationManager;
import com.qhiehome.ihome.lock.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.lock.bluetooth.BluetoothClient;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.EncryptUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.NetworkUtils;
import com.qhiehome.ihome.util.SharedPreferenceUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.QhLockConnectDialog;

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

    private ConnectLockReceiver mReceiver;

    MaterialDialog mProgressDialog;

    QhLockConnectDialog mControlLockDialog;

    private BluetoothAdapter mBluetoothAdapter;

    Unbinder unbinder;

    private String mGateWayId;
    private String mLockMac;
    private String mLockName;
    private String mLockPassword;

    private boolean isPasswordAlreadySet;

    @BindView(R.id.vs_user_locks)
    ViewStub mViewStub;

    View mView;

    private int mLockState;

    private UserLockBeanDao mUserLockBeanDao;

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
    public void onStart() {
        super.onStart();
        mReceiver = new ConnectLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LockController.BROADCAST_CONNECT);
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
    public void onStop() {
        super.onStop();
        disconnect();
        mActivity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public UserLockFragment() {
        // Required empty public constructor
    }

    private void initData() {
        DaoSession daoSession = ((IhomeApplication) getActivity().getApplicationContext()).getDaoSession();
        mUserLockBeanDao = daoSession.getUserLockBeanDao();
        mUserLocks = new ArrayList<>();
        inquiryOwnedParkings();
    }

    private void inquiryOwnedParkings() {
        if (hasNetwork()) {
            getNewestLocks();
        } else {
            getPersistenceLocks();
            showLocks();
        }
    }

    private void showLocks() {
        if (mUserLocks != null && mUserLocks.size() > 0) {
            mViewStub.inflate();
            RecyclerView rvUserLocks = (RecyclerView) mView.findViewById(R.id.rv_user_locks);
            rvUserLocks.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(mActivity);
            rvUserLocks.setLayoutManager(llm);
            UserLockAdapter userLockAdapter = new UserLockAdapter(mActivity, mUserLocks);
            rvUserLocks.setAdapter(userLockAdapter);
            initListener(userLockAdapter);
        }
    }

    private void getNewestLocks() {
        String phoneNum = SharedPreferenceUtil.getString(mActivity, Constant.PHONE_KEY, "");
        ParkingOwnedService parkingOwnedService = ServiceGenerator.createService(ParkingOwnedService.class);
        ParkingOwnedRequest parkingOwnedRequest = new ParkingOwnedRequest(EncryptUtil.rsaEncrypt(phoneNum));
        Call<ParkingOwnedResponse> call = parkingOwnedService.parkingOwned(parkingOwnedRequest);
        call.enqueue(new Callback<ParkingOwnedResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkingOwnedResponse> call, @NonNull Response<ParkingOwnedResponse> response) {
                try {
                    if (response.code() == Constant.RESPONSE_SUCCESS_CODE && response.body().getErrcode() == Constant.ERROR_SUCCESS_CODE) {
                        List<ParkingResponse.DataBean.EstateBean> estateList = response.body().getData().getEstate();
                        if (estateList != null && estateList.size() > 0) {
                            mUserLockBeanDao.deleteAll();
                            UserLockBean userLockBean;
                            boolean isRented;
                            long currentTime = System.currentTimeMillis();
                            for (ParkingResponse.DataBean.EstateBean estate : estateList) {
                                for (ParkingResponse.DataBean.EstateBean.ParkingListBean parkingBean : estate.getParkingList()) {
                                    isRented = false;
                                    for (ParkingResponse.DataBean.EstateBean.ParkingListBean.ShareListBean shareListBean: parkingBean.getShareList()) {
                                        long startTime = shareListBean.getStartTime();
                                        long endTime = shareListBean.getEndTime();
                                        if (startTime <= currentTime && currentTime <= endTime) {
                                            isRented = true;
                                            break;
                                        }
                                    }
                                    userLockBean = new UserLockBean(null, estate.getName(), parkingBean.getName(), parkingBean.getId(), parkingBean.getGatewayId(),
                                            parkingBean.getLockMac(), parkingBean.getPassword(), isRented);
                                    mUserLocks.add(userLockBean);
                                    mUserLockBeanDao.insertOrReplace(userLockBean);
                                }
                            }
                            showLocks();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToast(mActivity, "服务器错误，请稍后再试");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ParkingOwnedResponse> call, @NonNull Throwable t) {
            }
        });
    }

    private void getPersistenceLocks() {
        Query<UserLockBean> userLockBeansQuery = mUserLockBeanDao.queryBuilder().orderAsc(UserLockBeanDao.Properties.Id).build();
        List<UserLockBean> lockList = userLockBeansQuery.list();
        if (lockList != null) {
            for (UserLockBean userLockBean : lockList) {
                mUserLocks.add(userLockBean);
            }
        }
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
            public void onButtonClick(View view, int i) {
                UserLockBean userLockBean = mUserLocks.get(i);
                mGateWayId = userLockBean.getGatewayId();
                mLockMac = userLockBean.getLockMac();
                mLockName = userLockBean.getParkingName();
                mLockPassword = userLockBean.getPassword();
                if (!hasNetwork()) {
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
        });
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

                    isPasswordAlreadySet = intent.getIntExtra(BLECommandIntent.EXTRA_MM_SET_ALREADY, -1) == 1;
                    // send password
                    LogUtil.d(TAG, "receive lock password " + mLockPassword);
                    Bundle data = new Bundle();
                    int[] password = new int[6];
                    for (int i = 0; i < 6; i++) {
                        password[i] = Integer.
                                valueOf(mLockPassword.substring(i, i + 1));
                    }
                    data.putIntArray(BLECommandIntent.EXTRA_PASSWORD, password);
                    data.putInt(BLECommandIntent.EXTRA_ROLE,
                            UserLockBean.LOCK_ROLE.OWNER.ordinal());

                    LogUtil.d(TAG, "isPassword already set " + isPasswordAlreadySet);
                    if (isPasswordAlreadySet) {
                        CommunicationManager.getInstance().sendBLEEvent(getActivity(), BLECommandIntent.CHECKING_PASSWORD, data);
                    } else {
                        CommunicationManager.getInstance().sendBLEEvent(getActivity(), BLECommandIntent.SETTING_PASSWORD, data);
                    }

                    int state = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, 3);
                    if (state == 1) {
                        mLockState = BluetoothClient.LOCK_STATE.DOWN.ordinal();
                    } else {
                        mLockState = BluetoothClient.LOCK_STATE.UP.ordinal();
                    }

                    break;
                case LockController.BROADCAST_CONNECT:
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
                    LogUtil.d(TAG, "actionId is " + actionId + ", result is " + result + ", isPassword set " + isPasswordAlreadySet);
                    if (actionId == 0x01) { // setting
                        checkPassword(result);
                    } else if (actionId == 0x02 && isPasswordAlreadySet) { // checking
                        checkPassword(result);
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

    private void checkPassword(int result) {
        if (result == 0x10) {
            // MM 代表密码
            LogUtil.d(TAG, "password is " + mLockPassword);
            Intent normal = new Intent(LockController.BROADCAST_CONNECT);
            mActivity.sendBroadcast(normal);
        } else {
            Intent errPassword = new Intent(LockController.BROADCAST_CONNECT);
            errPassword.putExtra("info", "密码错误");
            mActivity.sendBroadcast(errPassword);
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
                            if (hasNetwork()) {
                                Intent gateWayConnect = new Intent(mActivity, MqttManagerService.class);
                                gateWayConnect.setAction(MqttManagerService.ACTION_GATEWAY_CONNECT);
                                gateWayConnect.putExtra(MqttManagerService.GATEWAY_ID, mGateWayId);
                                gateWayConnect.putExtra(MqttManagerService.LOCK_MAC, mLockMac);
                                mActivity.startService(gateWayConnect);
                            } else {
                                if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                    ToastUtil.showToast(mActivity, "不支持蓝牙低能耗特性");
                                    dialog.dismiss();
                                } else {
                                    Intent bluetoothConnect = new Intent(mActivity, BluetoothManagerService.class);
                                    bluetoothConnect.setAction(BluetoothManagerService.ACTION_BLUETOOTH_CONNECT);
                                    bluetoothConnect.putExtra(BluetoothManagerService.EXTRA_LOCK_NAME, mLockName);
                                    mActivity.startService(bluetoothConnect);
                                }
                            }
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
                    Intent upLock;
                    if (hasNetwork()) {
                        upLock = new Intent(mActivity, MqttManagerService.class);
                        upLock.setAction(MqttManagerService.ACTION_UP_LOCK);
                    } else {
                        upLock = new Intent(mActivity, BluetoothManagerService.class);
                        upLock.setAction(BluetoothManagerService.ACTION_UP_LOCK);
                        upLock.putExtra(BluetoothManagerService.ACTION_LOCK_STATE, mLockState);
                    }
                    mActivity.startService(upLock);
                }

                @Override
                public void onLockDown(View view) {
                    Intent downLock;
                    if (hasNetwork()) {
                        downLock = new Intent(mActivity, MqttManagerService.class);
                        downLock.setAction(MqttManagerService.ACTION_DOWN_LOCK);
                    } else {
                        downLock = new Intent(mActivity, BluetoothManagerService.class);
                        downLock.setAction(BluetoothManagerService.ACTION_DOWN_LOCK);
                        downLock.putExtra(BluetoothManagerService.ACTION_LOCK_STATE, mLockState);
                    }
                    mActivity.startService(downLock);
                }
            });
            mControlLockDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                   disconnect();
                }
            });
        }
        mControlLockDialog.show();
    }

    private void disconnect() {
        LogUtil.d(TAG, "disconnect to bluetooth");
        Intent disConnectLock = new Intent(mActivity, BluetoothManagerService.class);
        disConnectLock.setAction(BluetoothManagerService.ACTION_DISCONNECT);
        mActivity.startService(disConnectLock);
    }

    private boolean hasNetwork() {
        return NetworkUtils.isConnected(mActivity);
    }
}
