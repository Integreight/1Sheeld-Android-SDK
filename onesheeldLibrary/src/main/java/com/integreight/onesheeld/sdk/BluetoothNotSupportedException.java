package com.integreight.onesheeld.sdk;

/**
 * This exception is thrown if any Bluetooth related operations is requested
 * (ex: scanning, connecting,..etc) and the device doesn't support Bluetooth.
 */
public class BluetoothNotSupportedException extends OneSheeldException {
    BluetoothNotSupportedException(String msg) {
        super(msg);
    }

    BluetoothNotSupportedException(String msg, Exception e) {
        super(msg, e);
    }
}
