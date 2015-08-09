package com.integreight.onesheeld.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by dell on 6/21/2015.
 */
public class BluetoothUtils {
    private static final UUID BLUETOOTH_SPP_PROFILE = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static boolean isBluetoothEnabled() {
        return doesDeviceHasBluetooth() && getBluetoothAdapter().isEnabled();
    }

    public static boolean doesDeviceHasBluetooth() {
        return getBluetoothAdapter() != null;
    }

    public static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static synchronized BluetoothSocket getRfcommSocket(BluetoothDevice device, int numberOfRetries) {
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

    public static synchronized BluetoothSocket getRfcommSocket(BluetoothDevice device) {
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
}
