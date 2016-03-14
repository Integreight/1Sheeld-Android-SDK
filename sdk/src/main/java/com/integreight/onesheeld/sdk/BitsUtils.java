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

abstract class BitsUtils {

    static byte setBit(byte b, int bit) {
        if (bit < 0 || bit >= 8) return b;
        return (byte) (b | (1 << bit));
    }

    static byte resetBit(byte b, int bit) {
        if (bit < 0 || bit >= 8) return b;
        return (byte) (b & (~(1 << bit)));
    }

    static boolean isBitSet(byte b, int bit) {
        return !(bit < 0 || bit >= 8) && (b & (1 << bit)) > 0;
    }

    static boolean isBitSet(int b, int bit) {
        return !(bit < 0 || bit >= 32) && (b & (1 << bit)) > 0;
    }
}

