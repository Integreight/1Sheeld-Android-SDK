package com.integreight.onesheeld.sdk;

import java.util.List;

public abstract class OneSheeldScanningCallback {
    public void onStartScan() {

    }

    public void onDeviceFind(OneSheeldDevice device) {

    }

    public void onFinishScan(List<OneSheeldDevice> foundDevices) {

    }
}
