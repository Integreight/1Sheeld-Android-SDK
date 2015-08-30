package com.integreight.onesheeld.sdk;

/**
 * This exception is thrown if the sdk is not init.
 */
public class SdkNotInitializedException extends OneSheeldException {
    SdkNotInitializedException(String msg) {
        super(msg);
    }

    SdkNotInitializedException(String msg, Exception e) {
        super(msg, e);
    }
}
