package com.eliasbagley.rxmqtt;

import android.support.annotation.NonNull;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Message {
    @NonNull private String      topic;
    @NonNull private MqttMessage mqttMessage;
    @NonNull private String message;
    private int qos;

    public Message(@NonNull String topic, @NonNull MqttMessage mqttMessage) {
        this.topic = topic;
        this.mqttMessage = mqttMessage;
        this.message = new String(mqttMessage.getPayload());
        this.qos = mqttMessage.getQos();
    }

    @NonNull
    public String getTopic() {
        return topic;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public byte[] getMessageBytes() {
        return getMessage().getBytes();
    }

    //TODO use QoS enum
    public int getQos() {
        return qos;
    }

    @Override
    public String toString() {
        return String.format("topic: %s\n message: %s\n qos: %d", getTopic(), getMessage(), getQos());
    }
}
