package com.eliasbagley.rxmqtt.impl;

import com.eliasbagley.rxmqtt.enums.RxMqttClientState;

import java.sql.Timestamp;

public class RxMqttClientStatus implements Cloneable {
    private Timestamp         logTime;
    private RxMqttClientState state;

    public RxMqttClientStatus() {
        this.state = RxMqttClientState.Init;
    }

    public Timestamp getLogTime() {
        return logTime;
    }

    public void setLogTime(Timestamp logTime) {
        this.logTime = logTime;
    }

    public RxMqttClientState getState() {
        return state;
    }

    public void setState(RxMqttClientState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("time:%sm state:%s", getLogTime(), getState());
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
