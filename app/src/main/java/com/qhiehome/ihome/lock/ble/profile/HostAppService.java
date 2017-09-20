package com.qhiehome.ihome.lock.ble.profile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.qhiehome.ihome.lock.LockController;
import com.qhiehome.ihome.lock.bluetooth.BluetoothManagerService;
import com.qhiehome.ihome.lock.ble.CommunicationManager;
import com.qhiehome.ihome.lock.ble.request.NotificationRunnable;
import com.qhiehome.ihome.lock.ble.request.Request;
import com.qhiehome.ihome.lock.ble.request.RequestManager;
import com.qhiehome.ihome.util.APPUtils;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.util.ToastUtil;

public class HostAppService extends Service implements Observer {

	private final String TAG = "HostAppService";

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothGatt mBluetoothGatt;
	private Request mCurrentRequest;
	private BluetoothGattCharacteristic mCurrentCharacteristic;
	private GattConntionStateChangeRunnable mGattConntionStateChangeRunnable;
	private ServiceDiscoveryRunnable mServiceDiscoveryRunnable;
	private GattReadOrWriteRunnable mGattReadOrWriteRunnable;
	private GattDescriptorReadOrWriteRunnable mGattDescriptorReadOrWriteRunnable;

	private String mDeviceAddress;
	private String mDeviceName;

	private Context mContext;

	private HandlerThread mGattHandlerThread;
	private Handler mGattHandler;

	private RequestManager mRequestManager;
	private TimeoutRunner mDiscoveryTimeout;
	private TimeoutRunner mRequestTimeout;
	private TimeoutRunner mConnectTimeout;
	private TimeoutRunner mFristTimeConnTimeOut;

	private final String GATT_THREAD_NAME = "GATT_THREAD";
	public static final int LOWEST_ALLOWED_DFU_BATTERY_LEVEL = 10;
	private final long SCAN_TIMEOUT = 20000L;

	private final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	private final int HEART_BEART = 10009;

	// private final String CLIENT_CHARACTERISTIC_CONFIG =
	// "00000000-0000-1000-8000-00805f9b34fb";

