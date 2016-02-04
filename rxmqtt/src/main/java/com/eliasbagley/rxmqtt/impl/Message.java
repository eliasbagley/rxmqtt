package com.eliasbagley.rxmqtt.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Message {
    @NonNull private String      topic;
    @NonNull private MqttMessage rxMessage;

    public Message(@NonNull String topic, @NonNull MqttMessage rxMessage) {
        this.topic = topic;
        this.rxMessage = rxMessage;
    }

    @NonNull
    public String getTopic() {
        return topic;
    }

    @Nullable
    public String getMessage() {
        if (rxMessage == null) {
            return null;
        }

        return new String(rxMessage.getPayload());
    }

    public int getQos() {
        return rxMessage == null ? -1 : rxMessage.getQos();
    }

    @Override
    public String toString() {
        return String.format("topic: %s; msg: %s; qos: %d", getTopic(), getMessage(), getQos());
    }
}
