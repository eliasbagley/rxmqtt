package com.eliasbagley.rxmqtt.recordingobservers;

import com.eliasbagley.rxmqtt.PublishResponse;
import com.eliasbagley.rxmqtt.assertionmodels.PublishResponseAssert;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import rx.Subscriber;

import static com.google.common.truth.Truth.*;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by eliasbagley on 2/11/16.
 */
public class PublishResponseRecordingObserver extends RecordingObserver<PublishResponse> {
    public PublishResponseAssert assertPublishResponse() {
        Object event = takeEvent();
        assertThat(event).isInstanceOf(PublishResponse.class);
        return new PublishResponseAssert((PublishResponse) event);
    }
}

