package com.eliasbagley.rxmqtt.impl;

import com.eliasbagley.rxmqtt.enums.RxMqttClientState;
import com.eliasbagley.rxmqtt.enums.RxMqttExceptionType;
import com.eliasbagley.rxmqtt.exceptions.RxMqttException;
import com.eliasbagley.rxmqtt.interfaces.IRxMqttClient;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by xudshen@hotmail.com on 14-7-21.
 */
public abstract class RxMqttClient implements IRxMqttClient {
    private MqttConnectOptions                 conOpt;
    private RxMqttClientStatus                 status;
    private PublishSubject<RxMqttClientStatus> statusSubject;

    protected RxMqttClient() {
        //init status
        status = new RxMqttClientStatus();
        status.setLogTime(new Timestamp(System.currentTimeMillis()));
        status.setState(RxMqttClientState.Init);
        statusSubject = PublishSubject.create();
    }

    public MqttConnectOptions getConOpt() {
        return conOpt;
    }

    public void setConnectionOptions(MqttConnectOptions conOpt) {
        this.conOpt = conOpt;
    }


    public void updateState(RxMqttClientState state) {
        if (this.status.getState() != state) {
            this.status.setState(state);
            this.status.setLogTime(new Timestamp(System.currentTimeMillis()));
            statusSubject.onNext((RxMqttClientStatus) status.clone());
        }
    }

    @Override
    public Observable<RxMqttClientStatus> statusReport() {
        return statusSubject;
    }
}
