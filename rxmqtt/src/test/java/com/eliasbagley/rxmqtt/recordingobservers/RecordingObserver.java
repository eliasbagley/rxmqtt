package com.eliasbagley.rxmqtt.recordingobservers;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by eliasbagley on 2/11/16.
 * <p/>
 * A Subscriber subclass which enqueues items into a BlockingDeque for testing
 */
public class RecordingObserver<T> extends Subscriber<T> {
    private static final Object                COMPLETED = "<completed>";
    protected final      BlockingDeque<Object> events    = new LinkedBlockingDeque<>();

    @Override
    public void onCompleted() {
        events.add(COMPLETED);
    }

    @Override
    public void onError(Throwable e) {
        events.add(e);
    }

    @Override
    public void onNext(Object message) {
        events.add(message);
    }

    public void doRequest(long amount) {
        request(amount);
    }

    protected Object takeEvent() {
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
}
