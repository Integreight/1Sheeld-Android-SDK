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

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ClassicConnection extends OneSheeldConnection {
    private BluetoothSocket socket;
    private boolean isDefaultConnectingRetriesEnabled;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final int MAX_BUFFER_SIZE = 1024;

    ClassicConnection(OneSheeldDevice device, boolean isDefaultConnectingRetriesEnabled) {
        super(device);
        this.isDefaultConnectingRetriesEnabled = isDefaultConnectingRetriesEnabled;
    }

    protected synchronized boolean onConnectionInitiationRequest() {
        boolean isConnectionSuccessful;
        int triesCounter = 3;
        do {
            socket = isDefaultConnectingRetriesEnabled ? BluetoothUtils.getRfcommSocket(getDevice().getBluetoothDevice(), 3 - triesCounter) : BluetoothUtils.getRfcommSocket(getDevice().getBluetoothDevice());
            isConnectionSuccessful = true;
            try {
                if (socket != null) {
                    Thread.sleep(500);
                    socket.connect();
                } else {
                    triesCounter--;
                    isConnectionSuccessful = false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                connectionInterrupt();
                return false;
            } catch (Exception e) {
                triesCounter--;
                isConnectionSuccessful = false;
            }
        } while (triesCounter > 0 && !isConnectionSuccessful);

        if (isConnectionSuccessful) {
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                return false;
            }
        }
        return isConnectionSuccessful;
    }

    boolean write(final byte[] buffer) {
        if (socket == null || outputStream == null) return false;
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            close();
            return false;
        }
        return true;
    }

    byte[] read() {
        if (socket == null || inputStream == null) return new byte[]{};
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int bufferLength;
        try {
            bufferLength = inputStream.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            close();
            return new byte[]{};
        }
        bufferLength = bufferLength >= buffer.length ? buffer.length : bufferLength;
        return ArrayUtils.copyOfRange(buffer, 0, bufferLength);
    }

    protected void onClose() {
        if (inputStream != null)
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        if (outputStream != null)
            try {
                outputStream.close();

            } catch (IOException ignored) {
            }
        if (socket != null)
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        inputStream = null;
        outputStream = null;
        socket = null;
    }
}
