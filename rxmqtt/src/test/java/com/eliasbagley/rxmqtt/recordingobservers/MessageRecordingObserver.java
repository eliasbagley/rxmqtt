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
package com.eliasbagley.rxmqtt.recordingobservers;

import com.eliasbagley.rxmqtt.Message;
import com.eliasbagley.rxmqtt.assertionmodels.MessageAssert;
import com.eliasbagley.rxmqtt.constants.QoS;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class MessageRecordingObserver extends RecordingObserver<Message> {
    public MessageAssert assertMessage() {
        Object event = takeEvent();
        assertThat(event).isInstanceOf(Message.class);
        return new MessageAssert((Message) event);
    }
}
