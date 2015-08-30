package com.integreight.onesheeld.sdk;

import java.util.List;

/**
 * Represents various scanning events for {@link OneSheeldManager}.
 */
public abstract class OneSheeldScanningCallback {
    /**
     * This method gets called when the Bluetooth scanning starts.
     */
    public void onScanStart() {

    }

    /**
     * This method gets called for each found Bluetooth device that
     * either has 1Sheeld in its name or its name could not be retrieved.
     *
     * @param device the device
     */
    public void onDeviceFind(OneSheeldDevice device) {

    }

    /**
     * This method gets called after finishing scanning or a scanning abort
     * request.
     *
     * @param foundDevices an unmodifiable list of all connected devices.
     */
    public void onScanFinish(List<OneSheeldDevice> foundDevices) {

    }
}
