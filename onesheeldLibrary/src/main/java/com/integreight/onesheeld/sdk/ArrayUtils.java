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

abstract class ArrayUtils {
    private ArrayUtils() {

    }

    static byte[] copyOfRange(byte[] from, int start, int end) {
        int length = end - start;
        byte[] result = new byte[length];
        System.arraycopy(from, start, result, 0, length);
        return result;
    }

    static String toHexString(byte[] array) {
        if (array == null) return null;
        String s = "";
        for (byte b : array) {
            if ((Integer.toHexString(b).length() < 2))
                s += "0" + Integer.toHexString(b) + " ";
            else if ((Integer.toHexString(b).length() == 2))
                s += Integer.toHexString(b) + " ";
            else {
                String temp = Integer.toHexString(b);
                temp = temp.substring(temp.length() - 2);
                s += temp + " ";
            }
        }
        return s;
    }
}
