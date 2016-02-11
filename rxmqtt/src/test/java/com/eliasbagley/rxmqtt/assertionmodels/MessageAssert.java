package com.eliasbagley.rxmqtt.assertionmodels;

import com.eliasbagley.rxmqtt.Message;
import com.eliasbagley.rxmqtt.constants.QoS;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.google.common.truth.Truth.*;

/**
 * Created by eliasbagley on 2/11/16.
 */
public final class MessageAssert {
    private final Message message;

    public MessageAssert(Message message) {
        this.message = message;
    }

    public MessageAssert withQos(QoS qos) {
        assertThat(message.getQos()).isEqualTo(qos.getValue());
        return this;
    }

    public MessageAssert onTopic(String topic) {
        assertThat(message.getTopic()).isEqualTo(topic);
        return this;
    }

    public MessageAssert hasPayload(String payload) {
        return hasPayload(payload.getBytes());
    }

    public MessageAssert hasPayload(byte[] payload) {
        assertThat(message.getMessageBytes()).isEqualTo(payload);
        return this;
    }

    public MessageAssert isEqualToMessage(MqttMessage m) {
        assertThat(message.getQos()).isEqualTo(m.getQos());
        assertThat(message.getMessageBytes()).isEqualTo(m.getPayload());
        return this;
    }
}

