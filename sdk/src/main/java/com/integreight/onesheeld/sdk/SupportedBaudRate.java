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
 * Represents a supported communications baud rate for a {@link OneSheeldDevice}.
 *
 * @see OneSheeldDevice
 */
public enum SupportedBaudRate {
    _9600(9600, 0x01),
    _14400(14400, 0x02),
    _19200(19200, 0x03),
    _28800(28800, 0x04),
    _38400(38400, 0x05),
    _57600(57600, 0x06),
    _115200(115200, 0x07);

    private int baudRate;
    private int frameValue;

    SupportedBaudRate(int baudRate, int frameValue) {
        this.baudRate = baudRate;
        this.frameValue = frameValue;
    }

    /**
     * Gets the baud rate.
     *
     * @return the baud rate
     */
    public int getBaudRate() {
        return baudRate;
    }

    byte getFrameValue() {
        return (byte) frameValue;
    }

}
