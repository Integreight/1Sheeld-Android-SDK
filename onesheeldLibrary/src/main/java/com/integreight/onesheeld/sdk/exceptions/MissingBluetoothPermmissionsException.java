package com.integreight.onesheeld.sdk.exceptions;

public class MissingBluetoothPermmissionsException extends OneSheeldException {
    public MissingBluetoothPermmissionsException(String msg) {
        super(msg);
    }

    public MissingBluetoothPermmissionsException(String msg, Exception e) {
        super(msg,e);
    }
}
