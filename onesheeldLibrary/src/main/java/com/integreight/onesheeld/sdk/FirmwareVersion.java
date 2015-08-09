package com.integreight.onesheeld.sdk;

public class FirmwareVersion {
    private int majorVersion;
    private int minorVersion;

    public FirmwareVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

}
