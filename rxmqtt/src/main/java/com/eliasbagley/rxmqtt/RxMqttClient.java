package com.eliasbagley.rxmqtt;


import android.support.annotation.VisibleForTesting;

import com.eliasbagley.rxmqtt.constants.QoS;
import com.eliasbagley.rxmqtt.constants.State;
import com.eliasbagley.rxmqtt.exceptions.RxMqttTokenException;
import com.google.gson.Gson;

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

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.eliasbagley.rxmqtt.constants.QoS.AT_LEAST_ONCE;
import static com.eliasbagley.rxmqtt.constants.State.CONNECTED;
import static com.eliasbagley.rxmqtt.constants.State.CONNECTING;
import static com.eliasbagley.rxmqtt.constants.State.CONNECTION_FAILED;
import static com.eliasbagley.rxmqtt.constants.State.CONNECTION_LOST;
import static com.eliasbagley.rxmqtt.constants.State.DISCONNECTED;
import static com.eliasbagley.rxmqtt.constants.State.DISCONNECTING;
import static com.eliasbagley.rxmqtt.constants.State.INITIALIZING;

//TODO wut does the "Context" String do?
//TODO add an initialized state?
public class RxMqttClient {
    private MqttConnectOptions connectOptions;
    private Status             status;
    private BehaviorSubject<Status> statusSubject = BehaviorSubject.create();

    private MqttAsyncClient client;
    private Map<String, Pattern>                 patternHashtable = new Hashtable<>();
    private Map<String, PublishSubject<Message>> subjectHashtable = new Hashtable<>();
    private Gson gson;

    @VisibleForTesting
    public RxMqttClient(MqttAsyncClient client, MqttConnectOptions connectOptions, Gson gson) {
        updateState(INITIALIZING);
        this.client = client;
        this.connectOptions = connectOptions;
        this.gson = gson;
        createListener();
    }

    public RxMqttClient(String brokerUrl, String clientId, MqttClientPersistence persistence, MqttConnectOptions connectOptions, Gson gson) {
        this.connectOptions = connectOptions;
        this.gson = gson;

        try {
            updateState(INITIALIZING);
            client = new MqttAsyncClient(brokerUrl, clientId, persistence);
            createListener();
        } catch (MqttException ex) {
            throw new RuntimeException(String.format("Mqtt client init error, %s", ex.getMessage())); // Convert checked to unchecked exception
        }
    }

    private void createListener() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                cause.printStackTrace();
                updateState(CONNECTION_LOST);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                System.out.println(String.format("Message arrived on topic: %s", topic));

