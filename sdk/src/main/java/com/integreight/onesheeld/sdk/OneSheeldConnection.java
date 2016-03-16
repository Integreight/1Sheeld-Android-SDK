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

abstract class OneSheeldConnection {
    private BluetoothConnectionCallback connectionCallback;
    private BluetoothConnectionCloseCallback connectionCloseCallback;
    private OneSheeldDevice device;
    private boolean isConnectionCallbackCalled;
    private boolean isConnected;

    protected OneSheeldConnection(OneSheeldDevice device) {
        this.device = device;
    }

    final synchronized void initiate() {
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

    final void close() {
        onClose();
        if (isConnected) {
            isConnected = false;
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
