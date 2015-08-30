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
 * Represents various connection events.
 * <p>Should be extended and passed to either {@link OneSheeldManager} or
 * {@link OneSheeldDevice} to get notified about connection and disconnection
 * of Bluetooth devices.</p>
 *
 * @see OneSheeldDevice
 * @see OneSheeldManager
 */
public abstract class OneSheeldConnectionCallback {
    /**
     * This method gets called once a successful connection is made to a
     * 1Sheeld board.
     * <p>It gets called after the initialization and querying necessary
     * information of the board.</p>
     *
     * @param device the connected device.
     * @see OneSheeldDevice
     * @see OneSheeldManager
     */
    public void onConnect(OneSheeldDevice device) {

    }

    /**
     * This method gets called once a disconnection occurs.
     * <p>Either through a Bluetooth reset request from the board, power cut,
     * or a disconnection request.</p>
     *
     * @param device the device
     * @see OneSheeldDevice
     * @see OneSheeldManager
     */
    public void onDisconnect(OneSheeldDevice device) {

    }

    /**
     * This method gets called if {@link OneSheeldManager} retries to connect
     * to the device again.
     * <p>This happen after an unsuccessful Bluetooth connection attempt if
     * the requested retries number has not been reached.</p>
     * <p>This method won't be called for the automatic retries if configured in
     * {@link OneSheeldManager}.</p>
     *
     * @param device     the device
     * @param retryCount the retry count
     * @see OneSheeldDevice
     * @see OneSheeldManager
     */
    public void onConnectionRetry(OneSheeldDevice device, int retryCount) {

    }
}
