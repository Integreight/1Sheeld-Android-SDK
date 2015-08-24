package com.integreight.onesheeld.sdk;

public abstract class OneSheeldDataCallback {
    public void onDigitalPinStatusChange(int pinNumber, boolean newValue) {

    }

    public void onSerialDataReceive(int data) {

    }

    public void onShieldFrameReceive(ShieldFrame frame) {

    }

    public void onKnownShieldFrameReceive(KnownShield shields, ShieldFrame frame) {

    }
}
