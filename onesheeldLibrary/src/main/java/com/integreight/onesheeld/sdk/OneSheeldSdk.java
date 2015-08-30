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

import android.content.Context;

/**
 * Represents a head class for the sdk.
 * <p>Used mainly to get {@link OneSheeldManager} and the list of {@link KnownShields}.</p>
 */
public class OneSheeldSdk {
    static final String TAG = "OneSheeldSdk";
    private static final FirmwareVersion compatibleFirmwareVersion = new FirmwareVersion(1, 3);
    private static final int compatibleLibraryVersion = 7;
    private static final int versionCode = BuildConfig.VERSION_CODE;
    private static final String versionName = BuildConfig.VERSION_NAME;
    private static boolean isDebuggingEnabled = false;
    private static Context context;
    private static boolean isInit = false;

    private OneSheeldSdk() {

    }

    /**
     * Checks whether the log debugging messages is enabled or not.
     *
     * @return the boolean
     */
    public static boolean isDebuggingEnabled() {
        return isDebuggingEnabled;
    }

    /**
     * Sets the debugging messages enabled or not.
     * <p>This will print various messages to the log cat console under the tag
     * OneSheeldSdk</p>
     * <p>default value is false</p>
     *
     * @param value the value
     */
    public static void setDebugging(boolean value) {
        isDebuggingEnabled = value;
        if (value) Log.d("Debugging logs enabled.");
    }

    /**
     * Gets the highest compatible firmware version.
     *
     * @return the compatible firmware version
     */
    public static FirmwareVersion getCompatibleFirmwareVersion() {
        return compatibleFirmwareVersion;
    }

    /**
     * Gets the highest compatible library version.
     *
     * @return the compatible library version
     */
    public static int getCompatibleLibraryVersion() {
        return compatibleLibraryVersion;
    }

    /**
     * Gets the current version code of the sdk.
     *
     * @return the version code
     */
    public static int getVersionCode() {
        return versionCode;
    }

    /**
     * Gets the current version name of the sdk.
     *
     * @return the version name
     */
    public static String getVersionName() {
        return versionName;
    }

    /**
     * Initialize the sdk.
     * <p>This should be called before any other sdk methods.</p>
     *
     * @param context a valid context, could be an activity or application context.
     * @throws NullPointerException if the passed context is null.
     */
    public static void init(Context context) {
        if (context == null)
            throw new NullPointerException("The passed context is null, have you checked its validity?");
        OneSheeldSdk.context = context;
        isInit = true;
        Log.d("OneSheeld Android SDK v" + versionName + " is initialized.");
    }

    static Context getContext() {
        return context;
    }

    /**
     * Checks if the sdk is initialized.
     *
     * @return the boolean
     */
    public static boolean isInit() {
        return isInit;
    }

    /**
     * Gets {@link OneSheeldManager} which responsible for scanning and connection.
     *
     * @return the manager
     */
    public static OneSheeldManager getManager() {
        return OneSheeldManager.getInstance();
    }

    /**
     * Gets the list of {@link KnownShields}.
     *
     * @return the known shields
     */
    public static KnownShields getKnownShields() {
        return KnownShields.getInstance();
    }
}
