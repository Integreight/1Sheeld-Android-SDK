package com.integreight.onesheeld.sdk;

/**
 * Represents a valid firmware version for 1Sheeld device.
 */
public class FirmwareVersion {
    private int majorVersion;
    private int minorVersion;

    FirmwareVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    /**
     * Gets the major version component of the firmware version.
     *
     * @return the major version
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Gets the minor version component of the firmware version.
     *
     * @return the minor version
     */
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
