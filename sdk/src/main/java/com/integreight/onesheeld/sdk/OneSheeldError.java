/*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License version 3 only, as
* published by the Free Software Foundation.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
* version 3 for more details (a copy is included in the LICENSE file that
* accompanied this code).
*
* Please contact Integreight, Inc. at info@integreight.com or post on our
* support forums www.1sheeld.com/forum if you need additional information
* or have any questions.
*/

package com.integreight.onesheeld.sdk;

/**
 * Represents various possible errors that could happen during Bluetooth
 * scanning, connection, or data transmission in {@link OneSheeldManager}
 * or {@link OneSheeldDevice}.
 *
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
