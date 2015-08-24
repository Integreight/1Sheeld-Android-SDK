package com.integreight.onesheeld.sdk.exceptions;

public class SdkNotInitializedException extends OneSheeldException {
    public SdkNotInitializedException(String msg) {
        super(msg);
    }

    public SdkNotInitializedException(String msg, Exception e) {
        super(msg,e);
    }
}
