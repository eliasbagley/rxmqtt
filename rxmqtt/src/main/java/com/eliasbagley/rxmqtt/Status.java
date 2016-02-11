package com.eliasbagley.rxmqtt;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.eliasbagley.rxmqtt.constants.State;

import java.sql.Timestamp;

import static com.eliasbagley.rxmqtt.constants.State.*;

/**
 * Represents the MqttClient's connection status
 */

public class Status {
    @NonNull private Timestamp timestamp;
    @NonNull private State     state;

    public Status(@NonNull State state) {
        this(state, new Timestamp(System.currentTimeMillis()));
    }

    public Status(@NonNull State state, @NonNull Timestamp logTime) {
        this.state = state;
        this.timestamp = logTime;
    }

    @NonNull
    @CheckResult
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @NonNull @CheckResult
    public State getState() {
        return state;
    }

    @Override
    @NonNull @CheckResult
    public String toString() {
        return String.format("state: %s, time:%sm", getState(), getTimestamp());
    }

    //region convenience methods

    public boolean isConnecting() {
        return state == CONNECTING;
    }

    public boolean isConnected() {
        return state == CONNECTED;
    }

    public boolean isDisconnected() {
        return state == DISCONNECTED;
    }

    public boolean isDisconnecting() {
        return state == DISCONNECTING;
    }

    public boolean isInitializing() {
        return state == INITIALIZING;
    }

    public boolean isConnectionFailed() {
        return state == CONNECTION_FAILED;
    }

    public boolean isConnectionLost() {
        return state == CONNECTION_LOST;
    }

    //endregion convenience methods
}
