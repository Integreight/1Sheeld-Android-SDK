package com.integreight.onesheeld.sdk;

/**
 * Represents a generic OneSheeldException.
 */
public class OneSheeldException extends RuntimeException {
    OneSheeldException(String msg) {
        super(msg);
    }

    OneSheeldException(String msg, Exception e) {
        super(msg, e);
    }
}
