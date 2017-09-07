package com.qhiehome.ihome.lock.ble.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.util.APPUtils;
import com.qhiehome.ihome.util.LogUtil;

public class RequestManager extends Observable {
	private final String TAG = "RequestManager";
	private static RequestManager INSTANCE = null;
	private ConcurrentHashMap<UUID, NotificationCallback> mNotificationMap = new ConcurrentHashMap<UUID, NotificationCallback>();
	private ConcurrentLinkedQueue<Request> mRequestQueue = new ConcurrentLinkedQueue<Request>();
	private List<BluetoothGattService> mServiceList = Collections
	        .synchronizedList(new ArrayList<BluetoothGattService>());

	private Context mContext;
	
	public RequestManager(Context mContex) {
		LogUtil.d(TAG, "RequestManager create now....");
		this.mContext = mContex;
	}
	
	public static RequestManager getInstance() {
		if(INSTANCE == null)
			INSTANCE = new RequestManager(IhomeApplication.getInstance());
		return INSTANCE;
	}

	public static RequestManager getInstance(Context mContex) {
		if(INSTANCE == null)
			INSTANCE = new RequestManager(mContex);
		return INSTANCE;
	}

	public boolean addRequest(BluetoothGattCharacteristic characteristic,
	        RequestCallback requestCallback, Request.REQUEST_TYPE type, boolean isInit) {
		synchronized (mRequestQueue) {

			if ((characteristic == null) || (type == null)) {
				LogUtil.e(TAG, "invalid request, characteristic or type is null");
				return false;
			}
			if(mContext == null){
				LogUtil.e(TAG, "...addRequest : context is null....");
			}
			
    		mRequestQueue.offer(new Request(characteristic, type,
					        requestCallback,isInit));
    		if(type == Request.REQUEST_TYPE.WRITE){
				LogUtil.d(TAG, "...addRequest WRITE: "  + APPUtils.byteArrayToString(characteristic.getValue()));
    		}else{
				LogUtil.d(TAG, "...addRequest ");
    		}
			setChanged();
			notifyObservers();
			return true;
		}
	}
	
	public boolean addRequest(BluetoothGattCharacteristic characteristic,
	        RequestCallback requestCallback, Request.REQUEST_TYPE type) {
		synchronized (mRequestQueue) {
			return addRequest(characteristic,requestCallback,type,false);
		}
	}

	public void clearRequestQueue() {
		synchronized (mRequestQueue) {

			mRequestQueue.clear();
		}
	}

	public Request fetchRequest() {
		LogUtil.d(TAG, "in fetchRequest...");
		synchronized (mRequestQueue) {

			LogUtil.d(TAG,
			        "fetch a Request, request number now: "
			                + mRequestQueue.size());
			return mRequestQueue.poll();
		}
	}

	public BluetoothGattService getGattService(UUID serviceUUID) {
		LogUtil.d(TAG, "enter getGattService...");
		Iterator<BluetoothGattService> it = mServiceList.iterator();
		while (it.hasNext()) {
			BluetoothGattService service = it.next();
			if (service != null && service.getUuid().equals(serviceUUID)) {
				LogUtil.d(TAG, "service found...");
				return service;
			}
		}
		LogUtil.d(TAG, "no service found with uuid: " + serviceUUID);
		return null;
	}

	public BluetoothGattCharacteristic getGattChara(UUID serviceUUID,
	        UUID charaUUID) {
		LogUtil.d(TAG, "enter getGattChara...");
		Iterator<BluetoothGattService> it = mServiceList.iterator();
		while (it.hasNext()) {
			BluetoothGattService service = it.next();
			if (service != null && service.getUuid().equals(serviceUUID)) {
				LogUtil.d(TAG, "service found...");
				return service.getCharacteristic(charaUUID);
			}
		}
		LogUtil.d(TAG, "no service found with uuid: " + serviceUUID);
		return null;
	}

	//
	// public boolean isInQueue(BluetoothGattCharacteristic characteristic,
	// Request.REQUEST_TYPE type) {
	// return false;
	// }

