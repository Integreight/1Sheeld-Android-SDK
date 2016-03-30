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
 * Represents various testing events for {@link OneSheeldDevice}.
 * <p>Most of the methods here gets called in a thread different than the UI thread.
 * So take precautions and use some sort of handlers if you want to interact with the Ui.</p>
 */
public abstract class OneSheeldTestingCallback {
    /**
     * This method gets called when the device responds with an answer to the firmware challenge.
     *
     * @param device   the device where the event occurred
     * @param isPassed represents the status of the test
     */
    public void onFirmwareTestResult(OneSheeldDevice device, boolean isPassed) {

    }

    /**
     * This method gets called when the device responds with an answer to the library challenge.
     *
     * @param device   the device where the event occurred
     * @param isPassed represents the status of the test
     */
    public void onLibraryTestResult(OneSheeldDevice device, boolean isPassed) {

    }

    /**
     * This method gets called when the device does not respond with an answer to the firmware challenge.
     *
     * @param device the device where the event occurred
     */
    public void onFirmwareTestTimeOut(OneSheeldDevice device) {

    }

    /**
     * This method gets called when the device does not respond with an answer to the library challenge.
     *
     * @param device the device where the event occurred
     */
    public void onLibraryTestTimeOut(OneSheeldDevice device) {

    }
}
