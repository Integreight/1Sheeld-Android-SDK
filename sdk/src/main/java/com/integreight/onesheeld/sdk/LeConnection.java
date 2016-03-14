package com.integreight.onesheeld.sdk;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by iSsO on 2/29/16.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LeConnection extends OneSheeldConnection {
    public static final String COMMUNICATIONS_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String COMMUNICATIONS_CHAR_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final int MAX_BUFFER_SIZE = 1024;
    public final int MAX_DATA_LENGTH_PER_WRITE = 20;
    OneSheeldDevice device;
    BluetoothGatt bluetoothGatt;
    Queue<Byte> readBuffer;
    private final Queue<byte[]> writeBuffer;
    private final Object connectionLock;
    private boolean hasGattCallbackReplied;
    private boolean isConnectionSuccessful;
    private byte[] pendingSending;
    private TimeOut sendingPendingBytesTimeOut;
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (!isConnectionCallbackCalled()) {
                    notifyConnectionFailure();
                }
                else {
                    close();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic commChar = service.getCharacteristic(UUID.fromString(COMMUNICATIONS_CHAR_UUID));
                    if (commChar != null && (commChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 &&
                            (commChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                        gatt.setCharacteristicNotification(commChar, true);
                        commChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        {
                            notifyConnectionSuccess();
                        }
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
                    for (byte dataByte : data) {
                        readBuffer.add(dataByte);
                    }
                }
            } else {
                close();
            }
        }
    };

    public LeConnection(OneSheeldDevice device) {
        super(device);
        this.device = device;
        this.readBuffer = new ConcurrentLinkedQueue<>();
        this.writeBuffer = new ConcurrentLinkedQueue<>();
        this.connectionLock = new Object();
        this.hasGattCallbackReplied = false;
        this.isConnectionSuccessful = false;
        this.pendingSending=new byte[]{};
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
        sendingPendingBytesTimeOut = new TimeOut(20, 20, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                synchronized (writeBuffer){
                    for (int i = 0; i < pendingSending.length; i += MAX_DATA_LENGTH_PER_WRITE) {
                            byte[] subArray=(i + MAX_DATA_LENGTH_PER_WRITE > pendingSending.length)?
                                    ArrayUtils.copyOfRange(pendingSending, i, pendingSending.length):
                                    ArrayUtils.copyOfRange(pendingSending, i, i + MAX_DATA_LENGTH_PER_WRITE);
                            writeBuffer.add(subArray);
                    }
                    pendingSending=new byte[]{};
                    flushWriteBuffer();
                }
            }

            @Override
            public void onTick(long milliSecondsLeft) {
            }
        });
    }

    @Override
    synchronized boolean write(byte[] buffer) {
        if (bluetoothGatt == null || buffer.length<=0|| !hasGattCallbackReplied || !isConnectionSuccessful || bluetoothGatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID)) == null ||
                bluetoothGatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID)).getCharacteristic(UUID.fromString(COMMUNICATIONS_CHAR_UUID)) == null) {
            return false;
        }
        synchronized (writeBuffer) {
            if(pendingSending.length<=0)initSendingPendingBytesTimeOut();
            pendingSending=ArrayUtils.concatenateBytesArrays(pendingSending, buffer);
            if(pendingSending.length<20){
                return true;
            }
            stopSendingPendingBytesTimeOut();
            for (int i = 0; i < pendingSending.length; i += MAX_DATA_LENGTH_PER_WRITE) {
                if(i + MAX_DATA_LENGTH_PER_WRITE > pendingSending.length){
                    pendingSending=ArrayUtils.copyOfRange(pendingSending, i, pendingSending.length);
                    if(pendingSending.length>0)initSendingPendingBytesTimeOut();
                    break;
                }
                else{
                    byte[] subArray=ArrayUtils.copyOfRange(pendingSending, i, i + MAX_DATA_LENGTH_PER_WRITE);
                    writeBuffer.add(subArray);
                }
            }
            return flushWriteBuffer();
        }
    }

    private boolean flushWriteBuffer(){
        if (bluetoothGatt == null || !hasGattCallbackReplied || !isConnectionSuccessful || bluetoothGatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID)) == null ||
                bluetoothGatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID)).getCharacteristic(UUID.fromString(COMMUNICATIONS_CHAR_UUID)) == null) {
            return false;
        }

        BluetoothGattCharacteristic commChar = bluetoothGatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID)).getCharacteristic(UUID.fromString(COMMUNICATIONS_CHAR_UUID));
        synchronized (writeBuffer) {
            while (!writeBuffer.isEmpty()) {
                byte[] byteArrayInProgress = writeBuffer.poll();
                boolean isSet = false;
                for (int i = 0; i < 3; i++) {
                    if (commChar.setValue(byteArrayInProgress)) {
                        isSet = true;
                        break;
                    }
                }
                if (!isSet) {
                    writeBuffer.clear();
                    return false;
                }
                bluetoothGatt.writeCharacteristic(commChar);
                try {
                    writeBuffer.wait();
                } catch (InterruptedException e) {
                    writeBuffer.clear();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    synchronized byte[] read() {
        if (bluetoothGatt == null || readBuffer.isEmpty() || !hasGattCallbackReplied || !isConnectionSuccessful)
            return new byte[]{};
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int readBufferSize = readBuffer.size();
        int readBytesLength;
        for (readBytesLength = 0; readBytesLength < readBufferSize && readBytesLength < buffer.length && !readBuffer.isEmpty(); readBytesLength++) {
            Byte dataByte = readBuffer.poll();
            if (dataByte != null)
                buffer[readBytesLength] = dataByte;
            else break;
        }
        return ArrayUtils.copyOfRange(buffer, 0, readBytesLength);
    }

    @Override
    protected boolean onConnectionInitiationRequest() {
        bluetoothGatt = device.getBluetoothDevice().connectGatt(OneSheeldSdk.getContext(), false, gattCallback);
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
        return isConnectionSuccessful;
    }

    @Override
    protected void onClose() {
        if (bluetoothGatt != null) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(COMMUNICATIONS_SERVICE_UUID));
            if (service != null) {
                BluetoothGattCharacteristic commChar = service.getCharacteristic(UUID.fromString(COMMUNICATIONS_CHAR_UUID));
                if (commChar != null) {
                    bluetoothGatt.setCharacteristicNotification(commChar, false);
                }
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        readBuffer.clear();
        synchronized (writeBuffer) {
            writeBuffer.clear();
            writeBuffer.notifyAll();
            pendingSending=new byte[]{};
        }
        synchronized (connectionLock) {
            isConnectionSuccessful = false;
            hasGattCallbackReplied = false;
            connectionLock.notifyAll();
        }
        stopSendingPendingBytesTimeOut();
    }
}
