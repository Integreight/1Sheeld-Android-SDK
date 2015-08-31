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
