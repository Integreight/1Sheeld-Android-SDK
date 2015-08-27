package com.integreight.onesheeld.sdk.exceptions;

public class OneSheeldException extends RuntimeException {

    public OneSheeldException(String msg) {
        super(msg);
    }

    public OneSheeldException(String msg, Exception e) {
        super(msg, e);
    }
}