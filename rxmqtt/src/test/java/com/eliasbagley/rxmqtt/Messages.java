package com.eliasbagley.rxmqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by eliasbagley on 2/11/16.
 */
public final class Messages {
    public static final MqttMessage MESSAGE_1 = new MqttMessage("payload1".getBytes());
    public static final MqttMessage MESSAGE_2 = new MqttMessage("payload2".getBytes());
    public static final MqttMessage MESSAGE_3 = new MqttMessage("payload3".getBytes());

    public static final String TOPIC_1 = "topic/testing/1";
    public static final String TOPIC_2 = "topic/testing/2";

    private Messages() {
        throw new AssertionError("No instances.");
    }
}
