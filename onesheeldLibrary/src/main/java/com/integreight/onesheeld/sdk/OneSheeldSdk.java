package com.integreight.onesheeld.sdk;

import android.content.Context;

public class OneSheeldSdk {
    public static final String TAG = "OneSheeldSdk";
    private static final FirmwareVersion COMPATIBLE_FIRMWARE_VERSION = new FirmwareVersion(1, 3);
    private static final int versionCode = BuildConfig.VERSION_CODE;
    private static final String versionName = BuildConfig.VERSION_NAME;
    private static boolean isDebuggingEnabled = false;
    private static Context context;
    private static boolean isInit = false;

    public static boolean isDebuggingEnabled() {
        return isDebuggingEnabled;
    }

    public static void setDebugging(boolean value) {
        isDebuggingEnabled = value;
        if (value) Log.d("Debugging logs enabled.");
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
        Log.d("OneSheeld SDK initialized.");
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
