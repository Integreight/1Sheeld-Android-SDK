package com.integreight.onesheeld.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.integreight.onesheeld.sdk.exceptions.BluetoothNotSupportedException;
import com.integreight.onesheeld.sdk.exceptions.MissingBluetoothPermmissionsException;
import com.integreight.onesheeld.sdk.exceptions.NullOneSheeldDeviceException;
import com.integreight.onesheeld.sdk.exceptions.SdkNotInitializedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class OneSheeldManager {
    private static OneSheeldManager instance = null;
    private final Object currentStateLock = new Object();
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

    public void scan() {
        Log.d("Manager: Bluetooth scanning requested.");
        if (!handleBluetoothErrors()) {
            startScanning();
        } else Log.d("Manager: Unable to initiate Bluetooth scanning, an error occurred.");
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getScanningTimeOut() {
        return scanningTimeOutValue;
    }

    public void setScanningTimeOut(int scanningTimeOut) {
        this.scanningTimeOutValue = scanningTimeOut;
    }

    public ConnectionState getCurrentState() {
        synchronized (currentStateLock) {
            return currentState;
        }
    }

    public void broadcastSerialData(byte[] data) {
        broadcastSerialData(data, null);
    }

    public void broadcastSerialData(byte[] data, OneSheeldDevice exceptionArray[]) {
        Log.d("Manager: Broadcasting serial data to all connected devices.");
        for (OneSheeldDevice device : connectedDevices.values()) {
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

    public void broadcastShieldFrame(ShieldFrame frame) {
        broadcastShieldFrame(frame, null);
    }

    public void broadcastShieldFrame(ShieldFrame frame, OneSheeldDevice exceptionArray[]) {
        broadcastShieldFrame(frame, false, exceptionArray);
    }

    public void broadcastShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        broadcastShieldFrame(frame, waitIfInACallback, null);
    }

    public void broadcastShieldFrame(ShieldFrame frame, boolean waitIfInACallback, OneSheeldDevice exceptionArray[]) {
        Log.d("Manager: Broadcasting frame to all connected devices.");
        for (OneSheeldDevice device : connectedDevices.values()) {
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

    public boolean isScanning() {
        synchronized (currentStateLock) {
            return currentState == ConnectionState.SCANNING;
        }
    }

    public boolean isReady() {
        synchronized (currentStateLock) {
            return currentState == ConnectionState.READY;
        }
    }

    public boolean isConnecting() {
        synchronized (currentStateLock) {
            return currentState == ConnectionState.CONNECTING;
        }
    }

    public boolean isAutomaticConnectingRetriesEnabled() {
        return isAutomaticConnectingRetriesEnabled;
    }

    public void setAutomaticConnectingRetries(boolean value) {
        isAutomaticConnectingRetriesEnabled = value;
    }

    public List<OneSheeldDevice> getConnectedDevices() {
        return Collections.unmodifiableList(new ArrayList<>(connectedDevices.values()));
    }

    public void disconnectAll() {
        Log.d("Manager: Disconnect all connected devices.");
        for (OneSheeldDevice device : connectedDevices.values()) {
            disconnect(device);
        }
    }

    public void disconnect(OneSheeldDevice device) {
        Log.d("Manager: Delegate the disconnection from " + device.getName() + " to the device.");
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
            if (device.isConnected() || connectedDevices.containsKey(device.getAddress())) {
                Log.d("Manager: Connection to " + device.getName() + " failed, already connected to that device.");
                onError(device, OneSheeldError.ALREADY_CONNECTED_TO_DEVICE);
                device.onError(OneSheeldError.ALREADY_CONNECTED_TO_DEVICE);
            } else if (connectedDevices.size() >= BluetoothUtils.MAXIMUM_CONNECTED_BLUETOOTH_DEVICES) {
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

    public void connect(OneSheeldDevice device) {
        if (device != null) {
            Log.d("Manager: Connection request to " + device.getName() + " received.");
            if (!handleBluetoothErrors()) {
                startConnection(device);
            } else
                Log.d("Unable to initiate connection to " + device.getName() + ", an error occurred.");
        } else
            throw new NullOneSheeldDeviceException("OneSheeldDevice is null, have you checked its validity?");
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
        while (connectedDevices.containsKey(device.getAddress()))
            connectedDevices.remove(device.getAddress());
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
        while (connectedDevices.containsKey(device.getAddress()))
            connectedDevices.remove(device.getAddress());
        connectedDevices.put(device.getAddress(), device);
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
            throw new MissingBluetoothPermmissionsException("Bluetooth permissions are missing. Have you added them to the manifest?");
        } else if (!BluetoothUtils.doesDeviceHasBluetooth()) {
            throw new BluetoothNotSupportedException("The device doesn't support Bluetooth. Are you sure you ran it on the correct device?");
        } else if (!BluetoothUtils.isBluetoothEnabled()) {
            onError(null, OneSheeldError.BLUETOOTH_NOT_ENABLED);
            return true;
        } else if (state != ConnectionState.READY) {
            if (state == ConnectionState.CONNECTING) {
                onError(null, OneSheeldError.PENDING_CONNECTION_IN_PROGRESS);
            } else if (state == ConnectionState.SCANNING) {
                onError(null, OneSheeldError.SCANNING_IN_PROGRESS);
            }
            return true;
        }
        return false;
    }

    public void addCallbacks(OneSheeldScanningCallback scanningCallback, OneSheeldConnectionCallback connectionCallback, OneSheeldErrorCallback errorCallback) {
        addScanningCallback(scanningCallback);
        addConnectionCallback(connectionCallback);
        addErrorCallback(errorCallback);
    }

    public void addConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && !connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.add(connectionCallback);
    }

    public void addScanningCallback(OneSheeldScanningCallback scanningCallback) {
        if (scanningCallback != null && !scanningCallbacks.contains(scanningCallback))
            scanningCallbacks.add(scanningCallback);
    }

    public void addErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && !errorCallbacks.contains(errorCallback))
            errorCallbacks.add(errorCallback);
    }

    public void removeConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.remove(connectionCallback);
    }

    public void removeScanningCallback(OneSheeldScanningCallback scanningCallback) {
        if (scanningCallback != null && scanningCallbacks.contains(scanningCallback))
            scanningCallbacks.remove(scanningCallback);
    }

    public void removeErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && errorCallbacks.contains(errorCallback))
            errorCallbacks.remove(errorCallback);
    }

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

        public ConnectThread(OneSheeldDevice device) {
            this.device = device;
            setName("OneSheeldConnectThread: " + device.getName());
        }

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

        public synchronized void cancel() {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e) {
                }
        }
    }
}
