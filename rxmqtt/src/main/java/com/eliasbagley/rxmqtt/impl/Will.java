package com.eliasbagley.rxmqtt.impl;

import com.eliasbagley.rxmqtt.constants.QoS;

/**
 * Created by eliasbagley on 2/4/16.
 */
public class Will {
    private String topic;
    private String message;
    private QoS qos;
    private boolean retained;

    public Will(String topic, String message, QoS qos, boolean retained) {
        this.topic = topic;
        this.message = message;
        this.qos = qos;
        this.retained = retained;
    }

    public Will(String topic, String message) {
        this(topic, message, QoS.AT_LEAST_ONCE, false);
    }

    //region getters

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getMessageBytes() {
        return getMessage().getBytes();
    }

    public QoS getQoS() {
        return this.qos;
    }

    public boolean isRetained() {
        return retained;
    }

    //endregion
}
