package com.integreight.onesheeld.sdk;

public class BluetoothNotSupportedException extends OneSheeldException {
    BluetoothNotSupportedException(String msg) {
        super(msg);
    }

    BluetoothNotSupportedException(String msg, Exception e) {
        super(msg, e);
    }
}
