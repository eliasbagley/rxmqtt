package com.eliasbagley.rxmqtt.recordingobservers;

import com.eliasbagley.rxmqtt.Status;
import com.eliasbagley.rxmqtt.constants.State;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by eliasbagley on 2/11/16.
 */
public class StatusRecordingObserver extends Subscriber<Status> {
    private static final Object COMPLETED = "<completed>";
    private static final String TAG       = MessageRecordingObserver.class.getSimpleName();

    private final BlockingDeque<Object> events = new LinkedBlockingDeque<>();

    @Override
    public void onCompleted() {
        events.add(COMPLETED);
    }

    @Override
    public void onError(Throwable e) {
        events.add(e);
    }

    @Override
    public void onNext(Status message) {
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

    public StatusAssert assertStatus() {
        Object event = takeEvent();
        assertThat(event).isInstanceOf(Status.class);
        return new StatusAssert((Status) event);
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

    public static final class StatusAssert {
        private final Status status;

        StatusAssert(Status status) {
            this.status = status;
        }

        public StatusAssert hasState(State state) {
            assertThat(status.getState()).isEqualTo(state);
            return this;
        }
    }
}

