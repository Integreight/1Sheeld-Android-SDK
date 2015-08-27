package com.integreight.onesheeld.sdk;

public class IncorrectPinException extends OneSheeldException {
    public IncorrectPinException(String msg) {
        super(msg);
    }

    public IncorrectPinException(String msg, Exception e) {
        super(msg,e);
    }
}
