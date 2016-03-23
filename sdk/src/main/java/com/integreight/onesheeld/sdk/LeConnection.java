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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class LeConnection extends OneSheeldConnection {
    private final int MAX_DATA_LENGTH_PER_WRITE = 20;
    private OneSheeldDevice device;
    private BluetoothGatt bluetoothGatt;
    private final Queue<Byte> readBuffer;
    private final Queue<byte[]> writeBuffer;
    private final Object connectionLock;
    private boolean hasGattCallbackReplied;
    private boolean isConnectionSuccessful;
    private byte[] pendingSending;
    private TimeOut sendingPendingBytesTimeOut;
    private final Object writeLock;
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (!isConnectionCallbackCalled()) {
                    notifyConnectionFailure();
                } else {
                    close();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic commChar = service.getCharacteristic(BluetoothUtils.COMMUNICATIONS_CHAR_UUID);
                    if (commChar != null && (commChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 &&
                            (commChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                        commChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        notifyConnectionSuccess();
                    } else notifyConnectionFailure();
                } else
                    notifyConnectionFailure();

            } else {
                notifyConnectionFailure();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (gatt != null && characteristic != null) {
                synchronized (writeBuffer) {
                    writeBuffer.notifyAll();
                }
            } else {
                close();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (gatt != null && characteristic != null) {
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    synchronized (readBuffer) {
                        for (byte dataByte : data) {
                            readBuffer.add(dataByte);
                        }
                    }
                }
            } else {
                close();
            }
        }
    };

    LeConnection(OneSheeldDevice device) {
        super(device);
        this.device = device;
        this.readBuffer = new ConcurrentLinkedQueue<>();
        this.writeBuffer = new ConcurrentLinkedQueue<>();
        this.connectionLock = new Object();
        this.hasGattCallbackReplied = false;
        this.isConnectionSuccessful = false;
        this.pendingSending = new byte[]{};
        this.writeLock = new Object();
    }

    private void notifyConnectionFailure() {
        synchronized (connectionLock) {
            hasGattCallbackReplied = true;
            isConnectionSuccessful = false;
            connectionLock.notifyAll();
        }
    }

    private void notifyConnectionSuccess() {
        synchronized (connectionLock) {
            hasGattCallbackReplied = true;
            isConnectionSuccessful = true;
            connectionLock.notifyAll();
        }
    }

    private void stopSendingPendingBytesTimeOut() {
        if (sendingPendingBytesTimeOut != null)
            sendingPendingBytesTimeOut.stopTimer();
    }

    private void initSendingPendingBytesTimeOut() {
        stopSendingPendingBytesTimeOut();
        sendingPendingBytesTimeOut = new TimeOut(100, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                synchronized (writeLock) {
                    synchronized (writeBuffer) {
                        if (pendingSending.length >= 0) {
                            for (int i = 0; i < pendingSending.length; i += MAX_DATA_LENGTH_PER_WRITE) {
                                byte[] subArray = (i + MAX_DATA_LENGTH_PER_WRITE > pendingSending.length) ?
                                        ArrayUtils.copyOfRange(pendingSending, i, pendingSending.length) :
                                        ArrayUtils.copyOfRange(pendingSending, i, i + MAX_DATA_LENGTH_PER_WRITE);
                                writeBuffer.add(subArray);
                            }
                            pendingSending = new byte[]{};
                            flushWriteBuffer();
                        }
                    }
                }
            }

            @Override
            public void onTick(long milliSecondsLeft) {
            }
        });
        sendingPendingBytesTimeOut.setName("SendingPendingBytesTimeOut");
    }

    @Override
    boolean write(byte[] buffer) {
        if (bluetoothGatt == null || buffer.length <= 0 || !hasGattCallbackReplied || !isConnectionSuccessful || bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID) == null ||
                bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID).getCharacteristic(BluetoothUtils.COMMUNICATIONS_CHAR_UUID) == null) {
            return false;
        }
        synchronized (writeLock) {
            synchronized (writeBuffer) {
                if (pendingSending.length <= 0 && buffer.length < 20)
                    initSendingPendingBytesTimeOut();
                pendingSending = ArrayUtils.concatenateBytesArrays(pendingSending, buffer);
                if (pendingSending.length < 20) {
                    return true;
                }
                if (sendingPendingBytesTimeOut != null) sendingPendingBytesTimeOut.resetTimer();
                boolean isDataLeft = false;
                for (int i = 0; i < pendingSending.length; i += MAX_DATA_LENGTH_PER_WRITE) {
                    if (i + MAX_DATA_LENGTH_PER_WRITE > pendingSending.length) {
                        pendingSending = ArrayUtils.copyOfRange(pendingSending, i, pendingSending.length);
                        if (pendingSending.length > 0) initSendingPendingBytesTimeOut();
                        isDataLeft = true;
                        break;
                    } else {
                        byte[] subArray = ArrayUtils.copyOfRange(pendingSending, i, i + MAX_DATA_LENGTH_PER_WRITE);
                        writeBuffer.add(subArray);
                    }
                }
                if (!isDataLeft) pendingSending = new byte[]{};
                return flushWriteBuffer();
            }
        }
    }

    private boolean flushWriteBuffer() {
        if (bluetoothGatt == null || !hasGattCallbackReplied || !isConnectionSuccessful || bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID) == null ||
                bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID).getCharacteristic(BluetoothUtils.COMMUNICATIONS_CHAR_UUID) == null) {
            return false;
        }

        BluetoothGattCharacteristic commChar = bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID).getCharacteristic(BluetoothUtils.COMMUNICATIONS_CHAR_UUID);
        synchronized (writeBuffer) {
            while (!writeBuffer.isEmpty()) {
                byte[] byteArrayInProgress = writeBuffer.peek();
                boolean isSet = false;
                for (int i = 0; i < 3; i++) {
                    if (commChar.setValue(byteArrayInProgress)) {
                        isSet = true;
                        break;
                    }
                }
                if (!isSet) {
                    return false;
                }
                bluetoothGatt.writeCharacteristic(commChar);
                try {
                    writeBuffer.wait();
                } catch (InterruptedException e) {
                    return false;
                }
                writeBuffer.poll();
            }
        }
        return true;
    }

    @Override
    byte[] read() {
        synchronized (readBuffer) {
            if (bluetoothGatt == null || readBuffer.isEmpty() || !hasGattCallbackReplied || !isConnectionSuccessful)
                return new byte[]{};
            int readBufferSize = readBuffer.size();
            byte[] buffer = new byte[readBufferSize];
            int readBytesLength;
            for (readBytesLength = 0; readBytesLength < buffer.length; readBytesLength++) {
                Byte dataByte = readBuffer.poll();
                if (dataByte != null)
                    buffer[readBytesLength] = dataByte;
                else break;
            }
            return buffer;
        }
    }

    @Override
    protected synchronized boolean onConnectionInitiationRequest() {
        bluetoothGatt = device.getBluetoothDevice().connectGatt(OneSheeldSdk.getContext(), false, gattCallback);
        BluetoothUtils.refreshDeviceCache(bluetoothGatt);
        synchronized (connectionLock) {
            isConnectionSuccessful = false;
            hasGattCallbackReplied = false;
            while (!hasGattCallbackReplied) {
                try {
                    connectionLock.wait();
                } catch (InterruptedException e) {
                    connectionInterrupt();
                    return false;
                }
            }
        }
        if(isConnectionSuccessful){
            BluetoothGattService service = bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID);
            if (service != null) {
                BluetoothGattCharacteristic commChar = service.getCharacteristic(BluetoothUtils.COMMUNICATIONS_CHAR_UUID);
                if (commChar != null) {
                    BluetoothUtils.setCharacteristicNotification(bluetoothGatt, commChar, true);
                }
            }
        }
        return isConnectionSuccessful;
    }

    @Override
    protected void onClose() {
        if (bluetoothGatt != null) {
            BluetoothGattService service = bluetoothGatt.getService(BluetoothUtils.COMMUNICATIONS_SERVICE_UUID);
            if (service != null) {
                BluetoothGattCharacteristic commChar = service.getCharacteristic(BluetoothUtils.COMMUNICATIONS_CHAR_UUID);
                if (commChar != null) {
                    BluetoothUtils.setCharacteristicNotification(bluetoothGatt, commChar, false);
                }
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        synchronized (readBuffer) {
            readBuffer.clear();
        }
        synchronized (writeBuffer) {
            writeBuffer.clear();
            pendingSending = new byte[]{};
            writeBuffer.notifyAll();
        }
        synchronized (connectionLock) {
            isConnectionSuccessful = false;
            hasGattCallbackReplied = false;
            connectionLock.notifyAll();
        }
        stopSendingPendingBytesTimeOut();
    }
}
