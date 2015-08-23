package com.integreight.onesheeld.sdk;

public abstract class OneSheeldConnectionCallback {
    public void onConnect(OneSheeldDevice device) {

    }

    public void onDisconnect(OneSheeldDevice device) {

    }

    public void onConnectionRetry(OneSheeldDevice device, int retryCount) {

    }
}
