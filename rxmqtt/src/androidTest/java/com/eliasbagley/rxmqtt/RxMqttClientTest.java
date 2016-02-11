package com.eliasbagley.rxmqtt;

import com.eliasbagley.rxmqtt.constants.QoS;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;

import rx.Subscriber;
import rx.Subscription;

import static com.google.common.truth.Truth.*;
import static com.eliasbagley.rxmqtt.FakeMqttMessage.*;
import static com.eliasbagley.rxmqtt.constants.QoS.*;

/**
 * Created by eliasbagley on 2/11/16.
 */

public class RxMqttClientTest {

    MqttAsyncClient   fakeClient;
    RecordingObserver o;
    RxMqttClient      rxclient;

    @Before
    public void init() {
        try {
            fakeClient = new FakeClient();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        o = new RecordingObserver();
        rxclient = new RxMqttClient(fakeClient, new MqttConnectOptions(), new Gson());

//        client = mock(MqttAsyncClient.class);
//
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                MqttCallback callback = invocation.getArgumentAt(0, MqttCallback.class);
//                callback.messageArrived(TOPIC_1, MESSAGE_1);
//                return null;
//            }
//        }).when(client).setCallback(any(MqttCallback.class));
    }

    //TODO add the recording observer
    @Test
    public void testThatSingleMessageArrives() {
        rxclient.onTopic(TOPIC_1).subscribe(o);

        publish(TOPIC_1, MESSAGE_1);

        o.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        o.assertNoMoreEvents();
    }

    @Test
    public void testThatNothingArrivesFromDifferentTopic() {
        rxclient.onTopic(TOPIC_1).subscribe(o);

        publish(TOPIC_2, MESSAGE_1);

        o.assertNoMoreEvents();
    }

    @Test
    public void testThatNothingArrivesAfterUnsubscription() {
        Subscription subscription = rxclient.onTopic(TOPIC_1).subscribe(o);

        publish(TOPIC_1, MESSAGE_1);

        o.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        o.assertNoMoreEvents();

        subscription.unsubscribe();

        publish(TOPIC_1, MESSAGE_2);

        o.assertNoMoreEvents();

    }

    @Test
    public void testMultipleMessagesArrive() {
        rxclient.onTopic(TOPIC_1).subscribe(o);

        publish(TOPIC_1, MESSAGE_1);

        o.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        o.assertNoMoreEvents();

        publish(TOPIC_1, MESSAGE_1);
        publish(TOPIC_1, MESSAGE_2);

        o.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        o.assertMessage()
                .isEqualToMessage(MESSAGE_2)
                .onTopic(TOPIC_1);

        o.assertNoMoreEvents();
    }

    @Test
    public void testMessageArrivesWithQoS() {
        QoS qos = EXACTLY_ONCE;

        rxclient.onTopic(TOPIC_1, qos).subscribe(o);

        MqttMessage m = createMessage("payload", qos);

        publish(TOPIC_1, m);

        o.assertMessage()
                .isEqualToMessage(m)
                .onTopic(TOPIC_1);

        o.assertNoMoreEvents();
    }

    //region private helper methods

    private void publish(String topic, MqttMessage message) {
        try {
            fakeClient.publish(topic, message);
        } catch (Exception e) {
            assertThat(true).isFalse(); //TODO abstract this out
        }
    }

    public MqttMessage createMessage(String payload, QoS qos) {
        MqttMessage m = new MqttMessage("payload".getBytes());
        m.setQos(qos.getValue());
        return m;
    }

    //endregion
}
