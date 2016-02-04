package com.eliasbagley.rxmqtt.utils;

/**
 * Created by eliasbagley on 2/4/16.
 */
public class Strings {
    private Strings() {
        // No instances.
    }

    public static boolean isBlank(CharSequence string){
        return (string == null || string.toString().trim().length() == 0);
    }
}
