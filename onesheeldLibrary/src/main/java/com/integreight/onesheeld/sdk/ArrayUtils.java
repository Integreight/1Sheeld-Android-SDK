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
