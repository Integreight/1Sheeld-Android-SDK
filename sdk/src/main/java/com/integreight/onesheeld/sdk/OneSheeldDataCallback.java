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
 * Represents various data events for {@link OneSheeldDevice}.
 * <p>Should be extended and passed to {@link OneSheeldDevice} to get notified
 * about pins status change, raw data receive or shield frame receive.</p>
 * <p>Most of the methods here gets called in a thread different than the UI thread.
 * So take precautions and use some sort of handlers if you want to interact with the Ui.</p>
 *
 * @see OneSheeldDevice
 */
public abstract class OneSheeldDataCallback {
    /**
     * This method gets called once an input pin in 1Sheeld board changes its
     * value.
     *
     * @param device    the device where the event occurred
     * @param pinNumber the pin number
     * @param newValue  the new value
     */
    public void onDigitalPinStatusChange(OneSheeldDevice device, int pinNumber, boolean newValue) {

    }

    /**
     * This method gets called for every byte received on the serial pins of
     * 1Sheeld board.
     *
     * @param device the device where the event occurred
     * @param data   the received byte
     */
    public void onSerialDataReceive(OneSheeldDevice device, int data) {

    }

    /**
     * This method gets called for every {@link ShieldFrame} received on the
     * serial pins of 1Sheeld board.
     *
     * @param device the device where the event occurred
     * @param frame  the frame
     * @see ShieldFrame
     */
    public void onShieldFrameReceive(OneSheeldDevice device, ShieldFrame frame) {

    }

    /**
     * This method gets called for every {@link ShieldFrame} of a
     * {@link KnownShield} received on the serial pins of 1Sheeld board.
     * <p>For a <tt>ShieldFrame</tt> to be known, its shield id and function id
     * should be present in one of {@link KnownShield}s in {@link KnownShields}
     * list. </p>
     *
     * @param device      the device where the event occurred
     * @param knownShield the known shield
     * @param frame       the frame
     * @see ShieldFrame
     * @see KnownShield
     * @see KnownShields
     */
    public void onKnownShieldFrameReceive(OneSheeldDevice device, KnownShield knownShield, ShieldFrame frame) {

    }
}
