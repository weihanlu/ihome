package com.qhiehome.ihome.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.BindLockAdapter;
import com.qhiehome.ihome.bean.BLEDevice;
import com.qhiehome.ihome.bean.LockBean;
import com.qhiehome.ihome.ble.db.DatabaseHelper;
import com.qhiehome.ihome.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.ble.profile.IhomeService;
import com.qhiehome.ihome.manager.CommunicationManager;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.RecyclerViewEmptySupport;

import java.util.ArrayList;

public class BindLockActivity extends BaseActivity {

    private static final String TAG = BindLockActivity.class.getSimpleName();

    private static final int REQUEST_CODE = 1;

    private ArrayList<BLEDevice> mLeDevices;
    private BindLockAdapter bindLockAdapter;

    private ImageView mImgBluetooth;
    private ImageView mImgNetwork;

    private EventReceiver mEventReceiver;

    private boolean setAlready;
    private String mDeviceName;
    private String mDeviceAddress;

    // data in db
    private LockBean lock;
    private String mLockPassword;

    private boolean isOwner = true;

    private int tryCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_lock);
        mLeDevices = new ArrayList<>();
        initView();

        // start ihome service
        Intent intent = new Intent(this, IhomeService.class);
        intent.setAction(CommunicationManager.ACTION_UI_LAUNCH);
        startService(intent);
        // register connection status change broadcast
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        if (mEventReceiver == null) {
            IntentFilter localIntentFilter = new IntentFilter(BLECommandIntent.RX_CURRENT_STATUS);
            localIntentFilter.addAction(CommunicationManager.ACTION_DEVICE_FIRST_TIME);
            localIntentFilter.addAction(CommunicationManager.ACTION_DEVICE_FIRST_TIMEOUT);
            localIntentFilter.addAction(BLECommandIntent.RX_PASSWORD_RESULT);
            localIntentFilter.addAction(CommunicationManager.ACTION_CONNECTION_STATE_CHANGE);
            mEventReceiver = new EventReceiver();
            registerReceiver(mEventReceiver, localIntentFilter);
        }
    }

    private void initView() {
        initRecyclerView();
        initImgs();
    }

    private void initImgs() {
        mImgBluetooth = (ImageView) findViewById(R.id.img_bluetooth);
        mImgNetwork = (ImageView) findViewById(R.id.img_network);
        initImgListener();
    }

    private void initImgListener() {
        mImgBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BindLockActivity.this, BluetoothScanActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        mImgNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopConnect = new Intent(BindLockActivity.this, IhomeService.class);
                stopConnect.setAction(CommunicationManager.ACTION_DISCONNECT_TO_DEVICE);
                startService(stopConnect);
                ToastUtil.showToast(BindLockActivity.this, "click to stop connect");
            }
        });
    }

    private void initRecyclerView() {
        RecyclerViewEmptySupport rv = (RecyclerViewEmptySupport) findViewById(R.id.rv_list_empty);
        TextView tvListEmpty = (TextView) findViewById(R.id.tv_list_empty);
        rv.setEmptyView(tvListEmpty);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        bindLockAdapter = new BindLockAdapter(this, mLeDevices);
        initListener(bindLockAdapter);
        rv.setAdapter(bindLockAdapter);
    }

    private void initListener(final BindLockAdapter bindLockAdapter) {
        bindLockAdapter.setOnItemClickListener(new BindLockAdapter.OnClickListener() {
            @Override
            public void onClick(int i) {
                BLEDevice device = mLeDevices.get(i);
                // start IhomeService
                Intent startHomeService = new Intent(BindLockActivity.this, IhomeService.class);
                startHomeService.putExtra(CommunicationManager.EXTRA_ADDRESS, device.getAddress());
                startHomeService.putExtra(CommunicationManager.EXTRA_NAME, device.getName());
                startHomeService.setAction(CommunicationManager.ACTION_CONNECT_TO_DEVICE);
                startService(startHomeService);
                LogUtil.d(TAG, "Starting ihome service -- " + device.getName());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            BLEDevice bleDevice = data.getParcelableExtra(BluetoothScanActivity.DEVICE_DATA);
            if (!mLeDevices.contains(bleDevice)) {
                mLeDevices.add(bleDevice);
            } else {
                ToastUtil.showToast(this, "已添加过该设备");
            }
            bindLockAdapter.notifyDataSetChanged();
        }
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, BindLockActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEventReceiver != null) {
            unregisterReceiver(mEventReceiver);
            mEventReceiver = null;
        }
    }

    private AlertDialog mDialog;
    TextView mDialogTitle;
    TextView mDialogDesc;
    Button mBtnOK;

    private void showTip(String title, String message, boolean cancelable, boolean showBtn) {
        if (message.isEmpty()) {
            return;
        }
        if (mDialog == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.alert_dialog, null);// 这里的R.layout.alertdialog即为你自定义的布局文件
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);
            mDialog = builder.create();
            mDialogTitle = (TextView) view.findViewById(R.id.title);
            mDialogDesc = (TextView) view.findViewById(R.id.content);
            mBtnOK = (Button)view.findViewById(R.id.sure);
            mBtnOK.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mDialog.cancel();
                }
            });
        }

        mBtnOK.setVisibility(showBtn?View.VISIBLE : View.GONE);
        mDialogTitle.setText(title);
        mDialogDesc.setText(message);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private class EventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "onReceive: action = " + action);
            switch (action) {
                case BLECommandIntent.RX_CURRENT_STATUS:
                    LogUtil.d(TAG, "BLE device connected...");
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.cancel();
                    }
                    // 跳转
                    checkPassword(intent.getExtras());
                    break;
                case CommunicationManager.ACTION_DEVICE_FIRST_TIME:
                    showTip("Connecting", "First-time Connection takes longer time, please wait", true, false);
                    break;
                case CommunicationManager.ACTION_DEVICE_FIRST_TIMEOUT:
                    showTip("Connection not succeed", "Please close and reopen the Bluetooth", true, true);
                    break;
                case BLECommandIntent.RX_PASSWORD_RESULT:
                    int actionId = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_ACTION, -1);
                    int result = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_RESULT, -1);
                    String password;
                    if (actionId == 0x01 && !setAlready) { // setting
                        if (result == 0x10) {
                            // add to database
                            LockBean lock = new LockBean(mDeviceAddress, mDeviceName, LockBean.LOCK_ROLE.OWNER.ordinal(),
                                    LockBean.LOCK_DEFAULT.DEFAULT.ordinal(), CommunicationManager.getInstance().DEVICE_REAL_NAME);
                            password = mLockPassword;
                            lock.setPassword(password);
                            lock.setCustomerPassword(password);
                            DatabaseHelper.getInstance().insertLock(lock);
                            // 跳转
                            jumpToLockDetailActivity();
                        } else {
                            ToastUtil.showToast(BindLockActivity.this, "Wrong password");
                        }
                    } else if (actionId == 0x02 && setAlready) { // check
                        if ((result == 0x10 && isOwner) || (result == 0x20 && !isOwner)) {
                            if (lock != null) {
                                ContentValues cv = new ContentValues();
                                boolean change = false;
                                if (lock.getRole() != (isOwner? LockBean.LOCK_ROLE.OWNER.ordinal(): LockBean.LOCK_ROLE.CUSTOMER.ordinal())) {
                                    change = true;
                                    cv.put(DatabaseHelper.FIELD_LOCK_ROLE, isOwner? LockBean.LOCK_ROLE.OWNER.ordinal(): LockBean.LOCK_ROLE.CUSTOMER.ordinal());
                                }
                                cv.put(DatabaseHelper.FIELD_LOCK_DEFAULT, LockBean.LOCK_DEFAULT.DEFAULT.ordinal());
                                if (!lock.getName().equals(mDeviceName)) {
                                    change = true;
                                    cv.put(DatabaseHelper.FIELD_LOCK_NAME, mDeviceName);
                                }
                                if (isOwner) {
                                    password = Constant.DEFAULT_PASSWORD;
                                    if (lock.getPassword().equals(password)) {
                                        change = true;
                                        cv.put(DatabaseHelper.FIELD_LOCK_PASSWORD, password);
                                    }
                                }
                                if (change) {
                                    DatabaseHelper.getInstance().updateLock(cv, "address=?",
                                                    new String[] { mDeviceAddress });
                                }
                            } else {
                                LockBean lock = new LockBean(mDeviceAddress, mDeviceName,
                                        isOwner ? LockBean.LOCK_ROLE.OWNER
                                                .ordinal()
                                                : LockBean.LOCK_ROLE.CUSTOMER
                                                .ordinal(), LockBean.LOCK_DEFAULT.DEFAULT.ordinal(),CommunicationManager.getInstance().DEVICE_REAL_NAME);
                                if (isOwner){
                                    password = Constant.DEFAULT_PASSWORD;
                                    lock.setPassword(password);
                                }
                                DatabaseHelper.getInstance().insertLock(lock);
                            }
                            jumpToLockDetailActivity();
                        } else if (result == 0x30) {
                            // fail
                            tryCount++;
                            if (tryCount >= 5) {
                                ToastUtil.showToast(BindLockActivity.this, "5 times incorrected");
                                if (setAlready && lock != null) {
                                    DatabaseHelper.getInstance().updatePassword("", isOwner, lock.getAddress());
                                    LogUtil.i(TAG, "check password fail > 5, clear password in database");
                                }
                                // insert this lock into untrusted locks
                                DatabaseHelper.getInstance().insertIntoUntrusttedLocks(mDeviceAddress);
                                return;
                            }
                            ToastUtil.showToast(BindLockActivity.this, "password incorrected, you have " + (5 - tryCount) + " chances left");
                        }
                    }
                    break;
                case CommunicationManager.ACTION_CONNECTION_STATE_CHANGE:
                    int status = intent.getIntExtra(CommunicationManager.EXTRA_CONNECTION_STATE_NEW, CommunicationManager.BLE_INIT_CONNECT);
                    if (status == CommunicationManager.BLE_DISCONNECTED) {
                        ToastUtil.showToast(BindLockActivity.this, "连接设备已断开");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void jumpToLockDetailActivity() {
        LockDetailActivity.start(BindLockActivity.this, false);
    }

    private void checkPassword(Bundle data) {
        if (data == null) {
            ToastUtil.showToast(this, "check failed");
        }
        // data of current status.
        setAlready = data.getInt(BLECommandIntent.EXTRA_MM_SET_ALREADY, 1) == 1;

        // for debug
        mDeviceName = data.getString(BLECommandIntent.EXTRA_LOCK_NAME, "");
        mDeviceAddress = data.getString(BLECommandIntent.EXTRA_LOCK_ADDRESS, "");
        // if saved before
        if (setAlready) {
            lock = DatabaseHelper.getInstance().queryLockBean(mDeviceAddress);
        }
        if (setAlready) {
            if (lock != null && !TextUtils.isEmpty(lock.getPassword())) {
                mLockPassword = lock.getPassword();
            }
        }
        if (TextUtils.isEmpty(mLockPassword)) {
            mLockPassword = Constant.DEFAULT_PASSWORD;
        }
        // send password
        Bundle bundle = new Bundle();
        int[] mm = new int[6];
        for (int i = 0; i < 6; i++) {
            mm[i] = Integer.valueOf(mLockPassword.substring(i, i + 1));
        }
        if (setAlready) {
            bundle.putIntArray(BLECommandIntent.EXTRA_PASSWORD, mm);
            // this only one type of user: owner
            bundle.putInt(BLECommandIntent.EXTRA_ROLE, LockBean.LOCK_ROLE.OWNER.ordinal());
            CommunicationManager.getInstance().sendBLEEvent(this, BLECommandIntent.CHECKING_PASSWORD, bundle);
        } else {
            bundle.putIntArray(BLECommandIntent.EXTRA_PASSWORD, mm);
            bundle.putInt(BLECommandIntent.EXTRA_ROLE, LockBean.LOCK_ROLE.OWNER.ordinal());
            CommunicationManager.getInstance().sendBLEEvent(this, BLECommandIntent.SETTING_PASSWORD, bundle);
        }

    }
}
