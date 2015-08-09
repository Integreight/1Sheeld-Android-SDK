package com.integreight.onesheeld.sdk;

/**
 * Created by dell on 6/21/2015.
 */
public class OneSheeldException extends RuntimeException {

    public OneSheeldException(String msg) {
        super(msg);
    }

    public OneSheeldException(String msg, Exception e) {
        super(msg, e);
    }
}
