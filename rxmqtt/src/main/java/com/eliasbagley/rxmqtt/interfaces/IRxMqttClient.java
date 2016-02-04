package com.eliasbagley.rxmqtt.interfaces;

import com.eliasbagley.rxmqtt.impl.RxMqttClientStatus;
import com.eliasbagley.rxmqtt.impl.Message;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.regex.Pattern;

import rx.Observable;

public interface IRxMqttClient {

    Observable<IMqttToken> connect();

    Observable<IMqttToken> disconnect();

    void disconnectForcibly();

    Observable<IMqttToken> subscribeTopic(String topic, int qos);

    Observable<Message> subscribing(String regularExpression);

    Observable<Message> subscribing(Pattern pattern);


    Observable<IMqttToken> publish(String topic, byte[] msg, int qos);

    Observable<IMqttToken> publish(String topic, byte[] msg);

    Observable<RxMqttClientStatus> statusReport();

    Observable<IMqttToken> checkPing(Object userContext);

    /*

     */

    Observable<Message> topic(String topic, int qos);
}
