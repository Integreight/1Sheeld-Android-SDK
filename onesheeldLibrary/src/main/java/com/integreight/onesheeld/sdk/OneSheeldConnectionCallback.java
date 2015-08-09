package com.integreight.onesheeld.sdk;

/**
 * Created by dell on 6/18/2015.
 */
public abstract class OneSheeldConnectionCallback {
    public void onConnect(OneSheeldDevice device) {

    }

    public void onDisconnect(OneSheeldDevice device) {

    }

    public void onConnectionRetry(OneSheeldDevice device, int retryCount) {

    }
}
