package com.integreight.onesheeld.sdk;

import android.content.Context;

/**
 * Created by dell on 6/18/2015.
 */
public class OneSheeldSdk {
    private static boolean IS_DEBUGGING_ENABLED = false;
    private static FirmwareVersion COMPATIBLE_FIRMWARE_VERSION = new FirmwareVersion(1, 3);
    private static int versionCode = 111111;
    private static String versionName = "1.0.0";
    private static Context context;
    private static boolean isInit = false;

    public static boolean isDebuggingEnabled() {
        return IS_DEBUGGING_ENABLED;
    }

    public static void setDebugging(boolean value) {
        IS_DEBUGGING_ENABLED = value;
    }

    public static FirmwareVersion getCompatibleFirmwareVersion() {
        return COMPATIBLE_FIRMWARE_VERSION;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static void init(Context context) {
        OneSheeldSdk.context = context;
        isInit = true;
    }

    public static Context getContext() {
        return context;
    }

    public static boolean isInit() {
        return isInit;
    }

    public static OneSheeldManager getManager() {
        return OneSheeldManager.getInstance();
    }

    public static KnownShields getKnownShields() {
        return KnownShields.getInstance();
    }
}
