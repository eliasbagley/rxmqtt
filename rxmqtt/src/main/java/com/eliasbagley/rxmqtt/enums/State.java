package com.eliasbagley.rxmqtt.enums;

public enum State {
    INIT(0),
    CONNECTING(1),
    CONNECTION_FAILED(2),
    CONNECTED(3),
    CONNECTION_LOST(4),
    DISCONNECTING(5),
    DISCONNECTED(6);

    private int code;

    State(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
