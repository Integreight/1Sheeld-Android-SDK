package com.integreight.onesheeld.sdk;

/**
 * Represents various states for {@link OneSheeldManager}.
 * @see OneSheeldManager
 */
public enum ConnectionState {
    /**
     * Implies that {@link OneSheeldManager} there is a pending connection to a
     * Bluetooth device.
     */
    CONNECTING,
    /**
     * Implies that {@link OneSheeldManager} is scanning for nearby Bluetooth devices.
     */
    SCANNING,
    /**
     * Implies that {@link OneSheeldManager} is Bluetooth operations.
     */
    READY
}
