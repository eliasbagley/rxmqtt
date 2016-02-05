package com.eliasbagley.rxmqtt.impl;


import com.eliasbagley.rxmqtt.constants.QoS;
import com.eliasbagley.rxmqtt.enums.State;
import com.eliasbagley.rxmqtt.enums.RxMqttExceptionType;
import com.eliasbagley.rxmqtt.exceptions.RxMqttException;

import com.eliasbagley.rxmqtt.exceptions.RxMqttTokenException;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.eliasbagley.rxmqtt.enums.State.*;
import static com.eliasbagley.rxmqtt.constants.QoS.*;

//TODO wut does the "Context" String do?
public class RxMqttClient {
    private MqttConnectOptions connectOptions;
    private Status             status;
    private BehaviorSubject<Status> statusSubject = BehaviorSubject.create();

    private MqttAsyncClient client;
    private Map<String, Pattern>                 patternHashtable = new Hashtable<>();
    private Map<String, PublishSubject<Message>> subjectHashtable = new Hashtable<>();

    public RxMqttClient(String brokerUrl, String clientId, MqttClientPersistence persistence, MqttConnectOptions connectOptions) {
        this.connectOptions = connectOptions;

        try {
            updateState(INIT);
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

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("delivery complete");
            }
        });
    }

//    private void connect(final Subscriber<? super IMqttToken> subscriber) {
//        try {
//            System.out.println("Connecting..");
//            updateState(State.CONNECTING);
//            client.connect(connectOptions, "Context", new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    System.out.println("Connection success");
//                    updateState(State.CONNECTED);
//                    subscriber.onNext(asyncActionToken);
//                    subscriber.onCompleted();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    System.out.println("Connection failed");
//                    updateState(State.CONNECTION_FAILED);
//                    subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
//                }
//            });
//        } catch (MqttException ex) {
//            updateState(State.CONNECTION_FAILED);
//            subscriber.onError(ex);
//        }
//    }

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

//    @Override
//    public Observable<IMqttToken> connect() {
//        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
//            @Override
//            public void call(final Subscriber<? super IMqttToken> subscriber) {
//                if (client == null) {
//                    updateState(State.CONNECTION_FAILED);
//                    subscriber.onError(new RxMqttException(RxMqttExceptionType.CLIENT_NULL_ERROR));
//                }
//
//                disconnect().subscribe(new Observer<IMqttToken>() {
//                    @Override
//                    public void onCompleted() {
//                        connect(subscriber);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        connect(subscriber);
//                    }
//
//                    @Override
//                    public void onNext(IMqttToken iMqttToken) {
//                    }
//                });
//            }
//        });
//    }

    //TODO this method is ugly
    public Observable<IMqttToken> disconnect() {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (client != null && client.isConnected()) {
                    try {
                        updateState(DISCONNECTING);
                        client.disconnect("Context", new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                updateState(State.DISCONNECTED);
                                subscriber.onNext(asyncActionToken);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                updateState(State.DISCONNECTED);
                                subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                            }
                        });
                    } catch (MqttException e) {
                        updateState(State.DISCONNECTED);
                        subscriber.onError(e);
                    }
                } else {
                    subscriber.onCompleted();
                }
            }
        });
    }

    public Observable<IMqttToken> publish(final String topic, final byte[] msg) {
        return publish(topic, msg, 1); /* default QoS of 1 */
    }

    public Observable<IMqttToken> publish(final String topic, final byte[] msg, int qos) {
        final MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setPayload(msg);

        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (client == null) {
                    subscriber.onError(new RxMqttException(RxMqttExceptionType.CLIENT_NULL_ERROR));
                }

                try {
                    client.publish(topic, message, "Context", new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            subscriber.onNext(asyncActionToken);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                        }
                    });
                } catch (MqttException ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    public Observable<Message> topic(final String topic) {
        return topic(topic, AT_LEAST_ONCE);
    }

    public Observable<Message> topic(final String topic, final QoS qos) {
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
                        return subscribeTopic(topic, qos.getValue());
                    }
                });
    }

    //TODO factor in the passed in qos
    private Observable<Message> subscribeTopic(final String topic, final int qos) {
        System.out.println(String.format("Subscribing to topic: %s topic", topic));
//        final PublishSubject<Message> subject = PublishSubject.create();

        final Observable<Message> obs = subscribing(topic); //TODO factor qos into this
        try {
            client.subscribe(topic, qos, "context", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscribed to topic successfully");
//                    subject.onNext(asyncActionToken);
//                    subject.onCompleted();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed subscribing to topic..");
                    exception.printStackTrace();
//                    obs.onError(exception);
                }
            });
        } catch (MqttException e) {
//            obs.onError(e);
        }

//        return subject;
        return obs;


//        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
//            @Override
//            public void call(final Subscriber<? super IMqttToken> subscriber) {
//                try {
//                    client.subscribe(topic, qos, "Context", new IMqttActionListener() {
//                        @Override
//                        public void onSuccess(IMqttToken asyncActionToken) {
//                            subscriber.onNext(asyncActionToken);
//                            subscriber.onCompleted();
//                        }
//
//                        @Override
//                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                            subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
//                        }
//                    });
//                } catch (MqttException ex) {
//                    subscriber.onError(ex);
//                }
//            }
//        });
    }

    public Observable<Message> subscribing(String regularExpression) {
        return subscribing(Pattern.compile(regularExpression));
    }

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
