package com.eliasbagley.rxmqtt.recordingobservers;

import com.eliasbagley.rxmqtt.Status;
import com.eliasbagley.rxmqtt.assertionmodels.StatusAssert;
import com.eliasbagley.rxmqtt.constants.State;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by eliasbagley on 2/11/16.
 */
public class StatusRecordingObserver extends RecordingObserver<Status> {
    public StatusAssert assertStatus() {
        Object event = takeEvent();
        assertThat(event).isInstanceOf(Status.class);
        return new StatusAssert((Status) event);
    }
}