	public void newNotification(BluetoothGattCharacteristic characteristic) {
		NotificationCallback c = mNotificationMap.get(characteristic.getUuid());
		if (c != null) {
			c.onNotification(characteristic);
		}
	}

	public boolean registerNotification(
	        BluetoothGattCharacteristic characteristic,
	        NotificationCallback callback) {
		return registerNotification(characteristic, callback, null);
	}

	public boolean registerNotification(
	        BluetoothGattCharacteristic characteristic,
	        NotificationCallback nCallback, RequestCallback rCallback) {
		synchronized (mRequestQueue) {

			boolean bool;
			if ((characteristic == null) || (nCallback == null)) {
				return false;
			}
			bool = addRequest(characteristic, rCallback,
			        Request.REQUEST_TYPE.REG_NOTIFY);
			mNotificationMap.put(characteristic.getUuid(), nCallback);
			return true;
		}
	}

	public void removeRequests(RequestCallback callback) {
		synchronized (mRequestQueue) {

			if (callback == null) {
				return;
			}
			Request localRequest;
			Iterator<Request> localIterator = this.mRequestQueue.iterator();
			while (localIterator.hasNext()) {
				localRequest = localIterator.next();
				if (localRequest.equals(localRequest.getRequestCallback())
				        && localRequest.getRequestCallback() != null) {
					mRequestQueue.remove(localRequest);
					continue;
				}
				if (localRequest.getCharacteristic() == null) {
					mRequestQueue.remove(localRequest);
					continue;
				}
			}
		}
	}

	public boolean requestsAvailable() {
		return !mRequestQueue.isEmpty();
	}

	public void unregisterAllNotifications(BluetoothGatt gatt) {
		BluetoothGattCharacteristic characteristic;
		Iterator<BluetoothGattService> localIterator1 = mServiceList.iterator();
		if(gatt == null) return;
		while (localIterator1.hasNext()) {
			Iterator<?> localIterator2 = localIterator1.next()
			        .getCharacteristics().iterator();
			while (localIterator2.hasNext()) {
				characteristic = (BluetoothGattCharacteristic) localIterator2
				        .next();
				if ((0x10 & characteristic.getProperties()) > 0
				        || (0x20 & characteristic.getProperties()) > 0) {
					gatt.setCharacteristicNotification(characteristic, false);
				}
			}
		}
		mNotificationMap.clear();
	}

	public void unregisterAllNotifications(
	        NotificationCallback paramNotificationCallback) {
		Iterator<Entry<UUID, NotificationCallback>> localIterator1 = mNotificationMap.entrySet().iterator();
		while (localIterator1.hasNext()) {
			@SuppressWarnings("rawtypes")
			Entry localEntry = (Entry) localIterator1.next();
			if (localEntry.getValue() == paramNotificationCallback) {
				BluetoothGattCharacteristic localBluetoothGattCharacteristic = null;
				Iterator<BluetoothGattService> it2 = mServiceList.iterator();
				do {
					if (!it2.hasNext()) {
						break;
					}
					localBluetoothGattCharacteristic = it2.next()
					        .getCharacteristic((UUID) localEntry.getKey());
				} while (localBluetoothGattCharacteristic == null);
				if (localBluetoothGattCharacteristic != null) {
					addRequest(localBluetoothGattCharacteristic, null,
					        Request.REQUEST_TYPE.UNREG_NOTIFY);
				}
				mNotificationMap.remove(localEntry.getKey());
			}
		}
		return;
	}

	public void unregisterNotification(
	        BluetoothGattCharacteristic characteristic, RequestCallback callback) {
		if (characteristic == null)
			return;
		if (mNotificationMap.remove(characteristic.getUuid()) != null) {
			addRequest(characteristic, callback, Request.REQUEST_TYPE.UNREG_NOTIFY);
		}
	}

	public void updateServiceList(List<BluetoothGattService> list) {
		mServiceList.clear();
		mServiceList.addAll(list);
	}
}
