package com.eliasbagley.rxmqtt.enums;

public enum RxMqttClientState {
    Init(0),
    Connecting(1),
    ConnectingFailed(2),
    Connected(3),
    ConnectionLost(4),
    TryDisconnect(5),
    Disconnected(6);

    private int code;

    RxMqttClientState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
