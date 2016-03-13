package com.integreight.onesheeld.sdk;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by iSsO on 2/29/16.
 */
class ClassicConnection extends OneSheeldConnection {
    BluetoothSocket socket;
    boolean isDefaultConnectingRetriesEnabled;
    private InputStream inputStream;
    private OutputStream outputStream;
    public final int MAX_BUFFER_SIZE = 1024;

    public ClassicConnection(OneSheeldDevice device, boolean isDefaultConnectingRetriesEnabled) {
        super(device/*, false*/);
        this.isDefaultConnectingRetriesEnabled = isDefaultConnectingRetriesEnabled;
    }

    protected boolean onConnectionInitiationRequest() {
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

    public boolean write(final byte[] buffer) {
        if (socket == null || outputStream == null) return false;
        try {
            outputStream.write(buffer);
        } catch (IOException e) {
            close();
            return false;
        }
        return true;
    }

    public byte[] read() {
        if (socket == null || inputStream == null) return new byte[]{};
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int bufferLength ;
        try {
            bufferLength = inputStream.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            close();
            return new byte[]{};
        }
        bufferLength = bufferLength >= buffer.length ? buffer.length : bufferLength;
        return ArrayUtils.copyOfRange(buffer, 0, bufferLength);
    }

    public void onClose() {
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
