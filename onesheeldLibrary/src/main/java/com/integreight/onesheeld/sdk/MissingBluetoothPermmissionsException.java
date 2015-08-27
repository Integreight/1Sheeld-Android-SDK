package com.integreight.onesheeld.sdk;

public class MissingBluetoothPermmissionsException extends OneSheeldException {
    MissingBluetoothPermmissionsException(String msg) {
        super(msg);
    }

    MissingBluetoothPermmissionsException(String msg, Exception e) {
        super(msg, e);
    }
}
