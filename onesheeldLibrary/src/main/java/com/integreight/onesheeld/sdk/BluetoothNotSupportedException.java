package com.integreight.onesheeld.sdk;

public class BluetoothNotSupportedException extends OneSheeldException {
    public BluetoothNotSupportedException(String msg) {
        super(msg);
    }

    public BluetoothNotSupportedException(String msg, Exception e) {
        super(msg,e);
    }
}
