package com.qhiehome.ihome.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.LockBean;
import com.qhiehome.ihome.ble.db.DatabaseHelper;
import com.qhiehome.ihome.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.ble.profile.IhomeService;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.manager.CommunicationManager;
import com.qhiehome.ihome.util.APPUtils;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 还有一些数据的回返方面比较模糊，有点奇怪，先看使用效果
 */
public class LockDetailActivity extends BaseActivity {

    private static final String TAG = LockDetailActivity.class.getSimpleName();

    private static final String MMCHECK_TAG = "needMMCheck";

    private ImageView mImgUp;
    private ImageView mImgDown;
    private ImageView mImgConnState;

    private int battery;
    private LockBean lock;

    private BroadcastReceiver mBroadcastReceiver;

    private enum PEDNGING_ACTION{
        DOUP, DODOWN, NOTHING,
    }
    private PEDNGING_ACTION mPendingAction;

    // 0: ERROR 1: LOW 2: MID 3: HIGH
    private enum LOCK_STATE {
        ERROR, DOWN, UPING, UP, DOWNING
    }

    private LOCK_STATE mLockState;

    private String mAddress;

    // CommunicationManager tell us lock is BLE connected or not, this state tells us MM state
    private enum MM_STATE {
        UNKNOWN_STATE, // ble connect -> unknown state
        CHECKING,      // unknown state -> checking
        AFTER_CHECK,   // checking -> AFTER_CHECK (success)
        CHECK_FAILED   // checking -> CHECK_FAILED
    }

    private MM_STATE mMMState;

    private static final int DELAY_DURATION = 1998;

