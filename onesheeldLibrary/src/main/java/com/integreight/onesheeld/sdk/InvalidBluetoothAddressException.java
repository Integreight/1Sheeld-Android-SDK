package com.integreight.onesheeld.sdk;

public class InvalidBluetoothAddressException extends OneSheeldException {
    InvalidBluetoothAddressException(String msg) {
        super(msg);
    }

    InvalidBluetoothAddressException(String msg, Exception e) {
        super(msg, e);
    }
}
