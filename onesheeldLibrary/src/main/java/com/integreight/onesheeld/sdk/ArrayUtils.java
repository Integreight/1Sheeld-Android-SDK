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
}
