package com.integreight.onesheeld.sdk;

public class NullOneSheeldDeviceException extends OneSheeldException {
    NullOneSheeldDeviceException(String msg) {
        super(msg);
    }

    NullOneSheeldDeviceException(String msg, Exception e) {
        super(msg, e);
    }
}
