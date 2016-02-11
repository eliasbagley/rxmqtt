package com.eliasbagley.rxmqtt.constants;

//TODO add initialized state?
public enum State {
    INITIALIZING,
    READY,
    CONNECTING,
    CONNECTION_FAILED,
    CONNECTED,
    CONNECTION_LOST,
    DISCONNECTING,
    DISCONNECTED;
}

