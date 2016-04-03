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
 * Represents various firmware update events for {@link OneSheeldDevice}.
 * <p>Most of the methods here gets called in a thread different than the UI thread.
 * So take precautions and use some sort of handlers if you want to interact with the Ui.</p>
 */
public abstract class OneSheeldFirmwareUpdateCallback {
    /**
     * This method gets called when the firmware updating process starts.
     *
     * @param device the device where the event occurred
     */
    public void onStart(OneSheeldDevice device) {

    }

    /**
     * This method gets called when the a chunk of the firmware is sent successfully.
     *
     * @param device     the device where the event occurred
     * @param totalBytes the total size of the firmware
     * @param sentBytes  how many bytes sent successfully
     */
    public void onProgress(OneSheeldDevice device, int totalBytes, int sentBytes) {

    }

    /**
     * This method gets called when the firmware updating process complete successfully.
     *
     * @param device the device where the event occurred
     */
    public void onSuccess(OneSheeldDevice device) {

    }

    /**
     * This method gets called when the firmware updating fail with an error.
     *
     * @param device    the device where the event occurred
     * @param isTimeOut a boolean indicating if that error is a timeout error or a not
     */
    public void onFailure(OneSheeldDevice device, boolean isTimeOut) {

    }
}
