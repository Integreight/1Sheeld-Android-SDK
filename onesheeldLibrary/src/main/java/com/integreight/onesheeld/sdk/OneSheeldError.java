package com.integreight.onesheeld.sdk;

/**
 * Represents various possible errors that could happen during Bluetooth
 * scanning, connection, or data transmission in {@link OneSheeldManager}
 * or {@link OneSheeldDevice}.
 * @see OneSheeldManager
 * @see OneSheeldDevice
 */
public enum OneSheeldError {
    /**
     * Happens if Bluetooth scanning or connection is requested while the
     * Bluetooth is not enabled.
     */
    BLUETOOTH_NOT_ENABLED,
    /**
     * Happens if any {@link OneSheeldDevice} operation is requested while
     * the device is not connected.
     */
    DEVICE_NOT_CONNECTED,
    /**
     * Happens if Bluetooth scanning or connection is requested while
     * another scanning is in progress.
     */
    SCANNING_IN_PROGRESS,
    /**
     * Happens if a new connection is requested when the maximum connected
     * devices reached 7.
     */
    MAXIMUM_BLUETOOTH_CONNECTIONS_REACHED,
    /**
     * Happens if Bluetooth scanning or connection is requested while
     * another pending connection is in progress.
     */
    PENDING_CONNECTION_IN_PROGRESS,
    /**
     * Happens if a new connection is requested to an already connected devices
     */
    ALREADY_CONNECTED_TO_DEVICE,
    /**
     * Happens if the connection to {@link OneSheeldDevice} failed after all
     * attempts.
     */
    BLUETOOTH_CONNECTION_FAILED
}
