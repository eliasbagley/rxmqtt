package com.eliasbagley.rxmqtt.assertionmodels;

import com.eliasbagley.rxmqtt.PublishResponse;

import static com.google.common.truth.Truth.*;

/**
 * Created by eliasbagley on 2/11/16.
 */
public final class PublishResponseAssert {
    private final PublishResponse publishResponse;

    public PublishResponseAssert(PublishResponse publishResponse) {
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

