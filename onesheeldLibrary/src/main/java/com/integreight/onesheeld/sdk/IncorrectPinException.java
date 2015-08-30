package com.integreight.onesheeld.sdk;

/**
 * This exception is thrown if the user requested an IO operation on a
 * nonexistent 1Sheeld pin.
 */
public class IncorrectPinException extends OneSheeldException {
    IncorrectPinException(String msg) {
        super(msg);
    }

    IncorrectPinException(String msg, Exception e) {
        super(msg, e);
    }
}
