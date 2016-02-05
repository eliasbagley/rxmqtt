package com.eliasbagley.rxmqtt.impl;

import android.support.annotation.NonNull;

import com.eliasbagley.rxmqtt.constants.QoS;

/**
 * Created by eliasbagley on 2/5/16.
 *
 * Represents a response from a call to publish
 */

public class PublishResponse {
    @NonNull private String  topic;
    @NonNull private String  message;
    @NonNull private QoS     qos;
    private          boolean retained;

    public PublishResponse(@NonNull String topic, @NonNull String message, @NonNull QoS qos, boolean retained) {
        this.topic = topic;
        this.message = message;
        this.qos = qos;
        this.retained = retained;
    }

    //region getters

    @NonNull
    public String getTopic() {
        return topic;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public QoS getQos() {
        return qos;
    }

    public boolean isRetained() {
        return retained;
    }

    @Override
    public String toString() {
        return String.format("topic: %s, message: %s, qos: %d, retained %b", topic, message, qos.getValue(), retained);
    }

    //endregion
}
