package com.eliasbagley.rxmqtt.assertionmodels;

import com.eliasbagley.rxmqtt.Status;
import com.eliasbagley.rxmqtt.constants.State;

import static com.google.common.truth.Truth.*;

/**
 * Created by eliasbagley on 2/11/16.
 */
public final class StatusAssert {
    private final Status status;

    public StatusAssert(Status status) {
        this.status = status;
    }

    public StatusAssert hasState(State state) {
        assertThat(status.getState()).isEqualTo(state);
        return this;
    }
}

