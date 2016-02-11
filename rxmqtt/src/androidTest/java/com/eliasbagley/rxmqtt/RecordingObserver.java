/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eliasbagley.rxmqtt;

import android.util.Log;

import com.eliasbagley.rxmqtt.constants.QoS;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

final class RecordingObserver extends Subscriber<Message> {
  private static final Object COMPLETED = "<completed>";
  private static final String TAG = RecordingObserver.class.getSimpleName();

  private final BlockingDeque<Object> events = new LinkedBlockingDeque<>();

  @Override public void onCompleted() {
    Log.d(TAG, "onCompleted");
    events.add(COMPLETED);
  }

  @Override public void onError(Throwable e) {
    Log.d(TAG, "onError " + e.getClass().getSimpleName() + " " + e.getMessage());
    events.add(e);
  }

  @Override public void onNext(Message message) {
    Log.d(TAG, "onNext " + message);
    events.add(message);
  }

  public void doRequest(long amount) {
    request(amount);
  }

  private Object takeEvent() {
    try {
      Object item = events.pollFirst(1, SECONDS);
      if (item == null) {
        throw new AssertionError("Timeout expired waiting for item.");
      }
      return item;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public MessageAssert assertMessage() {
    Object event = takeEvent();
    assertThat(event).isInstanceOf(Message.class);
    return new MessageAssert((Message) event);
  }

  public void assertErrorContains(String expected) {
    Object event = takeEvent();
    assertThat(event).isInstanceOf(Throwable.class);
    assertThat(((Throwable) event).getMessage()).contains(expected);
  }

  public void assertNoMoreEvents() {
    try {
      assertThat(events.pollFirst(1, SECONDS)).isNull();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  static final class MessageAssert {
    private final Message message;

    MessageAssert(Message message) {
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
}
