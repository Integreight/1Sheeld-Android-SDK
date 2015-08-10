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

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof FirmwareVersion)
            return this.majorVersion == ((FirmwareVersion) o).majorVersion && this.minorVersion == ((FirmwareVersion) o).minorVersion;
        else return false;
    }
}
