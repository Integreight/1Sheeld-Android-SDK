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
        isAutomaticConnectingRetriesEnabled = true;
    }

    public static OneSheeldManager getInstance() {
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
        if (!handleBluetoothErrors()) {
            startScanning();
        }
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
        for (OneSheeldDevice device : connectedDevices.values()) {
            device.sendSerialData(data);
        }
    }

    public void broadcastShieldFrame(ShieldFrame frame) {
        for (OneSheeldDevice device : connectedDevices.values()) {
            device.sendShieldFrame(frame);
        }
    }

    public void broadcastShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        for (OneSheeldDevice device : connectedDevices.values()) {
            device.sendShieldFrame(frame, waitIfInACallback);
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
        for (OneSheeldDevice device : connectedDevices.values()) {
            device.disconnect();
        }
    }

    public void disconnect(OneSheeldDevice device) {
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
            stopConnectThread();
        }
    }

    private void startConnection(OneSheeldDevice device) {
        boolean isReady = false;
        synchronized (currentStateLock) {
            if (currentState == ConnectionState.READY) {
                isReady = true;
                currentState = ConnectionState.CONNECTING;
            }
        }
        if (isReady) {
            if (!device.isConnected() && !connectedDevices.containsKey(device.getAddress())) {
                finishScanning();
                stopScanningTimeOut();
                startConnectThread(device);
            } else {
                onError(device, OneSheeldError.ALREADY_CONNECTED_TO_THAT_DEVICE);
                device.onError(OneSheeldError.ALREADY_CONNECTED_TO_THAT_DEVICE);
            }
        }
    }

    void onError(OneSheeldDevice device, OneSheeldError error) {
        for (OneSheeldErrorCallback errorCallback : errorCallbacks) {
            if (errorCallback != null)
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
            if (!handleBluetoothErrors()) {
                startConnection(device);
            }
        } else
            throw new OneSheeldException("OneSheeldDevice is null, have you checked its validity?");
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
            if (connectionCallback != null)
                connectionCallback.onDisconnect(device);
        }
    }

    void onConnect(OneSheeldDevice device) {
        while (connectedDevices.containsKey(device.getAddress()))
            connectedDevices.remove(device.getAddress());
        connectedDevices.put(device.getAddress(), device);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            if (connectionCallback != null)
                connectionCallback.onConnect(device);
        }
    }

    private synchronized void onConnectionStart(OneSheeldDevice device, BluetoothSocket socket) {
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
            throw new OneSheeldException("1Sheeld SDK not initialized. Have you called OneSheeldSdk.init()?");
        } else if (OneSheeldSdk.getContext() != null && (OneSheeldSdk.getContext().checkCallingOrSelfPermission("android.permission.BLUETOOTH") !=
                PackageManager.PERMISSION_GRANTED || OneSheeldSdk.getContext().checkCallingOrSelfPermission("android.permission.BLUETOOTH_ADMIN") != PackageManager.PERMISSION_GRANTED)) {
            throw new OneSheeldException("Bluetooth permissions are missing. Have you added them to the manifest?");
        } else if (!BluetoothUtils.doesDeviceHasBluetooth()) {
            onError(null, OneSheeldError.BLUETOOTH_NOT_SUPPORTED);
            return true;
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
            for (OneSheeldScanningCallback scanningCallback : scanningCallbacks) {
                if (scanningCallback != null)
                    scanningCallback.onStartScan();
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
                        if (scanningCallback != null)
                            scanningCallback.onDeviceFind(d);
                    }
                }
            }
            for (OneSheeldScanningCallback scanningCallback : scanningCallbacks) {
                if (scanningCallback != null)
                    scanningCallback.onFinishScan(new ArrayList<>(foundOneSheeldDevices.values()));
            }
            foundOneSheeldDevices.clear();
            otherBluetoothDevices.clear();
        }
    }

    private void registerBluetoothBroadcast() {
        if (OneSheeldSdk.getContext() != null) {
            try {
                OneSheeldSdk.getContext().unregisterReceiver(bluetoothBroadcastReceiver);
            } catch (IllegalArgumentException e) {
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
            } catch (IllegalArgumentException e) {
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
                        if (scanningCallback != null)
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
            if (connectionCallback != null)
                connectionCallback.onConnectionRetry(device, retryCount);
        }
        if (device != null) device.onConnectionRetry(retryCount);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket = null;
        private OneSheeldDevice device;

        public ConnectThread(OneSheeldDevice device) {
            this.device = device;
            setName("OneSheeldConnectThread: " + device.getAddress());
        }

        public void run() {
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
            // Start the onConnectionStart thread
            if (hasError) onConnectionError(device);
            else {
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