                if (message.getPayload().length != 0) {
                    for (String key : patternHashtable.keySet()) {
                        if (patternHashtable.get(key).matcher(topic).matches()) {
                            subjectHashtable.get(key).onNext(new Message(topic, message));
                        }
                    }
                }
            }

            //TODO use this to track publishing
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("delivery complete");
            }
        });
    }

    // This version of connect tries to take into account the status so connect can't be called if already connected
    //TODO test
    public Observable<RxMqttClient> con() {
        return status()
                .filter(new Func1<Status, Boolean>() {
                    @Override
                    public Boolean call(Status status) {
                        return !(status.isConnected() || status.isConnecting());
                    }
                })
                .timeout(5, TimeUnit.SECONDS)
                .take(1)
                .flatMap(new Func1<Status, Observable<RxMqttClient>>() {
                    @Override
                    public Observable<RxMqttClient> call(Status status) {
                        return connect();
                    }
                });
    }

    public Observable<RxMqttClient> connect() {
        final PublishSubject<RxMqttClient> subject = PublishSubject.create();

        try {
            updateState(CONNECTING);
            client.connect(connectOptions, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    updateState(CONNECTED);

                    subject.onNext(RxMqttClient.this);
                    subject.onCompleted();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    updateState(CONNECTION_FAILED);
                    subject.onError(exception);
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
            updateState(CONNECTION_FAILED);
            subject.onError(ex);
        }

        return subject;
    }

    public Observable<RxMqttClient> disconnect() {
        final PublishSubject<RxMqttClient> subject = PublishSubject.create();

        try {
            updateState(DISCONNECTING);
            client.disconnect("Context", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    updateState(State.DISCONNECTED);
                    subject.onNext(RxMqttClient.this);
                    subject.onCompleted();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    updateState(State.DISCONNECTED);
                    subject.onError(exception);
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
            updateState(DISCONNECTED);
            subject.onError(ex);
        }

        return subject;
    }

    /**
     * Publish convenience method which uses gson to serialize an object as the body
     *
     * @param topic
     * @param body Json object to be serialized
     * @return
     */

    public Observable<PublishResponse> publish(final String topic, Object body) {
        return publish(topic, gson.toJson(body));
    }

    /**
     * Convenience method
     *
     * @param topic
     * @param message
     * @return
     */
    public Observable<PublishResponse> publish(final String topic, final String message) {
        return publish(topic, message, AT_LEAST_ONCE, false);
    }

    /**
     * Publishes only when connected the message on the topic only when connected
     *
     * @param topic
     * @param message
     * @param qos AT_MOST_ONCE, AT_LEAST_ONCE, EXACTLY_ONCE
     * @param retained whether or not the broker will retain the message to be delivered immediately to future subscribers
     * @return
     */

    public Observable<PublishResponse> publish(final String topic, final String message, final QoS qos, final boolean retained) {
        return status()
                .filter(new Func1<Status, Boolean>() {
                    @Override
                    public Boolean call(Status status) {
                        return status.isConnected();
                    }
                })
                .take(1)
                .flatMap(new Func1<Status, Observable<PublishResponse>>() {
                    @Override
                    public Observable<PublishResponse> call(Status status) {
                        return publishHelper(topic, message, qos, retained);
                    }
                });
    }

    private Observable<PublishResponse> publishHelper(final String topic, final String message, final QoS qos, final boolean retained) {
        final PublishSubject<PublishResponse> subject = PublishSubject.create();

        MqttMessage m = new MqttMessage(message.getBytes());
        m.setRetained(retained);
        m.setQos(qos.getValue());

        try {
            //TODO verify if the onSuccess guarantees the delivery. This might have to be combined with the other callback
            client.publish(topic, m, "Context", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subject.onNext(new PublishResponse(topic, message, qos, retained));
                    subject.onCompleted();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                    subject.onError(exception);
                }
            });
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
            subject.onError(e);
        } catch (MqttException e) {
            e.printStackTrace();
            subject.onError(e);
        }

        return subject;
    }

    /**
     * Convenience method for subscribing to a topic with default QoS AT_LEAST_ONCE
     *
     * @param topic
     * @return
     */

    public Observable<Message> onTopic(final String topic) {
        return onTopic(topic, AT_LEAST_ONCE);
    }

    /**
     * Returns an observable which subscribes to the topic with the given qos
     *
     * @param topic
     * @param qos
     * @return
     */

    public Observable<Message> onTopic(final String topic, final QoS qos) {
        return status()
                .filter(new Func1<Status, Boolean>() {
                    @Override
                    public Boolean call(Status status) {
                        return status.isConnected();
                    }
                })
                .take(1)
                .flatMap(new Func1<Status, Observable<Message>>() {
                    @Override
                    public Observable<Message> call(Status status) {
                        return subscribeTopic(topic, qos);
                    }
                });
    }

    private Observable<Message> subscribeTopic(final String topic, final QoS qos) {
        final Observable<Message> obs = subscribing(topic);

        try {
            client.subscribe(topic, qos.getValue(), "context", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return obs;
    }

    private Observable<Message> subscribing(String regularExpression) {
        return subscribing(Pattern.compile(regularExpression));
    }

    // TODO these might need to be purged at some point...
    public synchronized Observable<Message> subscribing(final Pattern pattern) {
        if (patternHashtable.containsKey(pattern.pattern())) {
            return subjectHashtable.get(pattern.pattern());
        } else {
            patternHashtable.put(pattern.pattern(), pattern);
            PublishSubject<Message> subject = PublishSubject.create();
            subjectHashtable.put(pattern.pattern(), subject);
            return subjectHashtable.get(pattern.pattern());
        }
    }

    public void disconnectForcibly() {
        try {
            client.disconnectForcibly();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public Observable<IMqttToken> checkPing(final Object userContext) {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    try {
                        IMqttToken mqttToken = client.checkPing(userContext, null);
                        subscriber.onNext(mqttToken);
                        subscriber.onCompleted();
                    } catch (MqttException e) {
                        subscriber.onError(new RxMqttTokenException(e, null));
                    }
                }
            }
        });
    }

    //region status reporting

    private void updateState(State state) {
        // Initialize status if it hasn't been before
        if (status == null) {
            status = new Status(state);
            statusSubject.onNext(status);
            return;
        }

        // Update the status and push out an update if the state has changed
        if (status.getState() != state) {
            status = new Status(state);
            statusSubject.onNext(status);
        }
    }

    public Observable<Status> status() {
        return statusSubject;
    }
    //endregion
}
