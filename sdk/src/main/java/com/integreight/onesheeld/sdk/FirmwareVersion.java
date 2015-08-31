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
