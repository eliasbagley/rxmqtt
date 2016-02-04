package com.eliasbagley.rxmqtt.constants;

/**
 * Quality of Service (QoS) delivery guarantees
 *
 * 0 - best effort, will be delivered at most once
 * 1 - guaranteed delivery at least once, possible duplicates
 * 2 - guaranteed delivery exactly once
 */
public enum QoS {
    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    int value;
    QoS(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
