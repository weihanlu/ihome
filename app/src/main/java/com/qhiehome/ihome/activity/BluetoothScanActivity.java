package com.qhiehome.ihome.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.ScanLockAdapter;
import com.qhiehome.ihome.bean.BLEDevice;
import com.qhiehome.ihome.util.Constant;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;
import com.qhiehome.ihome.view.RecyclerViewEmptySupport;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BluetoothScanActivity extends BaseActivity {

    private static final String TAG = BluetoothScanActivity.class.getSimpleName();

    public static final String DEVICE_DATA = "ble_device";

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;

    private static final int REQUEST_ENABLE_BT = 0X1;
    private static final int START_SCAN = 0x2;
    private static final int STOP_SCAN = 0x3;

    private Handler mHandler;

    // used to store all bluetooth low energy devices.
    private ArrayList<BLEDevice> mLeDevices;
    private ScanLockAdapter mScanLockAdapter;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 6000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        detectBLE();
        enableBluetooth();

        mHandler = new ScanHandler(this);
        mLeDevices = new ArrayList<>();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter.isEnabled()) {
//            if (!CommunicationManager.getInstance().isBleConnectingOrConnected()) {
//                LogUtil.i(TAG, "onResume, scanLeDevice(true)");
//                scanLeDevice(true);
//            }
        }
    }

    private void scanLeDevice(boolean isScan) {
        if (isScan) {
            mHandler.sendEmptyMessage(START_SCAN);
            LogUtil.d(TAG, "start scan");
        } else {
            mHandler.sendEmptyMessage(STOP_SCAN);
            LogUtil.d(TAG, "stop scan");
        }
    }

    /**
     * Use this check to determine whether BLE is supported on the device. Then
     * you can selectively disable BLE-related features.
     */
    private void detectBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            LogUtil.d(Constant.GLOBAL_FILTER, "This device does not support bluetooth low energy");
            ToastUtil.showToast(this, "This device does not support bluetooth low energy");
            finish();
        }
    }

    /**
     * Ensures the Bluetooth is available on the device and it is enabled. If not,
     * displays a dialog requesting user permission to enable Bluetooth.
     */
    private void enableBluetooth() {
        // Initializes Bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        LogUtil.d(TAG, "mBluetoothAdapter: " + mBluetoothAdapter);
        if (mBluetoothAdapter == null) {
            ToastUtil.showToast(this, "Bluetooth Adapter init failed");
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            LogUtil.d(TAG, "bluetooth enable successfully");
            scanLeDevice(true);
        } else if (requestCode == RESULT_CANCELED) {
            LogUtil.d(TAG, "bluetooth enable failed");
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        initToolbar();
        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerViewEmptySupport rv = (RecyclerViewEmptySupport) findViewById(R.id.rv_list_empty);
        TextView tvListEmpty = (TextView) findViewById(R.id.tv_list_empty);
        rv.setEmptyView(tvListEmpty);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        mScanLockAdapter = new ScanLockAdapter(this, mLeDevices);
        initListener(mScanLockAdapter);
        rv.setAdapter(mScanLockAdapter);
    }

    private void initListener(ScanLockAdapter mScanLockAdapter) {
        mScanLockAdapter.setOnItemClickListener(new ScanLockAdapter.OnClickListener() {
            @Override
            public void onClick(int i) {
                scanLeDevice(false);
                BLEDevice bleDevice = mLeDevices.get(i);
                // 传输数据给上一层Activity
                Intent intent = new Intent();
                intent.putExtra(DEVICE_DATA, bleDevice);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_bluetooth_scan:
                        scanLeDevice(true);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bluetooth_scan, menu);
        return true;
    }

    @Override
    protected void onStop() {
        scanLeDevice(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // filter parking lock device
            String deviceAddress = device.getAddress();
            String deviceName = device.getName();
            String lock_prefix = getResources().getString(R.string.lock_prefix);
            if (!TextUtils.isEmpty(deviceName) && deviceName.startsWith(lock_prefix)) {
                BLEDevice bleDevice = new BLEDevice(deviceName, deviceAddress);
                if (!mLeDevices.contains(bleDevice)) {
                    LogUtil.i(TAG, "add a new device: name = " + deviceName + " && address = " + deviceAddress);
                    mLeDevices.add(bleDevice);
                    mScanLockAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * Instances of static inner classes do not hold an implicit
     * reference to their outer classes.
     */
    private static class ScanHandler extends Handler {

        private final WeakReference<BluetoothScanActivity> mActivity;

        private ScanHandler(BluetoothScanActivity bluetoothScanActivity) {
            mActivity = new WeakReference<>(bluetoothScanActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothScanActivity bluetoothScanActivity = mActivity.get();
            if (bluetoothScanActivity != null) {
                switch (msg.what) {
                    case START_SCAN:
                        if (!bluetoothScanActivity.mScanning) {
                            bluetoothScanActivity.mLeDevices.clear();
                            bluetoothScanActivity.mScanLockAdapter.notifyDataSetChanged();
                            removeMessages(START_SCAN);
                            sendEmptyMessageDelayed(STOP_SCAN, SCAN_PERIOD);
                            bluetoothScanActivity.mScanning = true;
                            if (bluetoothScanActivity.mBluetoothAdapter != null) {
                                bluetoothScanActivity.mBluetoothAdapter.startLeScan(bluetoothScanActivity.mLeScanCallback);
                            }
                        } else {
                            LogUtil.d(TAG, "BLE scan already started");
                        }
                        break;
                    case STOP_SCAN:
                        if (bluetoothScanActivity.mScanning) {
                            removeMessages(START_SCAN);
                            removeMessages(STOP_SCAN);
                            bluetoothScanActivity.mScanning = false;
                            if (bluetoothScanActivity.mBluetoothAdapter != null) {
                                bluetoothScanActivity.mBluetoothAdapter.stopLeScan(bluetoothScanActivity.mLeScanCallback);
                            }
                        } else {
                            LogUtil.d(TAG, "BLE scan already stopped");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
