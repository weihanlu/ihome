package com.qhiehome.ihome.lock.gateway;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.lock.LockController;
import com.qhiehome.ihome.util.LogUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttManagerService extends Service implements LockController{

    public static final String ACTION_GATEWAY_CONNECT = "com.qhiehome.ihome.lock.action.GATEWAY_CONNECT";
    public static final String ACTION_GATEWAY_DISCONNECT = "com.qhiehome.ihome.lock.action.GATEWAY_DISCONNECT";
    public static final String ACTION_UP_LOCK = "com.qhiehome.ihome.lock.action.UP_LOCK";
    public static final String ACTION_DOWN_LOCK = "com.qhiehome.ihome.lock.action.DOWN_LOCK";

    public static final String LOCK_MAC = "lock_mac";
    public static final String GATEWAY_ID = "gateway_id";

    private static final String TAG = "MqttManagerService";

    private static final String COMMAND_UP = "[01:01]";

    private static final String COMMAND_DOWN = "[01:02]";

    private static final String COMMAND_BEE = "[0A:05]";

    private static final String HOST = "tcp://www.klmiot.cn:1883";

    private static final String UID = "c55e365d3f164df1";

    private MqttAndroidClient mqttAndroidClient;

    private static final String SUBSCRIBE_PREFIX = "/status/lock/" + UID + "/#";

    private static final String PUBLISH_PREFIX = "/set/lock/" + UID + "/";

    private String mPublishTopic;

    private String mSubscribeTopic;

    private String mGateWayId;

    private String mLockMac;

    private MqttConnectOptions mqttConnectOptions;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GateWayCallback gateWayCallback = new GateWayCallback();
        mqttAndroidClient = new MqttAndroidClient(IhomeApplication.getInstance(),
                HOST, MqttClient.generateClientId());
        mqttAndroidClient.setCallback(gateWayCallback);
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_GATEWAY_CONNECT:
                        mGateWayId = intent.getStringExtra(GATEWAY_ID);
                        mLockMac = intent.getStringExtra(LOCK_MAC);
                        mPublishTopic = PUBLISH_PREFIX + mGateWayId;
                        mSubscribeTopic = SUBSCRIBE_PREFIX;
                        connect();
                        break;
                    case ACTION_GATEWAY_DISCONNECT:
                        disconnect();
                        break;
                    case ACTION_DOWN_LOCK:
                        downLock();
                        break;
                    case ACTION_UP_LOCK:
                        raiseLock();
                        break;
                    default:
                        break;
                }
            }
        }
        return START_STICKY;
    }

    private void beeLock() {
        publishMessage(COMMAND_BEE);
    }

    @Override
    public void raiseLock() {
        publishMessage(COMMAND_UP);
    }

    @Override
    public void downLock() {
        publishMessage(COMMAND_DOWN);
    }

    @Override
    public void connect() {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void subscribeToTopic() {
        LogUtil.d(TAG, "subscribeTopic is " + mSubscribeTopic);
        try {
            mqttAndroidClient.subscribe(mSubscribeTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LogUtil.d(TAG, "subscribe successfully");
                    dismissProgressDialog();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessage(String command) {
        try {
            String sendMsg = "{\"version\": \"v1\", \"gateway_id\": \"" + mGateWayId
                    + "\",\"type\": \"DEVICE_CMD\", \"device\": {\"payload\": \""
                    + command + "\", \"mac\": \"" + mLockMac + "\", \"type\": \"CON\"}}";
            LogUtil.d(TAG, "send Message is " + sendMsg);
            MqttMessage message = new MqttMessage();
            message.setQos(0);
            message.setPayload(sendMsg.getBytes());
            LogUtil.d(TAG, "publishTopic is " + mPublishTopic + ", message is " + message);
            mqttAndroidClient.publish(mPublishTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        Intent intent = new Intent(LockController.BROADCAST_CONNECT);
        sendBroadcast(intent);
    }

    private class GateWayCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            LogUtil.d(TAG, "The connection was lost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LogUtil.d(TAG, "topic is " + topic + ", Qos is " + message.getQos() + ", content is " + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            LogUtil.d(TAG, "deliveryComplete..." + token.isComplete());
        }
    }
}
