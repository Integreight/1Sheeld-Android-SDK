package com.integreight.onesheeld.sdk;

/**
 * Represents various version query events for {@link OneSheeldDevice}.
 */
public abstract class OneSheeldVersionQueryCallback {
    /**
     * This method gets called when the device respond with the firmware version.
     *
     * @param firmwareVersion the firmware version
     */
    public void onFirmwareVersionQueryResponse(FirmwareVersion firmwareVersion) {

    }

    /**
     * This method gets called when the device respond with the library version.
     *
     * @param libraryVersion the version
     */
    public void onLibraryVersionQueryResponse(int libraryVersion) {

    }
}
