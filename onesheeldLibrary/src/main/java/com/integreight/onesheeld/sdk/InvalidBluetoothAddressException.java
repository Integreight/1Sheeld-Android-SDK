package com.integreight.onesheeld.sdk;

/**
 * This exception is thrown if the specified device Bluetooth address is invalid.
 * The correct address should be in this format (01:23:45:67:89:ab)
 */
public class InvalidBluetoothAddressException extends OneSheeldException {
    InvalidBluetoothAddressException(String msg) {
        super(msg);
    }

    InvalidBluetoothAddressException(String msg, Exception e) {
        super(msg, e);
    }
}