	public IBinder onBind(Intent paramIntent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mContext = this;
		LogUtil.d(TAG, "Service oncreate()");

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mRequestManager = RequestManager.getInstance(this);
		mRequestManager.addObserver(this);

		ThreadManager.getInstance(this);

		mGattHandlerThread = new HandlerThread(GATT_THREAD_NAME);
		mGattHandlerThread.start();

		mGattHandler = new Handler(mGattHandlerThread.getLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HEART_BEART:
					CommunicationManager.getInstance()
							.sendBLEEvent(mContext, new Intent(
									BLECommandIntent.ACTION_HEART_BEAT));
					if (CommunicationManager.getInstance().isBLEConnectted()) {
						sendEmptyMessageDelayed(HEART_BEART, 2000);
					}
					break;
				}
			}
		};

		mFristTimeConnTimeOut = new TimeoutRunner(
				TimeoutRunner.DEVICE_FRIST_TIME_CONNECT);

		LogUtil.d(TAG, "Start mGattHandlerThread");
	}

	public void onDestroy() {
		super.onDestroy();
		LogUtil.d(TAG, "onDestory");
		mRequestManager.deleteObserver(this);
		disconnect(true);
		Looper localLooper = mGattHandlerThread.getLooper();
		if (localLooper != null) {
			localLooper.quitSafely();
			LogUtil.d(TAG, "quitSafely: mGattHandlerThread.getLooper()");
		}
		LogUtil.d(TAG, "ThreadManager tearDown...");
		ThreadManager.getInstance(this).tearDown();
	}

	public int onStartCommand(final Intent intent, int flags, int startId) {

		if (intent != null) {
			mGattHandler.post(new Runnable() {
				public void run() {
					String action = intent.getAction();
					Bundle bundle = intent.getExtras();
					LogUtil.d(TAG, "onStartCommand. Due to intent action: " + action);
					if (TextUtils.isEmpty(action)) {
						LogUtil.e(TAG, "onStartCommand. No Action!!!");
						return;
					}
					boolean isBleConnecttedOrConnectting = CommunicationManager
							.getInstance().isBleConnecttedOrConnectting();
					if (CommunicationManager.ACTION_CONNECT_TO_DEVICE
							.equals(action)) {

						String address = bundle
								.getString(CommunicationManager.EXTRA_ADDRESS);
						String name = bundle
								.getString(CommunicationManager.EXTRA_NAME);

						if (!isDeviceValid(address)) {
							LogUtil.e(TAG, "Action is connect.Invalid address: " + address);
							return;
						}
						
						mGattHandler.removeCallbacks(mFristTimeConnTimeOut);

						if (isBleConnecttedOrConnectting) {
							LogUtil.d(TAG, "Action is connect. But we already in connecting/connected state, disconnect with it frist!");
							disconnect(true);
						}

						mDeviceAddress = address;
						mDeviceName = name;
						connectToBLE(true, mFristDevice);

					} else if (CommunicationManager.ACTION_UI_LUANCH
							.equals(action)) {
						LogUtil.d(TAG, "Action is CommunicationManager.ACTION_UI_LUANCH--- mAddress:" + mDeviceAddress + ", isConnecting: " + isBleConnecttedOrConnectting);
					} else if (CommunicationManager.ACTION_SERVICE_CONFIG_SUCCESS
							.equals(action)) {
						CommunicationManager.getInstance().DEVICE_REAL_NAME = mBluetoothDevice
								.getName();

						CommunicationManager
								.getInstance()
								.sendBTStateChanged(mContext, CommunicationManager.BLE_CONNECTTED);
//						showConnectedNotification();
						notifyClientServices(true);

						mGattHandler.sendEmptyMessage(HEART_BEART);

						LogUtil.i(TAG, "CommunicationManager.ACTION_SERVICE_CONFIG_SUCCESS receive!!!!!");
					} else if(CommunicationManager.ACTION_RX_STATUS_SUCCESS.equals(action)){
						removeDeviceFristConnTimeoutRunner();
					}else if (CommunicationManager.ACTION_SERVICE_CONFIG_FAIL
							.equals(action)) {
						if (isBleConnecttedOrConnectting) {
							reconnect();
						}
					} else if (CommunicationManager.ACTION_DISCONNECT_TO_DEVICE
							.equals(action)) {
						LogUtil.i(TAG, "CommunicationManager.ACTION_DISCONNECT_TO_DEVICE received....");
						disconnectByHost();
					}
				}
			});
		} else {
			LogUtil.d(TAG, "Service onStartCommand: " + intent);
		}
		return START_NOT_STICKY;
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			LogUtil.i(TAG, "onConnectionStateChange..., status: " + status + "newState: " + newState);
			removeAllTimeOutRunner();
			if (mGattConntionStateChangeRunnable != null) {
				mGattHandler.removeCallbacks(mGattConntionStateChangeRunnable);
			}
			mGattConntionStateChangeRunnable = new GattConntionStateChangeRunnable(
					status, newState, gatt);
			mGattHandler.post(mGattConntionStateChangeRunnable);
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			LogUtil.i(TAG, "onServicesDiscovered...");
			removeAllTimeOutRunner();
			if (mServiceDiscoveryRunnable != null) {
				mGattHandler.removeCallbacks(mServiceDiscoveryRunnable);
			}
			mServiceDiscoveryRunnable = new ServiceDiscoveryRunnable(status,
					gatt);
			mGattHandler.post(mServiceDiscoveryRunnable);
		}

		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			LogUtil.i(TAG, "onCharacteristicChanged...");
			if ((characteristic != null) && (characteristic.getValue() != null)) {
				NotificationRunnable r = new NotificationRunnable(
						characteristic);
				mGattHandler.post(r);
			} else {
				LogUtil.d(TAG, "Got a notification about a null characteristic or the characteristic did not contain any data. Ignoring the notification");
			}
		}

		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogUtil.i(TAG, "onCharacteristicRead...: " + status);
			if (mGattReadOrWriteRunnable != null) {
				mGattHandler.removeCallbacks(mGattReadOrWriteRunnable);
			}
			mGattReadOrWriteRunnable = new GattReadOrWriteRunnable(
					characteristic, status);
			mGattHandler.post(mGattReadOrWriteRunnable);
		}

		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			mGattHandler.post(new GattReadOrWriteRunnable(characteristic,
					status));
		}

		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor gattDescriptor, int status) {
			LogUtil.i(TAG, "onDescriptorRead...: " + status);
			if (mGattDescriptorReadOrWriteRunnable != null) {
				mGattHandler
						.removeCallbacks(mGattDescriptorReadOrWriteRunnable);
			}
			mGattDescriptorReadOrWriteRunnable = new GattDescriptorReadOrWriteRunnable(
					gattDescriptor, status);
			mGattHandler.post(mGattDescriptorReadOrWriteRunnable);
		}

		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor gattDescriptor, int status) {
			LogUtil.i(TAG, "onDescriptorWrite...: " + status);
			mGattHandler.post(new GattDescriptorReadOrWriteRunnable(
					gattDescriptor, status));
		}

	};

	private class GattConntionStateChangeRunnable implements Runnable {
		private int state;
		private int profileState;

		GattConntionStateChangeRunnable(int status, int newState, BluetoothGatt gatt) {
			state = status;
			profileState = newState;
		}

		public void run() {
			removeAllTimeOutRunner();
			failCurrentRequest();
			mRequestManager.clearRequestQueue();

			if (profileState == BluetoothGatt.STATE_CONNECTED) {
				if (state != BluetoothGatt.GATT_SUCCESS) {
					LogUtil.w(TAG, "Connect fail!!!! fail code: " + state);
					LogUtil.e(TAG, "Try to reconnect due to Connect fail, reconnect time: " + CommunicationManager.RECONNECT_TIME);
					reconnect();
					return;
				}

				updateDeviceMAC(mBluetoothGatt);
				LogUtil.d(TAG, "Connect state now, Starting discovery....");
				startDiscovery();
			} else if (profileState == BluetoothGatt.STATE_DISCONNECTED) {
				mGattHandler.removeMessages(HEART_BEART);

				mRequestManager.clearRequestQueue();

				if (state != BluetoothGatt.GATT_SUCCESS) {
					LogUtil.e(TAG, "Disconnect fail!!!! fail code: " + state);
				} else {
					LogUtil.d(TAG, "Disconnected !!!! mBluetoothGatt: " + mBluetoothGatt);
				}

				if (mBluetoothGatt != null) {
					if (!CommunicationManager.getInstance().HOST_DISCONNECT) {
						try {
							mBluetoothGatt.close();
							mBluetoothGatt = null; // fail and cleam
						} catch (Exception e) {
							e.printStackTrace();
						}
						LogUtil.e(TAG, "Try to reconnect due to disconnect/linkloss, reconnect time: " + CommunicationManager.RECONNECT_TIME);
						justReconnect();
					} else {
						try {
							CommunicationManager
									.getInstance()
									.sendBTStateChanged(mContext, CommunicationManager.BLE_DISCONNECTTED);
//							showDisconnectedNotification();
							sendTimeOutBroadcast();
							LogUtil.e(TAG, "need not to reconnect..");
							mBluetoothGatt.close();
							mGattHandler.removeMessages(HEART_BEART);
							mBluetoothGatt = null; // fail and cleam

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	private void reconnect() {
		disconnect(false);
		if (CommunicationManager.RECONNECT_TIME < 40) {
			CommunicationManager
					.getInstance()
					.sendBTStateChanged(mContext, CommunicationManager.BLE_CONNECTING);
			connectToBLE(true);
			CommunicationManager.RECONNECT_TIME++;
		} else {
			LogUtil.e(TAG, "reconnect to many time. stop reconnect now.. " + CommunicationManager.RECONNECT_TIME);
			disconnectByHost();
		}
	}

	private void justReconnect() {
		if (CommunicationManager.RECONNECT_TIME < 40) {
			CommunicationManager
					.getInstance()
					.sendBTStateChanged(mContext, CommunicationManager.BLE_CONNECTING);
			connectToBLE(true);
			CommunicationManager.RECONNECT_TIME++;
		} else {
			LogUtil.e(TAG, "reconnect to many time. stop reconnect now.. " + CommunicationManager.RECONNECT_TIME);
			CommunicationManager
					.getInstance()
					.sendBTStateChanged(mContext, CommunicationManager.BLE_DISCONNECTTED);
			mGattHandler.removeMessages(HEART_BEART);
		}
	}

	private class ServiceDiscoveryRunnable implements Runnable {
		private int mStatus;

		ServiceDiscoveryRunnable(int status, BluetoothGatt paramBluetoothGatt) {
			mStatus = status;
		}

		public void run() {
			if (mStatus != BluetoothGatt.GATT_SUCCESS) {
				LogUtil.e(TAG, "Try to recconect duto Service discovery fail! status: " + mStatus);
				reconnect();
				return;
			}
			LogUtil.d(TAG, "Service sucessfully discovered! status: " + mStatus);

			 if (mBluetoothGatt != null){
				 clearServiceCache(mBluetoothGatt);
				 LogUtil.d(TAG, "Clearing service cache");
			 }
//			 else {
			// LogUtils.e(TAG,
			// "Service sucessfully discovered! But gatt is null!");
			// return;
			// }

			if (mBluetoothGatt != null) {
				mRequestManager.updateServiceList(mBluetoothGatt.getServices());
				LogUtil.d(TAG, "StartConfig!");
				CommunicationManager
						.getInstance()
						.sendBLEEvent(mContext, BLECommandIntent.ACTION_START_CONFIG);
			}
		}
	}

	private class GattReadOrWriteRunnable implements Runnable {
		private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
		private int mConnectiongStatus;

		GattReadOrWriteRunnable(BluetoothGattCharacteristic characteristic, int status) {
			mBluetoothGattCharacteristic = characteristic;
			mBluetoothGattCharacteristic.setValue(mCurrentRequest.getValue()); // debug
			mConnectiongStatus = status;
		}

		public void run() {
			if (mConnectiongStatus != BluetoothGatt.GATT_SUCCESS && mCurrentRequest != null && mCurrentRequest
					.getType() == Request.REQUEST_TYPE.WRITE) {
				// restart();
				failCurrentRequest();
				LogUtil.e(TAG, "GattReadOrWriteRunnable error code: " + mConnectiongStatus);
				LogUtil.e(TAG, "UUID: " + mBluetoothGattCharacteristic
						.getUuid());
				byte[] data = mBluetoothGattCharacteristic.getValue();
				String dataLog = "";
				for (int i = 0; i < data.length; i++) {
					dataLog += String.format("%02x ", (int) (data[i] & 0xFF));
				}
				LogUtil.e(TAG, "Data: " + dataLog);
				ToastUtil.showToast(mContext, "Receive Error");
				return;
			}

			LogUtil.i(TAG, "onCharacteristicWrite...: " + APPUtils
					.byteArrayToString(mBluetoothGattCharacteristic.getValue()));
			handleRequestResult(mBluetoothGattCharacteristic, true);

			// onWrite not means M1 receive our infomation, we wait the
			// wriite_sucess information
		}
	}

	private class GattDescriptorReadOrWriteRunnable implements Runnable {
		private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
		private int mConnectiongStatus;

		GattDescriptorReadOrWriteRunnable(BluetoothGattDescriptor descriptor, int status) {
			mBluetoothGattCharacteristic = descriptor.getCharacteristic();
			mConnectiongStatus = status;
		}

		public void run() {
			if (mConnectiongStatus != BluetoothGatt.GATT_SUCCESS) {
				if (mCurrentRequest != null && mCurrentRequest.getType() == Request.REQUEST_TYPE.WRITE) {
					return;
				}
				failCurrentRequest();
				LogUtil.e(TAG, "GattDescriptorReadOrWriteRunnable error code: " + mConnectiongStatus);
				handleRequestResult(mBluetoothGattCharacteristic, false);
				return;
			}
			handleRequestResult(mBluetoothGattCharacteristic, true);
		}
	}

	private boolean isDeviceValid(String mDeviceAddress) {
		if (!isAddressValid(mDeviceAddress)) {
			LogUtil.e(TAG, "Device address is invalid");
			return false;
		}
		LogUtil.d(TAG, "Device MAC: " + mDeviceAddress + " is valid");

		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
		if (mBluetoothDevice == null) {
			return false;
		}
		return true;
	}

	private void connectToBLE(boolean scanFrist, boolean mFristDevice) {
		if (mFristDevice) {
			LogUtil.e(TAG, "this is a new device, first time connect, start time out runner...");
			removeRequestTimeoutRunner();
			mFristTimeConnTimeOut = new TimeoutRunner(
					TimeoutRunner.DEVICE_FRIST_TIME_CONNECT);
			mGattHandler
					.postDelayed(mFristTimeConnTimeOut, TimeoutRunner.DEVICE_FRIST_TIME_CONNECT_DELAY);
			sendBroadcast(new Intent(CommunicationManager.ACTION_DEVICE_FRIST_TIME));
		}
		connectToBLE(scanFrist);
	}

	private void connectToBLE(boolean scanFrist) {
		removeConnectTimeoutRunner();
		mGattHandler.removeMessages(HEART_BEART);
		CommunicationManager.getInstance().HOST_DISCONNECT = false;
//		showConnectingNotification();
		updateDeviceMAC();
		CommunicationManager
				.getInstance()
				.sendBTStateChanged(mContext, CommunicationManager.BLE_CONNECTING);
		LogUtil.d(TAG, "Enter BLE connect...");
		failCurrentRequest();
		// if (scanFrist) {
		// startScan();
		// } else {
		afterScanAction();
		// }
	}

	private void startScan() {
		removeAllTimeOutRunner();
		LogUtil.d(TAG, "Starting scan");
		mBluetoothAdapter.startLeScan(mScanCallback);
		mGattHandler.postDelayed(mScanTimeoutRunnable, SCAN_TIMEOUT);
	}

	private boolean mFristDevice = true;
	private final Runnable afterScanActionRunnbale = new Runnable() {
		public void run() {
			LogUtil.d(TAG, "Start connecting now... device.connect in afterScanActionRunnbale");
			try {
				if (mBluetoothGatt != null) {
					LogUtil.d(TAG, "mBluetoothGatt is not null, Clear it frist");
					mBluetoothGatt.disconnect();
					mBluetoothGatt.close();
					mBluetoothGatt = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// kiwi
			mBluetoothGatt = mBluetoothDevice
					.connectGatt(mContext, false, mGattCallback);

		}
	};

	private void afterScanAction() {
		mGattHandler.removeCallbacks(afterScanActionRunnbale);
		mGattHandler.post(afterScanActionRunnbale);

		mConnectTimeout = new TimeoutRunner(TimeoutRunner.STATE_CONNECTTING);
		mGattHandler
				.postDelayed(mConnectTimeout, TimeoutRunner.CONNECTTING_DELAY);
	}

	private final BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
		public void onLeScan(BluetoothDevice device, int paramAnonymousInt, byte[] paramAnonymousArrayOfByte) {
			LogUtil.d(TAG, "Scan found %s" + device.getAddress());
			if (device.equals(mBluetoothDevice)) {
				LogUtil.d(TAG, "Device found in LeScan, connecnt to it now...");
				mGattHandler.removeCallbacks(mScanTimeoutRunnable);
				mBluetoothAdapter.stopLeScan(mScanCallback);
				afterScanAction();
			}
		}
	};

	private final Runnable mScanTimeoutRunnable = new Runnable() {
		public void run() {
			LogUtil.d(TAG, "LE scan timeout");
			mBluetoothAdapter.stopLeScan(mScanCallback);
			LogUtil.d(TAG, "Device not found in LeScan, but we try to connect with it all the same");
			afterScanAction();
		}
	};

	private void startDiscovery() {
		removeAllTimeOutRunner();
		if (mBluetoothGatt != null) {
			mBluetoothGatt.discoverServices();
			mDiscoveryTimeout = new TimeoutRunner(
					TimeoutRunner.STATE_DISCOVERING);
			mGattHandler
					.postDelayed(mDiscoveryTimeout, TimeoutRunner.DISCOVERY_DELAY);
			LogUtil.d(TAG, "Wait for service discovered...");
		} else {
			LogUtil.e(TAG, "mBluetoothGatt is null...");
		}
	}

	// not use in this app
	private void clearServiceCache(BluetoothGatt paramBluetoothGatt) {
		try {
			Method localMethod = BluetoothGatt.class
					.getDeclaredMethod("refresh", new Class[0]);
			localMethod.setAccessible(true);
			localMethod.invoke(paramBluetoothGatt, (Object[]) null);
			return;
		} catch (NoSuchMethodException e) {
			LogUtil.e(TAG, e.getMessage());
		} catch (IllegalArgumentException e) {
			LogUtil.e(TAG, e.getMessage());
		} catch (IllegalAccessException e) {
			LogUtil.e(TAG, e.getMessage());
		} catch (InvocationTargetException e) {
			LogUtil.e(TAG, e.getMessage());
		}
	}

	private void disconnectByHost() {
		LogUtil.i(TAG, "enter disconnectByHost...");
		CommunicationManager.getInstance().HOST_DISCONNECT = true;
		removeDeviceFristConnTimeoutRunner();
		removeConnectTimeoutRunner();
		disconnect(true);
	}

	private void disconnect(boolean isClean) {
		LogUtil.i(TAG, "Enter disconnect/clsoe... need reconnect: " + !isClean);
		removeAllTimeOutRunner();
		mGattHandler.removeMessages(HEART_BEART);
		failCurrentRequest();
		mRequestManager.clearRequestQueue();
		try {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.disconnect();
				mBluetoothGatt.close();
				mBluetoothGatt = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isClean) {
			if (CommunicationManager.getInstance()
					.isBleConnecttedOrConnectting()) {
				CommunicationManager
						.getInstance()
						.sendBTStateChanged(mContext, CommunicationManager.BLE_DISCONNECTTED);
			}
//			showDisconnectedNotification();
			sendTimeOutBroadcast();
		}
	}

	public void update(Observable paramObservable, Object paramObject) {
		mGattHandler.post(new Runnable() {
			public void run() {
				getNewRequest();
			}
		});
	}

	private void getNewRequest() {
		LogUtil.d(TAG, "enter getNewRequest...");
		if (!CommunicationManager.getInstance().isBLEConnectted()) {
			LogUtil.e(TAG, "getNewRequest,but no connection!");
			return;
		}
		if (mBluetoothGatt == null) {
			LogUtil.e(TAG, "mBluetoothGatt is null,restart");
		} else if (!mRequestManager.requestsAvailable()) {
			LogUtil.d(TAG, "getNewRequest: No requests!");
		} else if (mCurrentRequest == null) {
			LogUtil.d(TAG, "getNewRequest...");
			mCurrentRequest = mRequestManager.fetchRequest();
			if (mCurrentRequest != null) {
				mCurrentCharacteristic = mCurrentRequest.getCharacteristic();
				if (mCurrentCharacteristic == null) {
					mCurrentRequest = null;
				} else {
					Request.REQUEST_TYPE requsetType = mCurrentRequest
							.getType();
					boolean isSuccess = false;
					switch (requsetType) {
					case READ:
						isSuccess = mBluetoothGatt
								.readCharacteristic(mCurrentCharacteristic);
						LogUtil.d(TAG, "Read request sent for " + mCurrentCharacteristic
								.getUuid().toString());
						this.handleRequestResult(mCurrentCharacteristic, true);
						break;
					case WRITE:
						byte[] data = mCurrentRequest.getValue();

						if (data == null) {
							LogUtil.e(TAG, "Data is null!! Fail this write request!!!!" + mCurrentCharacteristic
									.getUuid());
							failCurrentRequest();
							handleRequestResult(mCurrentCharacteristic, false);
							return;
						}

						String dataLog = "";
						for (int i = 0; i < data.length; i++) {
							dataLog += String
									.format("%02x ", (int) (data[i] & 0xFF));
						}
						LogUtil.i(TAG, "Write Data: " + dataLog);

						mCurrentCharacteristic.setValue(data);
						isSuccess = mBluetoothGatt
								.writeCharacteristic(mCurrentCharacteristic);
						LogUtil.d(TAG, "Write request sent for " + mCurrentCharacteristic
								.getUuid().toString());
						break;
					case REG_NOTIFY:
						BluetoothGattDescriptor descriptor = mCurrentCharacteristic
								.getDescriptor(UUID
										.fromString(CLIENT_CHARACTERISTIC_CONFIG));
						// try{
						// Thread.sleep(3000);
						// }catch(Exception e){
						// e.printStackTrace();
						// }
						if (descriptor != null) {
							descriptor
									.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							isSuccess = mBluetoothGatt
									.writeDescriptor(descriptor);
							LogUtil.d(TAG, "Request sent to enable notifications for " + mCurrentCharacteristic
									.getUuid().toString());
						} else {
							LogUtil.e(TAG, "Failed to enable notifications for " + mCurrentCharacteristic
									.getUuid().toString() + " descriptor was null");
						}
						
						mBluetoothGatt.setCharacteristicNotification(mCurrentCharacteristic, true);
						
						break;
					case UNREG_NOTIFY:
						BluetoothGattDescriptor descriptor2 = mCurrentCharacteristic
								.getDescriptor(UUID
										.fromString(CLIENT_CHARACTERISTIC_CONFIG));
						if (descriptor2 != null) {
							descriptor2
									.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
							isSuccess = mBluetoothGatt
									.writeDescriptor(descriptor2);
							LogUtil.d(TAG, "Request sent to disable notifications for " + mCurrentCharacteristic
									.getUuid().toString());
						} else {
							LogUtil.e(TAG, "Failed to disable notifications for " + mCurrentCharacteristic
									.getUuid().toString() + " descriptor was null");
						}
						
						mBluetoothGatt.setCharacteristicNotification(mCurrentCharacteristic, false);

						break;
					}// switch
					removeRequestTimeoutRunner();
					mRequestTimeout = new TimeoutRunner(
							TimeoutRunner.STATE_REQUESTING);
					mGattHandler
							.postDelayed(mRequestTimeout, TimeoutRunner.REQUEST_DELAY);
				}// if else characteristic == null
			}// if else mCurrentRequest != null
		}// if else mCurrentRequest == null
		else {
			LogUtil.d(TAG, "mCurrentRequest is not null now...");
		}
	}

	private void failCurrentRequest() {
		if ((mCurrentRequest != null) && (mCurrentRequest.getRequestCallback() != null)) {
			// mGattHandler.removeCallbacks(mRequestTimeout);
			mCurrentRequest
					.getRequestCallback()
					.onRequestComplete(mCurrentRequest.getCharacteristic(), false, mCurrentRequest
							.getType());
		}
		mCurrentRequest = null;
	}

	private class TimeoutRunner implements Runnable {
		public static final int REQUEST_DELAY = 7000;
		public static final int DISCOVERY_DELAY = 1200000;
		public static final int CONNECTTING_DELAY = 5000;
		public static final int DEVICE_FRIST_TIME_CONNECT_DELAY = 11000;

		public static final int STATE_CONNECTTING = 3;
		public static final int STATE_DISCOVERING = 1;
		public static final int STATE_REQUESTING = 2;
		public static final int DEVICE_FRIST_TIME_CONNECT = 4;

		private int mState;

		public TimeoutRunner(int paramInt) {
			mState = paramInt;
		}

		public void run() {
			switch (mState) {
			case STATE_CONNECTTING:
				LogUtil.e(TAG, "Try to reconnect due to connection time out, RECONNECT_TIME: " + CommunicationManager.RECONNECT_TIME);
				reconnect();
				break;
			case STATE_REQUESTING:
				handleRequestFail();
				break;
			case STATE_DISCOVERING:
				LogUtil.d(TAG, "Discovery failed!");
				handleDiscoveryFail();
				break;
			case DEVICE_FRIST_TIME_CONNECT:
				LogUtil.e(TAG, "new device, first time connect, time out ...");
				sendBroadcast(new Intent(CommunicationManager.ACTION_DEVICE_FRIST_TIMEOUT));
				disconnectByHost();
				break;
			}
		}
	}

	private void handleDiscoveryFail() {
		reconnect();
	}

	private void handleRequestFail() {
		LogUtil.e(TAG, String.format("Request time out!!!"));
		reconnect();
	}

	private void handleRequestResult(BluetoothGattCharacteristic characteristic, boolean isSuccess) {
		LogUtil.i(TAG, "handleRequestResult, complete. isSuccess: " + isSuccess);

		removeRequestTimeoutRunner();
		if ((mCurrentRequest != null) && (mCurrentRequest.getRequestCallback() != null)) {
			if (characteristic == null) {
				characteristic = mCurrentRequest.getCharacteristic();
				isSuccess = false;
			}
			mCurrentRequest
					.getRequestCallback()
					.onRequestComplete(characteristic, isSuccess, mCurrentRequest
							.getType());
		}
		mCurrentRequest = null;
		getNewRequest();
	}


	private boolean isAddressValid(String paramString) {
		if ((!TextUtils.isEmpty(paramString)) && (BluetoothAdapter
				.checkBluetoothAddress(paramString))) {
			return true;
		}
		return false;
	}

	private void notifyClientServices(boolean status) {
		LogUtil.d(TAG, "Notify BLECommandService, connect status: " + status);
		Intent localIntent = new Intent(this, BLECommandService.class);
		localIntent
				.setAction(BLECommandIntent.ACTION_HANDLE_CONNECTION_STATE_CHANGE);
		localIntent
				.putExtra(BLECommandIntent.EXTRA_CONNECTION_STATE_NEW, status);

		startService(localIntent);
	}

	private void removeAllTimeOutRunner() {
		removeScanTimeoutRunner();
		removeDiscoveryTimeoutRunner();
		removeRequestTimeoutRunner();
		removeConnectTimeoutRunner();
	}

	private void removeScanTimeoutRunner() {
		if (mDiscoveryTimeout != null) {
			mGattHandler.removeCallbacks(mScanTimeoutRunnable);
		}
	}

	private void removeDiscoveryTimeoutRunner() {
		if (mDiscoveryTimeout != null) {
			mGattHandler.removeCallbacks(mDiscoveryTimeout);
		}
	}

	private void removeDeviceFristConnTimeoutRunner() {
		if (mFristTimeConnTimeOut != null) {
			LogUtil.e(TAG, "removeDeviceFristConnTimeoutRunner... ");
			mGattHandler.removeCallbacks(mFristTimeConnTimeOut);
			mFristTimeConnTimeOut = null;
		}
	}

	private void removeRequestTimeoutRunner() {
		if (mRequestTimeout != null) {
			mGattHandler.removeCallbacks(mRequestTimeout);
		}
	}

	private void removeConnectTimeoutRunner() {
		if (mConnectTimeout != null) {
			mGattHandler.removeCallbacks(mConnectTimeout);
		}
	}

	private void updateDeviceMAC(BluetoothGatt gatt) {
		if ((gatt != null) && (gatt.getDevice() != null)) {
			APPUtils.setWatchAddress(this, gatt.getDevice().getAddress());
			APPUtils.setWatchName(this, mDeviceName);
		}
	}

	private void updateDeviceMAC() {
		APPUtils.setWatchAddress(this, mDeviceAddress);
		APPUtils.setWatchName(this, mDeviceName);
	}

	private void sendTimeOutBroadcast() {
		Intent intent = new Intent(LockController.BROADCAST_CONNECT);
		intent.putExtra("info", "连接超时");
		sendBroadcast(intent);
	}

}
