package com.integreight.onesheeld.sdk;

public class InvalidBluetoothAddressException extends OneSheeldException {
    public InvalidBluetoothAddressException(String msg) {
        super(msg);
    }

    public InvalidBluetoothAddressException(String msg, Exception e) {
        super(msg,e);
    }
}
