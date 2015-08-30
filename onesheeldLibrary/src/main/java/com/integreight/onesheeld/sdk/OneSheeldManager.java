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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents the manager that is responsible for all Bluetooth operations.
 * <p>It is responsible for scanning and connecting to 1Sheeld devices.</p>
 *
 * @see OneSheeldDevice
 */
public class OneSheeldManager {
    private static OneSheeldManager instance = null;
    private final Object currentStateLock = new Object();
    private final Object connectedDevicesLock = new Object();
    private int retryCount;
    private int scanningTimeOutValue;
    private TimeOut scanningTimeOut;
    private ConnectThread connectThread;
    private boolean isAutomaticConnectingRetriesEnabled;
    private CopyOnWriteArrayList<OneSheeldConnectionCallback> connectionCallbacks;
    private CopyOnWriteArrayList<OneSheeldScanningCallback> scanningCallbacks;
    private CopyOnWriteArrayList<OneSheeldErrorCallback> errorCallbacks;
    private ConcurrentHashMap<String, OneSheeldDevice> connectedDevices;
    private ConcurrentHashMap<String, OneSheeldDevice> foundOneSheeldDevices;
    private ConcurrentHashMap<String, OneSheeldDevice> otherBluetoothDevices;
    private ConnectionState currentState;
    private BluetoothAdapter bluetoothAdapter;
    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addFoundDevice(device.getName(), device.getAddress(), device.getBondState() == BluetoothDevice.BOND_BONDED);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                finishScanning();
            }
        }
    };

    private OneSheeldManager() {
        retryCount = 0;
        connectionCallbacks = new CopyOnWriteArrayList<>();
        scanningCallbacks = new CopyOnWriteArrayList<>();
        errorCallbacks = new CopyOnWriteArrayList<>();
        connectedDevices = new ConcurrentHashMap<>();
        foundOneSheeldDevices = new ConcurrentHashMap<>();
        otherBluetoothDevices = new ConcurrentHashMap<>();
        scanningTimeOutValue = 20;
        currentState = ConnectionState.READY;
        bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        isAutomaticConnectingRetriesEnabled = false;
    }

    static OneSheeldManager getInstance() {
        if (instance == null && OneSheeldSdk.isInit()) {
            synchronized (OneSheeldManager.class) {
                if (instance == null) {
                    instance = new OneSheeldManager();
                }
            }
        }
        return instance;
    }

    /**
     * Starts a Bluetooth scanning for 1Sheeld devices.
     *
     * @throws SdkNotInitializedException           if the <tt>OneSheeldSdk.init()</tt> hasn't been called.
     * @throws MissingBluetoothPermissionsException if the Bluetooth permissions has been omitted from AndroidManifest.xml
     * @throws BluetoothNotSupportedException       if the Android device doesn't support Bluetooth.
     */
    public void scan() {
        Log.d("Manager: Bluetooth scanning requested.");
        if (!handleBluetoothErrors()) {
            startScanning();
        } else Log.d("Manager: Unable to initiate Bluetooth scanning, an error occurred.");
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets retry count value.
     * <p>default value is 0</p>
     *
     * @param retryCount the new retry count value.
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * Gets scanning time out value.
     *
     * @return the scanning time out value
     */
    public int getScanningTimeOut() {
        return scanningTimeOutValue;
    }

    /**
     * Sets scanning time out value.
     * <p>default value is 20 seconds</p>
     *
     * @param scanningTimeOut the scanning time out value in seconds.
     */
    public void setScanningTimeOut(int scanningTimeOut) {
        this.scanningTimeOutValue = scanningTimeOut;
    }

    /**
     * Broadcast raw serial data to all connected devices on their 0,1 pins.
     *
     * @param data the data
     */
    public void broadcastSerialData(byte[] data) {
        broadcastSerialData(data, null);
    }

    /**
     * Broadcast raw serial data to all connected devices on their 0,1 pins except the ones provided.
     *
     * @param data           the data
     * @param exceptionArray the excepted devices array
     * @throws NullPointerException if the passed data array is null
     */
    public void broadcastSerialData(byte[] data, OneSheeldDevice exceptionArray[]) {
        if (data == null)
            throw new NullPointerException("The passed data array is null, have you checked its validity?");
        Log.d("Manager: Broadcasting serial data to all connected devices.");
        ArrayList<OneSheeldDevice> tempConnectedDevices;
        synchronized (connectedDevicesLock) {
            tempConnectedDevices = new ArrayList<>(connectedDevices.values());
        }
        for (OneSheeldDevice device : tempConnectedDevices) {
            boolean foundInExceptArray = false;
            if (exceptionArray != null)
                for (OneSheeldDevice exceptDevice : exceptionArray)
                    if (device.getAddress().equals(exceptDevice.getAddress())) {
                        foundInExceptArray = true;
                        break;
                    }
            if (!foundInExceptArray) device.sendSerialData(data);
        }
    }

    /**
     * Broadcast a shield frame to all connected devices on their 0,1 pins.
     *
     * @param frame the frame
     */
    public void broadcastShieldFrame(ShieldFrame frame) {
        broadcastShieldFrame(frame, null);
    }

    /**
     * Broadcast a shield frame to all connected devices on their 0,1 pins except the ones provided.
     *
     * @param frame          the frame
     * @param exceptionArray the excepted devices array
     */
    public void broadcastShieldFrame(ShieldFrame frame, OneSheeldDevice exceptionArray[]) {
        broadcastShieldFrame(frame, false, exceptionArray);
    }

    /**
     * Broadcast a shield frame to all connected devices on their 0,1 pins.
     *
     * @param frame             the frame
     * @param waitIfInACallback if true the frame will be queued if the Arduino is in a callback
     */
    public void broadcastShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        broadcastShieldFrame(frame, waitIfInACallback, null);
    }

    /**
     * Broadcast a shield frame to all connected devices on their 0,1 pins except the ones provided.
     *
     * @param frame             the frame
     * @param waitIfInACallback if true the frame will be queued if the Arduino is in a callback
     * @param exceptionArray    the excepted devices array
     * @throws NullPointerException if passed frame is null
     */
    public void broadcastShieldFrame(ShieldFrame frame, boolean waitIfInACallback, OneSheeldDevice exceptionArray[]) {
        if (frame == null)
            throw new NullPointerException("The passed frame is null, have you checked its validity?");
        Log.d("Manager: Broadcasting frame to all connected devices.");
        ArrayList<OneSheeldDevice> tempConnectedDevices;
        synchronized (connectedDevicesLock) {
            tempConnectedDevices = new ArrayList<>(connectedDevices.values());
        }
        for (OneSheeldDevice device : tempConnectedDevices) {
            boolean foundInExceptArray = false;
            if (exceptionArray != null)
                for (OneSheeldDevice exceptDevice : exceptionArray)
                    if (device.getAddress().equals(exceptDevice.getAddress())) {
                        foundInExceptArray = true;
                        break;
                    }
            if (!foundInExceptArray) device.sendShieldFrame(frame, waitIfInACallback);
        }
    }

    /**
     * Is the manager scanning.
     *
     * @return the boolean
     */
    public boolean isScanning() {
        synchronized (currentStateLock) {
            return currentState == ConnectionState.SCANNING;
        }
    }

    /**
     * Is the manager ready for scanning or connection.
     *
     * @return the boolean
     */
    public boolean isReady() {
        synchronized (currentStateLock) {
            return currentState == ConnectionState.READY;
        }
    }

    /**
     * Is the manager connecting to a device.
     *
     * @return the boolean
     */
    public boolean isConnecting() {
        synchronized (currentStateLock) {
            return currentState == ConnectionState.CONNECTING;
        }
    }

    /**
     * Is automatic connecting retries enabled.
     *
     * @return the boolean
     */
    public boolean isAutomaticConnectingRetriesEnabled() {
        return isAutomaticConnectingRetriesEnabled;
    }

    /**
     * Sets the automatic connecting retries.
     * <p>If set, the manager will try 3 different methods of connection for each connection attempt.</p>
     * <p>default value is false.</p>
     *
     * @param value the value
     */
    public void setAutomaticConnectingRetries(boolean value) {
        isAutomaticConnectingRetriesEnabled = value;
    }

    /**
     * Gets a list of connected devices.
     *
     * @return an unmodifiable list of connected devices
     */
    public List<OneSheeldDevice> getConnectedDevices() {
        synchronized (connectedDevicesLock) {
            return Collections.unmodifiableList(new ArrayList<>(connectedDevices.values()));
        }
    }

    /**
     * Disconnect all connected devices.
     */
    public void disconnectAll() {
        Log.d("Manager: Disconnect all connected devices.");
        ArrayList<OneSheeldDevice> tempConnectedDevices;
        synchronized (connectedDevicesLock) {
            tempConnectedDevices = new ArrayList<>(connectedDevices.values());
        }
        for (OneSheeldDevice device : tempConnectedDevices) {
            disconnect(device);
        }
    }

    /**
     * Disconnect a specific device.
     *
     * @param device the device
     * @throws NullPointerException if the passed device is null
     */
    public void disconnect(OneSheeldDevice device) {
        if (device == null)
            throw new NullPointerException("The passed device is null, have you checked its validity?");
        Log.d("Manager: Delegate the disconnection from " + device.getName() + " to the device itself.");
        device.disconnect();
    }

    private void stopScanningTimeOut() {
        if (scanningTimeOut != null)
            scanningTimeOut.stopTimer();
    }

    private void initScanningTimeOut() {
        stopScanningTimeOut();
        scanningTimeOut = new TimeOut(scanningTimeOutValue * 1000, 1000, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                finishScanning();
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });

    }

    private void resetScanningTimeOutTimer() {
        if (scanningTimeOut != null) scanningTimeOut.resetTimer();
    }

    /**
     * Cancel any pending connection operations.
     */
    public void cancelConnecting() {
        boolean isConnecting = false;
        synchronized (currentStateLock) {
            if (currentState == ConnectionState.CONNECTING) {
                isConnecting = true;
                currentState = ConnectionState.READY;
            }
        }
        if (isConnecting) {
            Log.d("Manager: Stopping pending connection.");
            stopConnectThread();
        }
    }

    private void startConnection(OneSheeldDevice device) {
        boolean isReady = false;
        synchronized (currentStateLock) {
            if (currentState == ConnectionState.READY) {
                isReady = true;
            }
        }
        if (isReady) {
            HashMap<String, OneSheeldDevice> tempConnectedDevices;
            synchronized (connectedDevicesLock) {
                tempConnectedDevices = new HashMap<>(connectedDevices);
            }
            if (device.isConnected() || tempConnectedDevices.containsKey(device.getAddress())) {
                Log.d("Manager: Connection to " + device.getName() + " failed, already connected to that device.");
                onError(device, OneSheeldError.ALREADY_CONNECTED_TO_DEVICE);
                device.onError(OneSheeldError.ALREADY_CONNECTED_TO_DEVICE);
            } else if (tempConnectedDevices.size() >= BluetoothUtils.MAXIMUM_CONNECTED_BLUETOOTH_DEVICES) {
                Log.d("Manager: Connection to " + device.getName() + " failed, maximum Bluetooth connections reached.");
                onError(device, OneSheeldError.MAXIMUM_BLUETOOTH_CONNECTIONS_REACHED);
                device.onError(OneSheeldError.MAXIMUM_BLUETOOTH_CONNECTIONS_REACHED);
            } else {
                synchronized (currentStateLock) {
                    currentState = ConnectionState.CONNECTING;
                }
                finishScanning();
                stopScanningTimeOut();
                Log.d("Manager: Starting connection sequence to " + device.getName() + ".");
                startConnectThread(device);
            }
        }
    }

    void onError(OneSheeldDevice device, OneSheeldError error) {
        for (OneSheeldErrorCallback errorCallback : errorCallbacks) {
            errorCallback.onError(device, error);
        }
    }

    private synchronized void stopConnectThread() {
        if (connectThread != null) {
            connectThread.interrupt();
            connectThread = null;
        }
    }

    private synchronized void startConnectThread(OneSheeldDevice device) {
        try {
            stopConnectThread();
            connectThread = new ConnectThread(device);
            connectThread.start();
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    /**
     * Connect to a new device.
     *
     * @param device the device
     * @throws SdkNotInitializedException           if the <tt>OneSheeldSdk.init()</tt> hasn't been called.
     * @throws MissingBluetoothPermissionsException if the Bluetooth permissions has been omitted from AndroidManifest.xml
     * @throws BluetoothNotSupportedException       if the Android device doesn't support Bluetooth.
     * @throws NullPointerException                 if the device is null
     */
    public void connect(OneSheeldDevice device) {
        if (device == null)
            throw new NullPointerException("The passed device is null, have you checked its validity?");
        Log.d("Manager: Connection request to " + device.getName() + " received.");
        if (!handleBluetoothErrors()) {
            startConnection(device);
        } else
            Log.d("Unable to initiate connection to " + device.getName() + ", an error occurred.");
    }

    private synchronized void onConnectionError(OneSheeldDevice device) {
        synchronized (currentStateLock) {
            currentState = ConnectionState.READY;
        }
        finishScanning();
        stopScanningTimeOut();
        onError(device, OneSheeldError.BLUETOOTH_CONNECTION_FAILED);
    }

    void onDisconnect(OneSheeldDevice device) {
        synchronized (connectedDevicesLock) {
            while (connectedDevices.containsKey(device.getAddress()))
                connectedDevices.remove(device.getAddress());
        }
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onDisconnect(device);
        }
    }

    void onConnect(OneSheeldDevice device) {
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onConnect(device);
        }
    }

    private synchronized void onConnectionStart(OneSheeldDevice device, BluetoothSocket socket) {
        Log.d("Manager: Delegate the connection management of " + device.getName() + " to the device.");
        synchronized (connectedDevicesLock) {
            while (connectedDevices.containsKey(device.getAddress()))
                connectedDevices.remove(device.getAddress());
            connectedDevices.put(device.getAddress(), device);
        }
        device.connectUsing(socket);
        synchronized (currentStateLock) {
            currentState = ConnectionState.READY;
        }
    }

    private boolean handleBluetoothErrors() {
        ConnectionState state;
        synchronized (currentStateLock) {
            state = currentState;
        }
        if (!OneSheeldSdk.isInit()) {
            throw new SdkNotInitializedException("1Sheeld SDK not initialized. Have you called OneSheeldSdk.init()?");
        } else if (OneSheeldSdk.getContext() != null && (OneSheeldSdk.getContext().checkCallingOrSelfPermission("android.permission.BLUETOOTH") !=
                PackageManager.PERMISSION_GRANTED || OneSheeldSdk.getContext().checkCallingOrSelfPermission("android.permission.BLUETOOTH_ADMIN") != PackageManager.PERMISSION_GRANTED)) {
            throw new MissingBluetoothPermissionsException("Bluetooth permissions are missing. Have you added them to the manifest?");
        } else if (!BluetoothUtils.doesDeviceHasBluetooth()) {
            throw new BluetoothNotSupportedException("The device doesn't support Bluetooth. Are you sure you ran it on the correct device?");
        } else if (!BluetoothUtils.isBluetoothEnabled()) {
            Log.d("Manager: Bluetooth was not enabled, aborting.");
            onError(null, OneSheeldError.BLUETOOTH_NOT_ENABLED);
            return true;
        } else if (state != ConnectionState.READY) {
            if (state == ConnectionState.CONNECTING) {
                Log.d("Manager: There is a pending connection in progress, aborting.");
                onError(null, OneSheeldError.PENDING_CONNECTION_IN_PROGRESS);
            } else if (state == ConnectionState.SCANNING) {
                Log.d("Manager: There is a scanning in progress, aborting.");
                onError(null, OneSheeldError.SCANNING_IN_PROGRESS);
            }
            return true;
        }
        return false;
    }

    /**
     * Add all of the manager callbacks in one method call.
     *
     * @param scanningCallback   the scanning callback
     * @param connectionCallback the connection callback
     * @param errorCallback      the error callback
     */
    public void addCallbacks(OneSheeldScanningCallback scanningCallback, OneSheeldConnectionCallback connectionCallback, OneSheeldErrorCallback errorCallback) {
        addScanningCallback(scanningCallback);
        addConnectionCallback(connectionCallback);
        addErrorCallback(errorCallback);
    }

    /**
     * Add a connection callback.
     *
     * @param connectionCallback the connection callback
     */
    public void addConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && !connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.add(connectionCallback);
    }

    /**
     * Add a scanning callback.
     *
     * @param scanningCallback the scanning callback
     */
    public void addScanningCallback(OneSheeldScanningCallback scanningCallback) {
        if (scanningCallback != null && !scanningCallbacks.contains(scanningCallback))
            scanningCallbacks.add(scanningCallback);
    }

    /**
     * Add an error callback.
     *
     * @param errorCallback the error callback
     */
    public void addErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && !errorCallbacks.contains(errorCallback))
            errorCallbacks.add(errorCallback);
    }

    /**
     * Remove a connection callback.
     *
     * @param connectionCallback the connection callback
     */
    public void removeConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.remove(connectionCallback);
    }

    /**
     * Remove a scanning callback.
     *
     * @param scanningCallback the scanning callback
     */
    public void removeScanningCallback(OneSheeldScanningCallback scanningCallback) {
        if (scanningCallback != null && scanningCallbacks.contains(scanningCallback))
            scanningCallbacks.remove(scanningCallback);
    }

    /**
     * Remove an error callback.
     *
     * @param errorCallback the error callback
     */
    public void removeErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && errorCallbacks.contains(errorCallback))
            errorCallbacks.remove(errorCallback);
    }

    /**
     * Cancel an pending scanning operations.
     */
    public void cancelScanning() {
        finishScanning();
    }

    private void startScanning() {
        boolean isReady = false;
        synchronized (currentStateLock) {
            if (currentState == ConnectionState.READY) {
                isReady = true;
                currentState = ConnectionState.SCANNING;
            }
        }
        if (isReady) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerBluetoothBroadcast();
            initScanningTimeOut();
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
                bluetoothAdapter.startDiscovery();
            }
            Log.d("Manager: Bluetooth scanning started.");
            for (OneSheeldScanningCallback scanningCallback : scanningCallbacks) {
                scanningCallback.onScanStart();
            }
        }
    }

    private void finishScanning() {
        boolean isScanning = false;
        synchronized (currentStateLock) {
            if (currentState == ConnectionState.SCANNING) {
                isScanning = true;
                currentState = ConnectionState.READY;
            }
        }
        if (isScanning || (bluetoothAdapter != null && bluetoothAdapter.isDiscovering())) {
            bluetoothAdapter.cancelDiscovery();
            stopScanningTimeOut();
            try {
                unregisterBluetoothBroadcast();
            } catch (IllegalArgumentException e) {
            }
            for (OneSheeldDevice d : otherBluetoothDevices.values()) {
                if (d.getName().equals("") && !foundOneSheeldDevices.containsKey(d.getAddress())) {
                    d.setName(d.getAddress());
                    foundOneSheeldDevices.put(d.getAddress(), d);
                    for (OneSheeldScanningCallback scanningCallback : scanningCallbacks) {
                        Log.d("Manager: Found new device: " + d.getName() + ".");
                        scanningCallback.onDeviceFind(d);
                    }
                }
            }
            Log.d("Manager: Bluetooth scanning finished, " + foundOneSheeldDevices.values().size() + " device(s) found.");
            for (OneSheeldScanningCallback scanningCallback : scanningCallbacks) {
                scanningCallback.onScanFinish(new ArrayList<>(foundOneSheeldDevices.values()));
            }
            foundOneSheeldDevices.clear();
            otherBluetoothDevices.clear();
        }
    }

    private void registerBluetoothBroadcast() {
        if (OneSheeldSdk.getContext() != null) {
            try {
                OneSheeldSdk.getContext().unregisterReceiver(bluetoothBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            OneSheeldSdk.getContext().registerReceiver(bluetoothBroadcastReceiver, filter);
        }
    }

    private void unregisterBluetoothBroadcast() {
        if (OneSheeldSdk.getContext() != null) {
            try {
                OneSheeldSdk.getContext().unregisterReceiver(bluetoothBroadcastReceiver);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void addFoundDevice(String name, final String address,
                                boolean isPaired) {
        boolean isScanning = false;
        synchronized (currentStateLock) {
            if (currentState == ConnectionState.SCANNING) {
                isScanning = true;
            }
        }
        if (isScanning) {
            resetScanningTimeOutTimer();
            if (name == null)
                name = "";
            OneSheeldDevice tempOneSheeldDevice = new OneSheeldDevice(address, name, isPaired);
            if (tempOneSheeldDevice.getName().trim().length() > 0
                    && (tempOneSheeldDevice.getName().toLowerCase().contains("1sheeld") || tempOneSheeldDevice.getAddress()
                    .equals(tempOneSheeldDevice.getName()))) {
                if (!foundOneSheeldDevices.containsKey(tempOneSheeldDevice.getAddress())) {
                    foundOneSheeldDevices.put(tempOneSheeldDevice.getAddress(), tempOneSheeldDevice);
                    for (OneSheeldScanningCallback scanningCallback : scanningCallbacks) {
                        Log.d("Manager: Found new device: " + tempOneSheeldDevice.getName() + ".");
                        scanningCallback.onDeviceFind(tempOneSheeldDevice);
                    }
                } else {
                    foundOneSheeldDevices.get(tempOneSheeldDevice.getAddress()).setPaired(tempOneSheeldDevice.isPaired());
                    foundOneSheeldDevices.get(tempOneSheeldDevice.getAddress()).setName(tempOneSheeldDevice.getName());
                }

            } else {
                otherBluetoothDevices.put(tempOneSheeldDevice.getAddress(), tempOneSheeldDevice);
            }
        }
    }

    private void onConnectionRetry(OneSheeldDevice device, int retryCount) {
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onConnectionRetry(device, retryCount);
        }
        if (device != null) device.onConnectionRetry(retryCount);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket = null;
        private OneSheeldDevice device;

        ConnectThread(OneSheeldDevice device) {
            this.device = device;
            setName("OneSheeldConnectThread: " + device.getName());
        }

        @Override
        public void run() {
            if (this.device == null) return;
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            boolean isDefaultConnectingRetriesEnabled = OneSheeldManager.this.isAutomaticConnectingRetriesEnabled;
            int totalTries = retryCount + 1;
            int triesCounter = isDefaultConnectingRetriesEnabled ? totalTries * 3 : totalTries;
            boolean hasError = false;
            do {
                socket = isDefaultConnectingRetriesEnabled ? BluetoothUtils.getRfcommSocket(device.getBluetoothDevice(), (totalTries * 3) - triesCounter) : BluetoothUtils.getRfcommSocket(device.getBluetoothDevice());
                if (hasError) {
                    hasError = false;
                    if (triesCounter % 3 == 0 | !isDefaultConnectingRetriesEnabled) {
                        Log.d("Manager: Connection attempt to " + device.getName() + " failed, retrying again in 1.5 seconds. #" + (totalTries - (isDefaultConnectingRetriesEnabled ? (triesCounter / 3) : triesCounter)));
                        onConnectionRetry(device, totalTries - (isDefaultConnectingRetriesEnabled ? (triesCounter / 3) : triesCounter));
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            cancel();
                            return;
                        }
                    }
                }
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        cancel();
                        return;
                    }
                    if (socket != null) {
                        Thread.sleep(500);
                        Log.d("Manager: Trying to connect to " + device.getName() + ".");
                        socket.connect();
                    } else {
                        triesCounter--;
                        hasError = true;
                        continue;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        cancel();
                        return;
                    }
                } catch (InterruptedException e) {
                    cancel();
                    return;
                } catch (Exception e) {
                    triesCounter--;
                    hasError = true;
                    continue;
                }
            } while (triesCounter > 0 && hasError);
            if (hasError) {
                Log.d("Manager: All connection attempts to " + device.getName() + " failed. Aborting.");
                onConnectionError(device);
            } else {
                Log.d("Manager: Connection to " + device.getName() + " succeeded.");
                onConnectionStart(device, socket);
            }
        }

        synchronized void cancel() {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                }
        }
    }
}
