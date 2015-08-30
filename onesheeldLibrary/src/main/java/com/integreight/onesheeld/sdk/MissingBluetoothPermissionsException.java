package com.integreight.onesheeld.sdk;

/**
 * This exception is thrown if the Bluetooth permissions has been omitted from
 * <tt>the AndroidManifest.xml</tt>
 */
public class MissingBluetoothPermissionsException extends OneSheeldException {
    MissingBluetoothPermissionsException(String msg) {
        super(msg);
    }

    MissingBluetoothPermissionsException(String msg, Exception e) {
        super(msg, e);
    }
}