    private static final int CANCEL_UP_MOVIE = 1001;
    private static final int CANCEL_DOWN_MOVIE = 1002;
    private static final int START_UP_MOVIE = 1003;
    private static final int START_DOWN_MOVIE = 1004;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_detail);

        mHandler = new LockDetailHandler(this);
        initView();

        boolean needMMCheck = getIntent().getBooleanExtra(MMCHECK_TAG, false);
        if (needMMCheck) {
            mAddress = APPUtils.getWatchAddress(this);
            battery = 100;
            mMMState = MM_STATE.UNKNOWN_STATE;
            mLockState = LOCK_STATE.DOWN;
        } else {
            mAddress = APPUtils.getWatchAddress(this);
            int state = getIntent().getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, 1);
            if (state == 1) {
                mLockState = LOCK_STATE.DOWN;
            } else {
                mLockState = LOCK_STATE.UP;
            }
            mMMState = MM_STATE.AFTER_CHECK;
            battery = getIntent().getIntExtra(BLECommandIntent.EXTRA_BATTERY_LEVEL, 100);
            processBattery(true);
        }
        // query database
        ArrayList<LockBean> allLocks = DatabaseHelper.getInstance().getAllLocks();
        for (LockBean bean: allLocks) {
            if (bean.getAddress().equals(mAddress)) {
                lock = bean;
            }
        }
        if (lock == null) {
            lock = new LockBean(mAddress, APPUtils.getWatchName(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();

        processConnectInfo();
    }

    private void processConnectInfo() {
        if(CommunicationManager.getInstance().isBleConnected()){
            if(mMMState == MM_STATE.UNKNOWN_STATE | mMMState == MM_STATE.CHECKING){
                mImgConnState.setBackgroundResource(R.drawable.icon_device_disconnect);
            }else if(mMMState == MM_STATE.AFTER_CHECK){
                mImgConnState.setBackgroundResource(R.drawable.icon_device_connect);
            }
        }else if(CommunicationManager.getInstance().isBleConnecting() ){
            // no change
        }else {
            mImgConnState.setBackgroundResource(R.drawable.icon_device_disconnect);
            if(!CommunicationManager.getInstance().HOST_DISCONNECT &&
                    mMMState != MM_STATE.AFTER_CHECK &&  lock.getRole() == LockBean.LOCK_ROLE.OWNER.ordinal()){
                connectToDevice();
            }
        }
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            IntentFilter localIntentFilter = new IntentFilter(BLECommandIntent.RX_CURRENT_STATUS);
            localIntentFilter.addAction(CommunicationManager.ACTION_CONNECTION_STATE_CHANGE);
            localIntentFilter.addAction(BLECommandIntent.RX_PASSWORD_RESULT);
            localIntentFilter.addAction(BLECommandIntent.RX_LOCK_AUTO_PARK);
            localIntentFilter.addAction(BLECommandIntent.RX_LOCK_RESULT);

            // rf
            localIntentFilter.addAction(BLECommandIntent.RX_LOCK_RF_START_UP);
            localIntentFilter.addAction(BLECommandIntent.RX_LOCK_RF_STOP_UP);
            localIntentFilter.addAction(BLECommandIntent.RX_LOCK_RF_LOCK_STATE);

            mBroadcastReceiver = new EventReceiver();
            registerReceiver(mBroadcastReceiver, localIntentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, TAG + " onPause()");
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void initView() {
        initToolbar();
        initImgs();
    }

    private void initImgs() {
        mImgDown = (ImageView) findViewById(R.id.img_down);
        mImgUp = (ImageView) findViewById(R.id.img_up);
        mImgConnState = (ImageView) findViewById(R.id.img_conn_state);
        initImgListeners();
    }

    private void initImgListeners() {
        mImgDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "down pressed");
                if (!CommunicationManager.getInstance().isBleConnected()) {
                    mPendingAction = PEDNGING_ACTION.DODOWN;
                    connectToDevice();
                    return;
                }
                doDown();
            }
        });
        mImgUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "up pressed");
                if (!CommunicationManager.getInstance().isBleConnected()) {
                    mPendingAction = PEDNGING_ACTION.DOUP;
                    connectToDevice();
                    return;
                }
                doUp();
            }
        });
        mImgConnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommunicationManager.getInstance().isBleConnectingOrConnected()) {
                    disconnectToDevice();
                } else {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(!mBluetoothAdapter.isEnabled()){
                        ToastUtil.showToast(LockDetailActivity.this, "Please open the bluetooth firstly");
                        return;
                    }
                    connectToDevice();
                }
            }
        });
    }

    private void connectToDevice() {
        // connect device
        Intent intent = new Intent(this, IhomeService.class);
        intent.putExtra(CommunicationManager.EXTRA_ADDRESS, lock.getAddress());
        intent.putExtra(CommunicationManager.EXTRA_NAME, lock.getName());

        intent.setAction(CommunicationManager.ACTION_CONNECT_TO_DEVICE);
        startService(intent);
    }

    private void disconnectToDevice() {
        LogUtil.i(TAG, "enter disConncetToDevice ");
        // connect device
        Intent intent = new Intent(this, IhomeService.class);
        intent.setAction(CommunicationManager.ACTION_DISCONNECT_TO_DEVICE);
        startService(intent);

        //process state change here
        if(mImgConnState != null)
            mImgConnState.setBackgroundResource(R.drawable.icon_device_disconnect);
        mPendingAction = PEDNGING_ACTION.NOTHING;
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private static class LockDetailHandler extends Handler {
        private WeakReference<LockDetailActivity> mActivity;

        private LockDetailHandler(LockDetailActivity mLockDetailActivity) {
            mActivity = new WeakReference<>(mLockDetailActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            LockDetailActivity lockDetailActivity = mActivity.get();
            if (lockDetailActivity != null) {
                switch (msg.what) {
                    case LockDetailActivity.CANCEL_DOWN_MOVIE:
                        removeMessages(START_UP_MOVIE);
                        removeMessages(START_DOWN_MOVIE);
                        break;
                    case LockDetailActivity.CANCEL_UP_MOVIE:
                        removeMessages(START_UP_MOVIE);
                        removeMessages(START_DOWN_MOVIE);
                        break;
                    case LockDetailActivity.START_DOWN_MOVIE:
                        removeMessages(START_UP_MOVIE);
                        removeMessages(START_DOWN_MOVIE);
                        lockDetailActivity.mLockState = LOCK_STATE.DOWNING;
                        sendEmptyMessageDelayed(START_DOWN_MOVIE, DELAY_DURATION);
                        break;
                    case LockDetailActivity.START_UP_MOVIE:
                        removeMessages(START_UP_MOVIE);
                        removeMessages(START_DOWN_MOVIE);
                        lockDetailActivity.mLockState = LOCK_STATE.UPING;
                        sendEmptyMessageDelayed(START_UP_MOVIE, DELAY_DURATION);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void doDown() {
        if(mLockState == LOCK_STATE.DOWN){
            LogUtil.i(TAG, "do nothing, down");
            ToastUtil.showToast(this, "Already the willing status");
        }else if(mLockState == LOCK_STATE.DOWNING){
            LogUtil.i(TAG, "do nothing.downing");
        }else{
            LogUtil.i(TAG, "send down msg");
            Intent intent = new Intent(BLECommandIntent.SEND_BUTTON_EVENT);
            intent.putExtra(BLECommandIntent.EXTRA_IS_OWNER, lock.getRole() == LockBean.LOCK_ROLE.OWNER.ordinal());
            intent.putExtra(BLECommandIntent.EXTRA_IS_UP, false);
            CommunicationManager.getInstance().sendBLEEvent(this, intent);
            mHandler.sendEmptyMessage(START_DOWN_MOVIE);
        }

    }

    protected void doUp() {
        if(mLockState == LOCK_STATE.UP){
            ToastUtil.showToast(this, "Already the willing status");
        }else if(mLockState == LOCK_STATE.UPING){
            LogUtil.i(TAG, "do nothing uping");
        }else{
            LogUtil.i(TAG, "send up msg");
            Intent intent = new Intent(
                    BLECommandIntent.SEND_BUTTON_EVENT);
            intent.putExtra(BLECommandIntent.EXTRA_IS_OWNER, lock.getRole() == LockBean.LOCK_ROLE.OWNER.ordinal());
            intent.putExtra(BLECommandIntent.EXTRA_IS_UP, true);
            CommunicationManager.getInstance().sendBLEEvent(this, intent);
            mHandler.sendEmptyMessage(START_UP_MOVIE);
        }
    }

    public void checkMM(Bundle data) {
        LogUtil.d(TAG, "checkMM");
        mMMState = MM_STATE.CHECKING;
        int[] mm = new int[6];
        String password = lock.getRole() == LockBean.LOCK_ROLE.OWNER.ordinal() ? lock.getPassword() : lock.getCustomerPassword();
        //no password, goto password check page
        if(password.equals("")){
            goBack();
            return;
        }
        //if has password,check it
        for (int i = 0; i < 6; i++) {
            mm[i] = Integer.valueOf(password.substring(i, i+1));
        }
        data.clear();
        data.putIntArray(BLECommandIntent.EXTRA_PASSWORD, mm);
        data.putInt(BLECommandIntent.EXTRA_ROLE,lock.getRole());
        CommunicationManager.getInstance().sendBLEEvent(this, BLECommandIntent.CHECKING_PASSWORD, data);
    }

    //state Lock Status[3--0]:  0x01(Lock_low)  /  0x02(Lock_Mid)	/	0x03(Lock_High)
    private void processLockState(){
        LogUtil.i(TAG, "processState: "+ mLockState.ordinal());
        if(!(mLockState == LOCK_STATE.DOWN | mLockState == LOCK_STATE.UPING)){
            mLockState = LOCK_STATE.UP;
        }
    }

    private void doPendingAction(){
        if(mPendingAction == PEDNGING_ACTION.DOUP){
            mPendingAction = PEDNGING_ACTION.NOTHING;
            LogUtil.d(TAG, "mPendingAction == PEDNGING_ACTION.DOUP");
            doUp();
        }else if(mPendingAction == PEDNGING_ACTION.DODOWN){
            mPendingAction = PEDNGING_ACTION.NOTHING;
            LogUtil.d(TAG, "mPendingAction == PEDNGING_ACTION.DODOWN");
            doDown();
        }
    }

    protected void goBack() {
        LogUtil.d(TAG, "go to the previous activity");
        mMMState = MM_STATE.UNKNOWN_STATE;
        if(mBroadcastReceiver != null){
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        finish();
    }

    public void clearMM() {
        if(lock == null)
            return;

        DatabaseHelper.getInstance().updatePassword("",
                lock.getRole() == LockBean.LOCK_ROLE.OWNER.ordinal(),
                lock.getAddress());

        Bundle data = new Bundle();
        data.putInt(BLECommandIntent.EXTRA_MM_SET_ALREADY, 1);
        data.putInt(BLECommandIntent.EXTRA_LOCK_STATE,mLockState.ordinal());
        data.putInt(BLECommandIntent.EXTRA_BATTERY_LEVEL, battery);

        goBack();
    }

    private void processBattery(boolean showLowBatteryToast) {
        LogUtil.i(TAG, "processBattery: "+ battery);
        if (battery < 20) {
            if(showLowBatteryToast && CommunicationManager.getInstance().isBleConnected())
                ToastUtil.showToast(this, "Battery low, please change new batteries");
        }

    }

    public static void start(Context context, boolean isNeedMMCheck) {
        Intent intent = new Intent(context, LockDetailActivity.class);
        intent.putExtra(MMCHECK_TAG, isNeedMMCheck);
        context.startActivity(intent);
    }

    private class EventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "mReceiver: action = " + action + ", isUping: " + (mLockState == LOCK_STATE.UPING));
            switch (action) {
                case BLECommandIntent.RX_LOCK_RESULT:
                    int result = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_RESULT, 10);
                    // success
                    if (result == 0x10 || result == 0x20) {
                        if (mLockState == LOCK_STATE.UPING) {
                            mLockState = LOCK_STATE.UP;
                        } else {
                            mLockState = LOCK_STATE.DOWN;
                        }
                    } else if (result == 0x30) {
                        if (mLockState == LOCK_STATE.DOWNING) {
                            mHandler.sendEmptyMessage(CANCEL_UP_MOVIE);
                            mLockState = LOCK_STATE.ERROR;
                            ToastUtil.showToast(LockDetailActivity.this, "Failed to fall down the barrier");
                        } else {
                            mHandler.sendEmptyMessage(CANCEL_DOWN_MOVIE);
                            mLockState = LOCK_STATE.ERROR;
                            ToastUtil.showToast(LockDetailActivity.this, "Failed to raise up the barrier");
                        }
                    }
                    break;
                case BLECommandIntent.RX_LOCK_AUTO_PARK:
                    // TODO: 2017/7/17
                    break;
                case BLECommandIntent.RX_CURRENT_STATUS:
                    int mmSet = intent.getIntExtra(BLECommandIntent.EXTRA_MM_SET_ALREADY, 1);
                    int state = intent.getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, 3);
                    if (state == 1) {
                        mLockState = LOCK_STATE.DOWN;
                    } else {
                        mLockState = LOCK_STATE.UP;
                    }
                    battery = intent.getIntExtra(BLECommandIntent.EXTRA_BATTERY_LEVEL, 100);
                    LogUtil.d(TAG, "mmState: " + mmSet + ", lockState: " + state + ", battery: " + battery);
                    if (mmSet == 1) {
                        checkMM(intent.getExtras());
                    } else {
                        goBack();
                    }
                    break;
                case CommunicationManager.ACTION_CONNECTION_STATE_CHANGE:
                    int status = intent.getIntExtra(CommunicationManager.EXTRA_CONNECTION_STATE_NEW, CommunicationManager.BLE_INIT_CONNECT);
                    if (status == CommunicationManager.BLE_DISCONNECTED) {
                        mImgConnState.setBackgroundResource(R.drawable.icon_device_disconnect);
                        mMMState = MM_STATE.UNKNOWN_STATE;
                        mImgConnState.invalidate();
                        if (mLockState == LOCK_STATE.UPING) {
                            mHandler.sendEmptyMessage(CANCEL_UP_MOVIE);
                        } else if (mLockState == LOCK_STATE.DOWNING) {
                            mHandler.sendEmptyMessage(CANCEL_DOWN_MOVIE);
                        } else if(mLockState == LOCK_STATE.UP){
                            LogUtil.i(TAG,"mLockState == LOCK_STATE.UP");
                        } else if(mLockState == LOCK_STATE.DOWN){
                            LogUtil.i(TAG,"mLockState == LOCK_STATE.DOWN");
                        }
                        mPendingAction = PEDNGING_ACTION.NOTHING;
                    } else if (status == CommunicationManager.BLE_CONNECTED | status == CommunicationManager.BLE_CONNECTING) {
                        mMMState = MM_STATE.UNKNOWN_STATE;
                        mImgConnState.setImageResource(R.drawable.icon_device_disconnect);
                    }
                    break;
                case BLECommandIntent.RX_PASSWORD_RESULT:
                    int actionId = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_ACTION, -1);
                    result = intent.getIntExtra(BLECommandIntent.EXTRA_PSW_RESULT, -1);
                    if (actionId == 0x02 && result != 0x30) {
                        mImgConnState.setImageResource(R.drawable.icon_device_connect);
                        processLockState();
                        processBattery(mMMState == MM_STATE.CHECKING);
                        doPendingAction();
                        mMMState = MM_STATE.AFTER_CHECK;

                        if(result == 0x10){
                            String cumstomerMM = intent.getStringExtra(BLECommandIntent.EXTRA_PSW_CUSTOMER);
                            if(cumstomerMM != null && !cumstomerMM.equals("")){
                                DatabaseHelper.getInstance().updatePassword(cumstomerMM, false, lock.getAddress());
                                lock.setCustomerPassword(cumstomerMM);
                            }
                        }
                    } else if (actionId == 0x02 && result == 0x30 && mMMState == MM_STATE.CHECKING) {
                        ToastUtil.showToast(LockDetailActivity.this, "Wrong password");
                        clearMM();
                    }
                    break;
                case BLECommandIntent.RX_LOCK_RF_START_UP:
                    mHandler.sendEmptyMessage(START_UP_MOVIE);
                    break;
                case BLECommandIntent.EXTRA_LOCK_STOP_UP:
                    boolean success = intent.getBooleanExtra(BLECommandIntent.EXTRA_LOCK_STOP_UP, false);
                    if(success){
                        mLockState = LOCK_STATE.UP;
                    }else{
                        ToastUtil.showToast(LockDetailActivity.this, "Failed to raise up the barrier");
                        mLockState = LOCK_STATE.ERROR;
                    }
                    break;
                case BLECommandIntent.RX_LOCK_RF_LOCK_STATE:
                    boolean isDown = intent.getBooleanExtra(BLECommandIntent.EXTRA_LOCK_RF_LOCK_STATE, false);
                    if(isDown){
                        mLockState = LOCK_STATE.DOWN;
                    }else{
                        mLockState = LOCK_STATE.UP;
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
