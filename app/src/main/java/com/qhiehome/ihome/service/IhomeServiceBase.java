package com.qhiehome.ihome.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.qhiehome.ihome.R;
import com.qhiehome.ihome.activity.MainActivity;
import com.qhiehome.ihome.util.LogUtil;

public abstract class IhomeServiceBase extends Service {

    private static final String TAG = "IhomeServiceBase";

    public static final int NOTIFICATION_ID = R.string.app_name;

    private enum NOTIFICATION_STATE {
        NOTIFICATION_NONE,
        NOTIFICATION_APP,
        NOTIFICATION_CONNECTING,
        NOTIFICATION_CONNECTED,
        NOTIFICATION_DISCONNECT,
        NOTIFICATION_FIRMWARE
    }

    private NOTIFICATION_STATE mNotificationState = NOTIFICATION_STATE.NOTIFICATION_NONE;

    private NotificationManager mNotificationMgr;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (getAppNotification() == null) {
            throw new NullPointerException("Notification is null");
        }
        showAppNotification();
        mNotificationState = NOTIFICATION_STATE.NOTIFICATION_APP;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LogUtil.i(TAG, "onTaskRemoved");
        stopForeground(true);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, TAG + " onDestroy()");
        super.onDestroy();
    }

    public Notification getAppNotification() {
        return getNotification(R.string.app_name, R.string.app_name,
                R.mipmap.ic_launcher_car);
    }

    public Notification getConnectedNotification() {
        return getNotification(R.string.device_connected,
                R.string.device_connected, R.mipmap.ic_launcher_car);
    }

    public Notification getConnectingNotification() {
        return getNotification(R.string.device_connecting,
                R.string.device_connecting, R.mipmap.ic_launcher_car);
    }

    public Notification getDisconnectedNotification() {
        return getNotification(R.string.device_disconnected,
                R.string.device_disconnected, R.mipmap.ic_launcher_car);
    }

    public Notification getFirmwareNotification() {
        return getNotification(R.string.app_name, 2131493040,
                R.mipmap.ic_launcher_car);
    }

    private Notification getNotification(int contextId, int trickId,
                                         int smallIconId) {
        Notification.Builder build = new Notification.Builder(this);
        build.setContentTitle(getString(R.string.app_name));
        build.setContentText(getString(contextId));
        build.setTicker(getString(trickId, getString(trickId)));
        build.setSmallIcon(smallIconId);
        build.setShowWhen(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        build.setContentIntent(PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT));
        return build.build();
    }

    public void showConnectedNotification() {
        if (getConnectedNotification() == null) {
            throw new NullPointerException("Connected notification is null");
        }
        if(mNotificationState == NOTIFICATION_STATE.NOTIFICATION_CONNECTED)
            return;
        mNotificationState = NOTIFICATION_STATE.NOTIFICATION_CONNECTED;
        mNotificationMgr.notify(NOTIFICATION_ID, getConnectedNotification());
    }

    public void showAppNotification() {
        if (getAppNotification() == null) {
            throw new NullPointerException("App notification is null");
        }
        if(mNotificationState == NOTIFICATION_STATE.NOTIFICATION_APP)
            return;
        mNotificationState = NOTIFICATION_STATE.NOTIFICATION_APP;
        startForeground(NOTIFICATION_ID, getAppNotification());

    }

    public void showConnectingNotification() {
        if (getConnectingNotification() == null) {
            throw new NullPointerException("Connected notification is null");
        }
        if(mNotificationState == NOTIFICATION_STATE.NOTIFICATION_CONNECTING)
            return;
        mNotificationState = NOTIFICATION_STATE.NOTIFICATION_CONNECTING;
        startForeground(NOTIFICATION_ID, getConnectingNotification());
    }

    public void showDisconnectedNotification() {
        if (getDisconnectedNotification() == null) {
            throw new NullPointerException("Disconnected notification is null");
        }

        if(mNotificationState == NOTIFICATION_STATE.NOTIFICATION_DISCONNECT)
            return;
        mNotificationState = NOTIFICATION_STATE.NOTIFICATION_DISCONNECT;
        startForeground(NOTIFICATION_ID, getDisconnectedNotification());
    }

    public void showFirmwareNotification() {
        if (getFirmwareNotification() == null) {
            throw new NullPointerException("Firmware notification is null");
        }

        if(mNotificationState == NOTIFICATION_STATE.NOTIFICATION_FIRMWARE)
            return;
        mNotificationState = NOTIFICATION_STATE.NOTIFICATION_FIRMWARE;
        startForeground(NOTIFICATION_ID, getFirmwareNotification());
    }
}
