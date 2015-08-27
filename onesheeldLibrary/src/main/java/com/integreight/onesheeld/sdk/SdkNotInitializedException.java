package com.integreight.onesheeld.sdk;

public class SdkNotInitializedException extends OneSheeldException {
    SdkNotInitializedException(String msg) {
        super(msg);
    }

    SdkNotInitializedException(String msg, Exception e) {
        super(msg, e);
    }
}
