package com.integreight.onesheeld.sdk;

import java.util.List;

public abstract class OneSheeldScanningCallback {
    public void onScanStart() {

    }

    public void onDeviceFind(OneSheeldDevice device) {

    }

    public void onScanFinish(List<OneSheeldDevice> foundDevices) {

    }
}
