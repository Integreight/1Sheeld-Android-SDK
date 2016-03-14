package com.integreight.onesheeld.sdk;

//import android.os.Handler;
//import android.os.Looper;

/**
 * Created by iSsO on 2/29/16.
 */
abstract class OneSheeldConnection {
    private BluetoothConnectionCallback connectionCallback;
    private BluetoothConnectionCloseCallback connectionCloseCallback;
    private OneSheeldDevice device;
    private boolean isConnectionCallbackCalled;
    private boolean isConnected;

    protected OneSheeldConnection(OneSheeldDevice device/*, boolean connectOnMainThread*/) {
        this.device = device;
    }

    public final void initiate() {
        isConnectionCallbackCalled = false;
        close();
        if (onConnectionInitiationRequest())
            connectionSuccess();
        else
            connectionFailure();
    }

    abstract boolean write(byte[] buffer);

    abstract byte[] read();

    final void setConnectionCallback(BluetoothConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    final void setConnectionCloseCallback(BluetoothConnectionCloseCallback bluetoothConnectionCloseCallback) {
        this.connectionCloseCallback = bluetoothConnectionCloseCallback;
    }

    public final void close() {
        if (isConnected) {
            isConnected = false;
            onClose();
            if (connectionCloseCallback != null) connectionCloseCallback.onConnectionClose();
        }
    }

    protected final void connectionSuccess() {
        if (connectionCallback != null && !isConnectionCallbackCalled) {
            isConnectionCallbackCalled = true;
            isConnected = true;
            connectionCallback.onConnectionSuccess();
        }
    }

    protected final void connectionFailure() {
        if (!isConnectionCallbackCalled) {
            close();
            isConnectionCallbackCalled = true;
            isConnected = false;
            if (connectionCallback != null) connectionCallback.onConnectionFailure();
        }
    }

    protected final void connectionInterrupt() {
        if (!isConnectionCallbackCalled) {
            close();
            isConnectionCallbackCalled = true;
            isConnected = false;
            if (connectionCallback != null) connectionCallback.onConnectionInterrupt();
        }
    }

    protected final boolean isConnectionCallbackCalled() {
        return isConnectionCallbackCalled;
    }

    protected abstract boolean onConnectionInitiationRequest();

    protected final OneSheeldDevice getDevice() {
        return device;
    }

    protected abstract void onClose();
}
