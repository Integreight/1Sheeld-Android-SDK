package com.integreight.onesheeld.sdk;

public class IncorrectPinException extends OneSheeldException {
    IncorrectPinException(String msg) {
        super(msg);
    }

    IncorrectPinException(String msg, Exception e) {
        super(msg, e);
    }
}
