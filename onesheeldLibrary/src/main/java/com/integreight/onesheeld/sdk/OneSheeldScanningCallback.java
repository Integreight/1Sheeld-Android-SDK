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

import java.util.List;

/**
 * Represents various scanning events for {@link OneSheeldManager}.
 * <p>Most of the methods here gets called in a thread different than the UI thread.
 * So take precautions and use some sort of handlers if you want to interact with the Ui.</p>
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
