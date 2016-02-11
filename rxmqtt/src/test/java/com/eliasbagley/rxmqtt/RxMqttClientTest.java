package com.eliasbagley.rxmqtt;

import com.eliasbagley.rxmqtt.constants.QoS;
import com.eliasbagley.rxmqtt.recordingobservers.MessageRecordingObserver;
import com.eliasbagley.rxmqtt.recordingobservers.PublishResponseRecordingObserver;
import com.eliasbagley.rxmqtt.recordingobservers.StatusRecordingObserver;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rx.Subscription;

import static com.eliasbagley.rxmqtt.Messages.MESSAGE_1;
import static com.eliasbagley.rxmqtt.Messages.MESSAGE_2;
import static com.eliasbagley.rxmqtt.Messages.TOPIC_1;
import static com.eliasbagley.rxmqtt.Messages.TOPIC_2;
import static com.eliasbagley.rxmqtt.constants.QoS.EXACTLY_ONCE;
import static com.eliasbagley.rxmqtt.constants.State.*;

/**
 * Created by eliasbagley on 2/11/16.
 */

public class RxMqttClientTest {
    FakeMqttClient fakeMqttClient;
    RxMqttClient   rxclient;

    // Recording observers
    MessageRecordingObserver         mo;
    StatusRecordingObserver          so;
    PublishResponseRecordingObserver pro;

