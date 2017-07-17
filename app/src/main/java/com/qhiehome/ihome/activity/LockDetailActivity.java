package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.bean.LockBean;
import com.qhiehome.ihome.ble.profile.BLECommandIntent;
import com.qhiehome.ihome.ble.profile.IhomeService;
import com.qhiehome.ihome.manager.ActivityManager;
import com.qhiehome.ihome.manager.CommunicationManager;
import com.qhiehome.ihome.util.APPUtils;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

public class LockDetailActivity extends AppCompatActivity {

    private static final String TAG = LockDetailActivity.class.getSimpleName();

    private ImageView mImgUp;
    private ImageView mImgDown;

    private String mDeviceName;

    private enum PEDNGING_ACTION{
        DOUP, DODOWN, NOTHING,
    }
    private PEDNGING_ACTION mPendingAction;

    private LockBean lock;

    private enum LOCK_STATE{
        ERROR,  DOWN,  UPING, UP,DOWNING,
        //0:ERROR 1:LOW 2:MID 3:HIGH
    }
    private LOCK_STATE mLockState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_detail);
        ActivityManager.add(this);

        // status, when first enter, needMMCheck means not connect yet, so owner
        // need to auto connect, otherwise, needMMCheck means receive connect
        // success info, need check MM
        boolean needMMCheck = getIntent().getBooleanExtra("needMMCheck", false);
        if (needMMCheck) {
            mLockState = LOCK_STATE.DOWN;
        } else {
            int state = getIntent().getIntExtra(BLECommandIntent.EXTRA_LOCK_STATE, 0);
            if (state == 1) {
                mLockState = LOCK_STATE.DOWN;
            } else {
                mLockState = LOCK_STATE.UP;
            }
        }

        initView();
    }

    private void initView() {
        initToolbar();
        initImgs();
    }

    private void initImgs() {
        mImgDown = (ImageView) findViewById(R.id.img_down);
        mImgUp = (ImageView) findViewById(R.id.img_up);
        initImgListeners();
    }

    private void initImgListeners() {
        mImgDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "down pressed");
                if(!CommunicationManager.getInstance().isBleConnected()){
                    mPendingAction = PEDNGING_ACTION.DODOWN;
                    conncetToDevice();
                    return;
                }
                doDown();
                mLockState = LOCK_STATE.DOWN;
            }
        });
        mImgUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "up pressed");
                if(!CommunicationManager.getInstance().isBleConnected()){
                    mPendingAction = PEDNGING_ACTION.DOUP;
                    conncetToDevice();
                    return;
                }
                doUp();
                mLockState = LOCK_STATE.UP;
            }
        });
    }

    protected void doDown() {
        if(mLockState == LOCK_STATE.DOWN){
            LogUtil.i(TAG, "do nothing, down");
            ToastUtil.showToast(this, "already the willing status");
        }else if(mLockState == LOCK_STATE.DOWNING){
            LogUtil.i(TAG, "do nothing.downing");
        }else{
            LogUtil.i(TAG, "send down msg");
            Intent intent = new Intent(BLECommandIntent.SEND_BUTTON_EVENT);
            intent.putExtra(BLECommandIntent.EXTRA_IS_OWNER, true);
            intent.putExtra(BLECommandIntent.EXTRA_IS_UP, false);
            CommunicationManager.getInstance().sendBLEEvent(this, intent);
        }

    }

    protected void doUp() {
        if(mLockState == LOCK_STATE.UP){
            ToastUtil.showToast(this, "already the willing status");
        }else if(mLockState == LOCK_STATE.UPING){
            LogUtil.i(TAG, "do nothing uping");
        }else{
            LogUtil.i(TAG, "send up msg");
            Intent intent = new Intent(
                    BLECommandIntent.SEND_BUTTON_EVENT);
            intent.putExtra(BLECommandIntent.EXTRA_IS_OWNER, true);
            intent.putExtra(BLECommandIntent.EXTRA_IS_UP, true);
            CommunicationManager.getInstance().sendBLEEvent(this, intent);
        }
    }

    private void conncetToDevice() {
        // connect device
        Intent intent = new Intent(this, IhomeService.class);
        intent.putExtra(CommunicationManager.EXTRA_ADDRESS, APPUtils.getWatchAddress(this));
        intent.putExtra(CommunicationManager.EXTRA_NAME, APPUtils.getWatchName(this));

        intent.setAction(CommunicationManager.ACTION_CONNECT_TO_DEVICE);
        startService(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LockDetailActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, boolean isNeedMMCheck) {
        Intent intent = new Intent(context, LockDetailActivity.class);
        intent.putExtra("needMMCheck", isNeedMMCheck);
        context.startActivity(intent);
    }
}
