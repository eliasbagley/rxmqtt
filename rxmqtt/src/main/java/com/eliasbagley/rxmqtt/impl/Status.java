package com.eliasbagley.rxmqtt.impl;

import com.eliasbagley.rxmqtt.enums.State;

import java.sql.Timestamp;

import static com.eliasbagley.rxmqtt.enums.State.*;

public class Status {
    private Timestamp logTime;
    private State     state;

    public Status(State state) {
        this(state, new Timestamp(System.currentTimeMillis()));
    }

    public Status(State state, Timestamp logTime) {
        this.state = state;
        this.logTime = logTime;
    }

    public Timestamp getLogTime() {
        return logTime;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format("time:%sm state:%s", getLogTime(), getState());
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

    //endregion
}