    @Before
    public void init() {
        try {
            fakeMqttClient = new FakeMqttClient();
        } catch (MqttException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        mo = new MessageRecordingObserver();
        so = new StatusRecordingObserver();
        pro = new PublishResponseRecordingObserver();

        rxclient = new RxMqttClient(fakeMqttClient, new MqttConnectOptions(), new Gson());
        rxclient.connect();
    }

    @Test
    public void testThatSingleMessageArrives() {
        rxclient.onTopic(TOPIC_1).subscribe(mo);

        publish(TOPIC_1, MESSAGE_1);

        mo.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        mo.assertNoMoreEvents();
    }

    @Test
    public void testThatNothingArrivesFromDifferentTopic() {
        rxclient.onTopic(TOPIC_1).subscribe(mo);

        publish(TOPIC_2, MESSAGE_1);

        mo.assertNoMoreEvents();
    }

    @Test
    public void testThatNothingArrivesAfterUnsubscription() {
        Subscription subscription = rxclient.onTopic(TOPIC_1).subscribe(mo);

        publish(TOPIC_1, MESSAGE_1);

        mo.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        mo.assertNoMoreEvents();

        subscription.unsubscribe();

        publish(TOPIC_1, MESSAGE_2);

        mo.assertNoMoreEvents();

    }

    @Test
    public void testMultipleMessagesArrive() {
        rxclient.onTopic(TOPIC_1).subscribe(mo);

        publish(TOPIC_1, MESSAGE_1);

        mo.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        mo.assertNoMoreEvents();

        publish(TOPIC_1, MESSAGE_1);
        publish(TOPIC_1, MESSAGE_2);

        mo.assertMessage()
                .isEqualToMessage(MESSAGE_1)
                .onTopic(TOPIC_1);

        mo.assertMessage()
                .isEqualToMessage(MESSAGE_2)
                .onTopic(TOPIC_1);

        mo.assertNoMoreEvents();
    }

    @Test
    public void testMessageArrivesWithQoS() {
        QoS qos = EXACTLY_ONCE;

        rxclient.onTopic(TOPIC_1, qos).subscribe(mo);

        MqttMessage m = createMessage("payload", qos);

        publish(TOPIC_1, m);

        mo.assertMessage()
                .isEqualToMessage(m)
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();
    }

    //region test status

    @Test
    public void testStatusConnected() {
        rxclient.status().subscribe(so);

        so.assertStatus()
                .hasState(CONNECTED);

        so.assertNoMoreEvents();
    }

    @Test
    public void testStatusDisconnect() {
        rxclient.status().subscribe(so);

        so.assertStatus().hasState(CONNECTED);
        so.assertNoMoreEvents();

        rxclient.disconnect();

        so.assertStatus().hasState(DISCONNECTING);
        so.assertStatus().hasState(DISCONNECTED);
        so.assertNoMoreEvents();
    }

    @Test
    public void testStatusDisconnectForcibly() {
        rxclient.status().subscribe(so);

        so.assertStatus().hasState(CONNECTED);
        so.assertNoMoreEvents();

        rxclient.disconnectForcibly();

        so.assertStatus().hasState(DISCONNECTING);
        so.assertStatus().hasState(DISCONNECTED);
        so.assertNoMoreEvents();
    }

    @Test
    public void testConnecting() {
        rxclient.status().subscribe(so);

        so.assertStatus().hasState(CONNECTED);
        so.assertNoMoreEvents();

        rxclient.disconnect();

        so.assertStatus().hasState(DISCONNECTING);
        so.assertStatus().hasState(DISCONNECTED);
        so.assertNoMoreEvents();

        rxclient.connect();

        so.assertStatus().hasState(CONNECTING);
        so.assertStatus().hasState(CONNECTED);
        so.assertNoMoreEvents();
    }

    @Test
    public void testInitializing() {
        RxMqttClient c = new RxMqttClient(fakeMqttClient, new MqttConnectOptions(), new Gson());
        c.status().subscribe(so);

        so.assertStatus().hasState(READY);
        so.assertNoMoreEvents();

        c.connect();

        so.assertStatus().hasState(CONNECTING);
        so.assertStatus().hasState(CONNECTED);
        so.assertNoMoreEvents();
    }

    @Test
    public void testFailedConnecting() {
        fakeMqttClient.setFailConnecting(true);
        RxMqttClient c = new RxMqttClient(fakeMqttClient, new MqttConnectOptions(), new Gson());
        c.status().subscribe(so);

        so.assertStatus().hasState(READY);
        so.assertNoMoreEvents();

        c.connect();

        so.assertStatus().hasState(CONNECTING);
        so.assertStatus().hasState(CONNECTION_FAILED);
        so.assertNoMoreEvents();
    }

    //endregion test status

    //region test publishing

    @Test
    public void testSinglePublishReceivesMessage() {
        String payload = "payload1";

        rxclient.onTopic(TOPIC_1).subscribe(mo);
        rxclient.publish(TOPIC_1, payload).subscribe(pro);

        mo.assertMessage()
                .hasPayload(payload)
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();
    }

    @Ignore("Something is working with the PublishResponse publish subject")
    @Test
    public void testSinglePublishGetsPublishResponse() {
        String payload = "payload1";

        rxclient.onTopic(TOPIC_1).subscribe(mo);
        rxclient.publish(TOPIC_1, payload).subscribe(pro);

        pro.assertPublishResponse()
                .hasPayload(payload)
                .onTopic(TOPIC_1);
        pro.assertNoMoreEvents();
    }

    @Test
    public void testMultiplePublishes() {
        String payload1 = "payload1";
        String payload2 = "payload2";

        rxclient.onTopic(TOPIC_1).subscribe(mo);

        rxclient.publish(TOPIC_1, payload1).subscribe(pro);
        rxclient.publish(TOPIC_1, payload2).subscribe(pro);

        mo.assertMessage()
                .hasPayload(payload1)
                .onTopic(TOPIC_1);
        mo.assertMessage()
                .hasPayload(payload2)
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();
    }

    @Test
    public void testPublishOnWrongTopicDoesntReceiveMessage() {
        rxclient.onTopic(TOPIC_1).subscribe(mo);

        rxclient.publish(TOPIC_2, "payload1").subscribe(pro);

        mo.assertNoMoreEvents();
    }

    @Test
    public void testPublishDoesntDoesntReceiveMessageAfterUnsubscribing() {
        String payload1 = "payload1";
        String payload2 = "payload2";

        Subscription subscription = rxclient.onTopic(TOPIC_1).subscribe(mo);

        rxclient.publish(TOPIC_1, payload1).subscribe(pro);

        mo.assertMessage()
                .hasPayload(payload1)
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();

        subscription.unsubscribe(); //TODO DO THAT RXBINDINGS TRICK THING TO REMOVE CALLABACKS!

        rxclient.publish(TOPIC_1, payload1).subscribe(pro);

        mo.assertNoMoreEvents();
    }

    @Ignore("check this out in a second")
    @Test
    public void testResubscribing() {
        String payload1 = "payload1";
        String payload2 = "payload2";
        String payload3 = "payload3";

        Subscription subscription = rxclient.onTopic(TOPIC_1).subscribe(mo);

        rxclient.publish(TOPIC_1, payload1).subscribe(pro);

        mo.assertMessage()
                .hasPayload(payload1)
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();

        subscription.unsubscribe();

        rxclient.publish(TOPIC_1, payload1).subscribe(pro);

        mo.assertNoMoreEvents();

        rxclient.onTopic(TOPIC_1).subscribe(mo);

        rxclient.publish(TOPIC_1, payload3).subscribe(pro);
        mo.assertMessage()
                .hasPayload(payload3)
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();
    }

    @Test
    public void testThatMultipleSubscribersEachGetMessages() {
        String payload = "payload";

        MessageRecordingObserver mo1 = new MessageRecordingObserver();
        MessageRecordingObserver mo2 = new MessageRecordingObserver();

        rxclient.onTopic(TOPIC_1).subscribe(mo1);
        rxclient.onTopic(TOPIC_1).subscribe(mo2);

        rxclient.publish(TOPIC_1, payload).subscribe(pro);

        mo1.assertMessage()
                .hasPayload(payload)
                .onTopic(TOPIC_1);
        mo1.assertNoMoreEvents();

        mo2.assertMessage()
                .hasPayload(payload)
                .onTopic(TOPIC_1);
        mo2.assertNoMoreEvents();
    }

    @Test
    public void testPublishingWithObjectSerialization() {
        MyTestClass testObject = new MyTestClass();

        rxclient.onTopic(TOPIC_1).subscribe(mo);

        rxclient.publish(TOPIC_1, testObject).subscribe(pro);

        mo.assertMessage()
                .hasPayload(new Gson().toJson(testObject))
                .onTopic(TOPIC_1);
        mo.assertNoMoreEvents();
    }

    //endregion test publishing

    //region private helper methods

    static class MyTestClass {
        int field1 = 1;
        String field = "test";
    }

    private void publish(String topic, MqttMessage message) {
        try {
            fakeMqttClient.publish(topic, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MqttMessage createMessage(String payload, QoS qos) {
        MqttMessage m = new MqttMessage(payload.getBytes());
        m.setQos(qos.getValue());
        return m;
    }

    //endregion
}
