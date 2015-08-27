package com.integreight.onesheeld.sdk;

public class OneSheeldException extends RuntimeException {

    public OneSheeldException(String msg) {
        super(msg);
    }

    public OneSheeldException(String msg, Exception e) {
        super(msg, e);
    }
}
