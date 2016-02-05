package com.eliasbagley.rxmqtt.impl;

import android.support.annotation.NonNull;

import com.eliasbagley.rxmqtt.constants.QoS;

/**
 * Created by eliasbagley on 2/4/16.
 */
public class Will {
    @NonNull private String  topic;
    @NonNull private String  message;
    @NonNull private QoS     qos;
    private          boolean retained;

    public Will(@NonNull String topic, @NonNull String message, @NonNull QoS qos, boolean retained) {
        this.topic = topic;
        this.message = message;
        this.qos = qos;
        this.retained = retained;
    }

    public Will(@NonNull String topic, @NonNull String message) {
        this(topic, message, QoS.AT_LEAST_ONCE, false);
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
    public byte[] getMessageBytes() {
        return getMessage().getBytes();
    }

    @NonNull
    public QoS getQoS() {
        return this.qos;
    }

    public boolean isRetained() {
        return retained;
    }

    //endregion
}
