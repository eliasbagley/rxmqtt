package com.eliasbagley.rxmqtt;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.TimerPingSender;

import java.util.Hashtable;
import java.util.Map;

import rx.subjects.PublishSubject;

/**
 * Created by eliasbagley on 2/11/16.
 */

//TODO model support for wildcard topics
//TODO model support for the keep alive interval
public class FakeMqttClient extends MqttAsyncClient {
    private boolean      connected;
    private MqttCallback callback;

    private Map<String, PublishSubject<Message>> subscriptions = new Hashtable<>();

    //region flags to force certain callbacks
    private boolean failConnecting = false;

    public FakeMqttClient(String serverURI, String clientId) throws MqttException {
        this(serverURI, clientId, null, new TimerPingSender());
    }

    public FakeMqttClient(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        this(serverURI, clientId, null, new TimerPingSender());
    }

    public FakeMqttClient(String serverURI, String clientId, MqttClientPersistence persistence, MqttPingSender pingSender) throws MqttException {
        super(serverURI, clientId, persistence, pingSender);
    }

    // Initialize with some defaults
    public FakeMqttClient() throws MqttException {
        this("tcp://localhost:1883", "test-client-id");
    }

    //region setters

    public void setFailConnecting(boolean failConnecting) {
        this.failConnecting = failConnecting;
    }

    //endregion


    //region connect
    @Override
    public IMqttToken connect() throws MqttException, MqttSecurityException {
        return connect(null, null, null);
    }

    @Override
    public IMqttToken connect(MqttConnectOptions options) throws MqttException, MqttSecurityException {
        return connect(options, null, null);
    }

    @Override
    public IMqttToken connect(Object userContext, IMqttActionListener callback) throws MqttException, MqttSecurityException {
        return connect(null, userContext, callback);
    }

    @Override
    public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback) throws MqttException, MqttSecurityException {
        this.connected = true;
        if (callback != null) {
            if (failConnecting) {
                callback.onFailure(null, new RuntimeException("Failed connecting"));
            } else {
                callback.onSuccess(null);
            }
        }
        return null;
    }
    //endregion connect

    //region disconnect
    @Override
    public IMqttToken disconnect() throws MqttException {
        return disconnect(0, null, null);
    }

    @Override
    public IMqttToken disconnect(long quiesceTimeout) throws MqttException {
        return disconnect(quiesceTimeout, null, null);
    }

    @Override
    public IMqttToken disconnect(Object userContext, IMqttActionListener callback) throws MqttException {
        return disconnect(0, userContext, callback);
    }

    @Override
    public IMqttToken disconnect(long quiesceTimeout, Object userContext, IMqttActionListener callback) throws MqttException {
        this.connected = false;
        if (callback != null) {
            callback.onSuccess(null);
        }
        return null;
    }

    //region disconnect

    @Override
    public void disconnectForcibly() throws MqttException {
        this.connected = false;
    }

    @Override
    public void disconnectForcibly(long disconnectTimeout) throws MqttException {
        disconnectForcibly();
    }

    @Override
    public void disconnectForcibly(long quiesceTimeout, long disconnectTimeout) throws MqttException {
        disconnectForcibly();
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException, MqttPersistenceException {
        return publish(topic, payload, qos, retained, null, null);
    }

    @Override
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained, Object userContext, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        MqttMessage m = new MqttMessage(payload);
        m.setQos(qos);
        m.setRetained(retained);
        return publish(topic, m, null, null);
    }

    @Override
    public IMqttDeliveryToken publish(String topic, MqttMessage message) throws MqttException, MqttPersistenceException {
        Message m = new Message(topic, message);

        if (subscriptions.get(topic) != null) {
            PublishSubject<Message> publishSubject = subscriptions.get(topic);
            publishSubject.onNext(m);

            if (callback != null) {
                try {
                    callback.messageArrived(topic, message);
                } catch (Exception e) {
                    callback.connectionLost(e);
                }
            }
        }

        return null;
    }

    @Override
    public IMqttDeliveryToken publish(String topic, MqttMessage message, Object userContext, IMqttActionListener callback) throws MqttException, MqttPersistenceException {
        if (callback != null) {
            callback.onSuccess(null);
        }

        return publish(topic, message);
    }

    @Override
    public IMqttToken subscribe(String topicFilter, int qos) throws MqttException {
        return subscribe(topicFilter, qos, null, null);
    }

    @Override
    public IMqttToken subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback) throws MqttException {
        if (subscriptions.get(topicFilter) == null) {
            PublishSubject<Message> publishSubject = PublishSubject.create();
            subscriptions.put(topicFilter, publishSubject);

            if (callback != null) {
                callback.onSuccess(null);
            }
        }

        return null;
    }

    @Override
    public IMqttToken subscribe(String[] topicFilters, int[] qos) throws MqttException {
        return subscribe(topicFilters, qos, null, null);
    }

    @Override
    public IMqttToken subscribe(String[] topicFilters, int[] qos, Object userContext, IMqttActionListener callback) throws MqttException {
        for (int i = 0; i < topicFilters.length; i++) {
            subscribe(topicFilters[i], qos[i], userContext, callback);
        }

        return null;
    }

    //region unsubscribe

    @Override
    public IMqttToken unsubscribe(String topicFilter) throws MqttException {
        subscriptions.remove(topicFilter);
        return null;
    }

    @Override
    public IMqttToken unsubscribe(String[] topicFilters) throws MqttException {
        for (String topic : topicFilters) {
            unsubscribe(topic);
        }

        return null;
    }

    @Override
    public IMqttToken unsubscribe(String topicFilter, Object userContext, IMqttActionListener callback) throws MqttException {
        callback.onSuccess(null);
        return unsubscribe(topicFilter);
    }

    @Override
    public IMqttToken unsubscribe(String[] topicFilters, Object userContext, IMqttActionListener callback) throws MqttException {
        for (String topic : topicFilters) {
            unsubscribe(topic, userContext, callback);
        }

        return null;
    }

    //endregion

    @Override
    public void setCallback(final MqttCallback callback) {
        this.callback = callback;
    }

    @Override
    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return new IMqttDeliveryToken[0];
    }

    @Override
    public void close() throws MqttException {
    }
}
