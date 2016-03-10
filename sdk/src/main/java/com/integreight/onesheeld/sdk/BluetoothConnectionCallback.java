package com.integreight.onesheeld.sdk;

/**
 * Created by iSsO on 3/2/16.
 */
interface BluetoothConnectionCallback {
    void onConnectionSuccess();

    void onConnectionFailure();

    void onConnectionInterrupt();
}
