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

abstract class Log {
    static void d(String tag, String msg) {
        if (OneSheeldSdk.isDebuggingEnabled() && tag != null && tag.trim().length() > 0 && msg != null && msg.trim().length() > 0)
            android.util.Log.d(tag, msg);
    }

    static void d(String msg) {
        if (OneSheeldSdk.isDebuggingEnabled() && msg != null && msg.trim().length() > 0)
            android.util.Log.d(OneSheeldSdk.TAG, msg);
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
