package com.integreight.onesheeld.sdk;

abstract class Log {
    static void d(String tag, String msg) {
        if (OneSheeldSdk.isDebuggingEnabled() && tag != null && tag.trim().length() > 0 && msg != null && msg.trim().length() > 0)
            android.util.Log.d(tag, msg);
    }

    static void e(String tag, String msg, Throwable tr) {
        if (OneSheeldSdk.isDebuggingEnabled() && tag != null && tag.trim().length() > 0 && msg != null && msg.trim().length() > 0) {
            if (tr != null) tr.printStackTrace();
            android.util.Log.e(tag, msg);
        }
    }

    static void e(String tag, String msg) {
        if (OneSheeldSdk.isDebuggingEnabled() && tag != null && tag.trim().length() > 0 && msg != null && msg.trim().length() > 0)
            android.util.Log.e(tag, msg);
    }

    static void sysOut(String msg) {
        if (OneSheeldSdk.isDebuggingEnabled() && msg != null && msg.trim().length() > 0)
            System.out.println(msg);
    }
}
