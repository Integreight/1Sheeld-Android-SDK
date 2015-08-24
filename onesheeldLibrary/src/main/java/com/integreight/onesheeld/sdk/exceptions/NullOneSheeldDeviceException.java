package com.integreight.onesheeld.sdk.exceptions;

public class NullOneSheeldDeviceException extends OneSheeldException {
    public NullOneSheeldDeviceException(String msg) {
        super(msg);
    }

    public NullOneSheeldDeviceException(String msg, Exception e) {
        super(msg,e);
    }
}
