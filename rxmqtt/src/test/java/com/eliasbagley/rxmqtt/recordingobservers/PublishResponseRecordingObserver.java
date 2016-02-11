package com.eliasbagley.rxmqtt.recordingobservers;

import com.eliasbagley.rxmqtt.PublishResponse;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by eliasbagley on 2/11/16.
 */
public class PublishResponseRecordingObserver extends Subscriber<PublishResponse> {
    private static final Object COMPLETED = "<completed>";
    private static final String TAG       = MessageRecordingObserver.class.getSimpleName();

    private final BlockingDeque<Object> events = new LinkedBlockingDeque<>();

    @Override
    public void onCompleted() {
        System.out.println("in recording observer oncomplete");
        events.add(COMPLETED);
    }

    @Override
    public void onError(Throwable e) {
        System.out.println("in recording observer onerror");
        events.add(e);
    }

    @Override
    public void onNext(PublishResponse message) {
        System.out.println("in recording observer onnext");
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

    public PublishResponseAssert assertPublishResponse() {
        Object event = takeEvent();
        assertThat(event).isInstanceOf(PublishResponse.class);
        return new PublishResponseAssert((PublishResponse) event);
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

    public static final class PublishResponseAssert {
        private final PublishResponse publishResponse;

        PublishResponseAssert(PublishResponse publishResponse) {
            this.publishResponse = publishResponse;
        }

        public PublishResponseAssert onTopic(String topic) {
            assertThat(topic).isEqualTo(publishResponse.getTopic());
            return this;
        }

        public PublishResponseAssert hasPayload(String payload) {
            assertThat(payload).isEqualTo(publishResponse.getMessage());
            return this;
        }

    }
}

