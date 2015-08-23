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
        if (bit < 0 || bit >= 8) return false;
        return (b & (1 << bit)) > 0;
    }

    static boolean isBitSet(int b, int bit) {
        if (bit < 0 || bit >= 32) return false;
        return (b & (1 << bit)) > 0;
    }
}
