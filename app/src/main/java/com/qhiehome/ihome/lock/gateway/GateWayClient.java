package com.qhiehome.ihome.lock.gateway;

import android.content.Context;
import android.content.Intent;

import com.qhiehome.ihome.lock.ConnectLockService;
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

public class GateWayClient {

    private static final String TAG = GateWayClient.class.getSimpleName();

    private static volatile GateWayClient gateWayClient;

    private static final String HOST = "tcp://www.klmiot.tk:1883";

    private String gateWayId;

    private String lockMac;

    private MqttAndroidClient mqttAndroidClient;

    private String subscribeTopic = "/status/lock/ap/v2/";

    private String publishTopic = "/set/lock/ap/v2/";

    private MqttConnectOptions mqttConnectOptions;

    private Context mContext;

    private GateWayClient(Context context, String gateWayId, String lockMac) {
        this.mContext = context;
        this.gateWayId = gateWayId;
        this.lockMac = lockMac;
        subscribeTopic += this.gateWayId;
        publishTopic += this.gateWayId;
        GateWayCallback gateWayCallback = new GateWayCallback();
        mqttAndroidClient = new MqttAndroidClient(context.getApplicationContext(), HOST, MqttClient.generateClientId());
        mqttAndroidClient.setCallback(gateWayCallback);
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
    }

    public static GateWayClient getInstance(Context context, String gateWayId, String lockMac) {
        if (gateWayClient == null) {
            synchronized (GateWayClient.class) {
                if (gateWayClient == null) {
                    gateWayClient = new GateWayClient(context, gateWayId, lockMac);
                }
            }
        }
        return gateWayClient;
    }

    public void connect() {
        try {
            if (mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
            }
            mqttAndroidClient.connect(mqttConnectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LogUtil.d(TAG, "Failed to connect to: " + HOST);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return mqttAndroidClient != null && mqttAndroidClient.isConnected();
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscribeTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LogUtil.d(TAG, "subscribe successfully");
                    Intent intent = new Intent(ConnectLockService.BROADCAST_CONNECT);
                    mContext.sendBroadcast(intent);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LogUtil.d(TAG, "subscribe failure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String command) {
        try {
            String sendMsg = "{\"version\": \"v1\", \"gateway_id\": \"" + gateWayId
                    + "\",\"type\": \"DEVICE_CMD\", \"device\": {\"payload\": \""
                    + command + "\", \"mac\": \"" + lockMac + "\", \"type\": \"CON\"}}";
            LogUtil.d(TAG, "send Message is " + sendMsg);
            MqttMessage message = new MqttMessage();
            message.setQos(0);
            message.setPayload(sendMsg.getBytes());
            LogUtil.d(TAG, "publishTopic is " + publishTopic + ", message is " + message);
            mqttAndroidClient.publish(publishTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
