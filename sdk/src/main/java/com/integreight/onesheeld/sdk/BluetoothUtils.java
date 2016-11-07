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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.UUID;

abstract class BluetoothUtils {
    public static final int MAXIMUM_CONNECTED_BLUETOOTH_DEVICES = 7;
    static final UUID BLUETOOTH_SPP_PROFILE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final UUID COMMUNICATIONS_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    static final UUID COMMUNICATIONS_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    static final UUID DEVICE_CONFIG_CHARACTERISTIC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    static boolean isBluetoothEnabled() {
        return doesDeviceHasBluetooth() && getBluetoothAdapter().isEnabled();
    }

    static boolean doesDeviceHasBluetooth() {
        return getBluetoothAdapter() != null;
    }

    static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    static synchronized BluetoothSocket getRfcommSocket(BluetoothDevice device, int numberOfRetries) {
        BluetoothSocket socket = null;
        numberOfRetries = numberOfRetries % 3;
        switch (numberOfRetries) {
            case 0:
                try {
                    if (Build.VERSION.SDK_INT < 10) {
                        socket = getInsecureRfcommSocketByReflection(device);
                    } else {
                        socket = device.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_SPP_PROFILE);
                    }
                    break;
                } catch (Exception e) {
                }

            case 1:
                try {
                    socket = device
                            .createRfcommSocketToServiceRecord(BLUETOOTH_SPP_PROFILE);
                    break;
                } catch (Exception e) {
                }

            case 2:
                try {
                    socket = getRfcommSocketByReflection(device);
                } catch (Exception e) {
                }
                break;
        }
        return socket;
    }

    static synchronized BluetoothSocket getRfcommSocket(BluetoothDevice device) {
        return getRfcommSocket(device, 0);
    }

    private static synchronized BluetoothSocket getRfcommSocketByReflection(BluetoothDevice device) throws Exception {
        if (device == null)
            return null;
        Method m = device.getClass().getMethod("createRfcommSocket",
                new Class[]{int.class});

        return (BluetoothSocket) m.invoke(device, 1);
    }

    private static synchronized BluetoothSocket getInsecureRfcommSocketByReflection(BluetoothDevice device) throws Exception {
        if (device == null)
            return null;
        Method m = device.getClass().getMethod("createInsecureRfcommSocket",
                new Class[]{int.class});

        return (BluetoothSocket) m.invoke(device, 1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    static boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                return (boolean) (Boolean) localMethod.invoke(gatt, new Object[0]);
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    static boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean isEnabled) {
        if (characteristic != null) {
            gatt.setCharacteristicNotification(characteristic, isEnabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DEVICE_CONFIG_CHARACTERISTIC);
            if (descriptor != null) {
                descriptor.setValue(isEnabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
                return gatt.writeDescriptor(descriptor);
            } else return false;
        } else return false;
    }
}
