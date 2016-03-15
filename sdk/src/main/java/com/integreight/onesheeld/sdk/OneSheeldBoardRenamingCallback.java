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
 * Represents various renaming events for {@link OneSheeldDevice}.
 * <p>Most of the methods here gets called in a thread different than the UI thread.
 * So take precautions and use some sort of handlers if you want to interact with the Ui.</p>
 */
public abstract class OneSheeldBoardRenamingCallback {

    /**
     * This method gets called when the device does not respond with a confirmation for the new board name.
     *
     * @param device the device where the event occurred
     */
    public void onRenamingAttemptTimeOut(OneSheeldDevice device) {

    }

    /**
     * This method gets called when all the attempts to rename the board fail.
     *
     * @param device the device where the event occurred
     */
    public void onAllRenamingAttemptsTimeOut(OneSheeldDevice device) {

    }

    /**
     * This method gets called when the device confirms the new board name.
     *
     * @param device the device where the event occurred
     */
    public void onRenamingRequestReceivedSuccessfully(OneSheeldDevice device) {

    }
}
