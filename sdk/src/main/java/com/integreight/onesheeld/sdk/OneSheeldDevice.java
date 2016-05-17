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
import android.os.Build;
import android.os.SystemClock;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a hardware 1Sheeld board.
 * <p>It is responsible for all operations and communications with the board.</p>
 *
 * @see OneSheeldManager
 */
public class OneSheeldDevice {
    /**
     * The input pin mode.
     */
    public static final byte INPUT = 0;
    /**
     * The output pin mode.
     */
    public static final byte OUTPUT = 1;
    /**
     * The pwm pin mode.
     */
    public static final byte PWM = 3;
    /**
     * A name for pin 14
     */
    public static final int A0 = 14;
    /**
     * A name for pin 15
     */
    public static final int A1 = 15;
    /**
     * A name for pin 16
     */
    public static final int A2 = 16;
    /**
     * A name for pin 17
     */
    public static final int A3 = 17;
    /**
     * A name for pin 18
     */
    public static final int A4 = 18;
    /**
     * A name for pin 19
     */
    public static final int A5 = 19;
    private final char MAX_DATA_BYTES = 4096;
    private final char MAX_OUTPUT_BYTES = 32;
    private final byte DIGITAL_MESSAGE = (byte) 0x90;
    private final byte ANALOG_MESSAGE = (byte) 0xE0;
    private final byte REPORT_DIGITAL = (byte) 0xD0;
    private final byte SET_PIN_MODE = (byte) 0xF4;
    private final byte REPORT_VERSION = (byte) 0xF9;
    private final byte START_SYSEX = (byte) 0xF0;
    private final byte END_SYSEX = (byte) 0xF7;
    private final byte SET_BAUD_RATE = (byte) 0x5B;
    private final byte QUERY_BAUD_RATE = (byte) 0x5C;
    private final byte BOARD_TESTING = (byte) 0x5D;
    private final byte BOARD_RENAMING = (byte) 0x5E;
    private final byte REPORT_INPUT_PINS = (byte) 0x5F;
    private final byte RESET_MICRO = (byte) 0x60;
    private final byte BLUETOOTH_RESET = (byte) 0x61;
    private final byte IS_ALIVE = (byte) 0x62;
    private final byte MUTE_FIRMATA = (byte) 0x64;
    private final byte SERIAL_DATA = (byte) 0x66;
    private final byte CONFIGURATION_SHIELD_ID = (byte) 0x00;
    private final byte BT_CONNECTED = (byte) 0x01;
    private final byte LIBRARY_TESTING_CHALLENGE_REQUEST = (byte) 0x05;
    private final byte LIBRARY_TESTING_CHALLENGE_RESPONSE = (byte) 0x05;
    private final byte QUERY_LIBRARY_VERSION = (byte) 0x03;
    private final byte LIBRARY_VERSION_RESPONSE = (byte) 0x01;
    private final byte IS_HARDWARE_CONNECTED_QUERY = (byte) 0x02;
    private final byte IS_CALLBACK_ENTERED = (byte) 0x03;
    private final byte IS_CALLBACK_EXITED = (byte) 0x04;
    private final Object sendingDataLock = new Object();
    private final Object arduinoCallbacksLock = new Object();
    private final Object isConnectedLock = new Object();
    private final int MAX_RENAMING_RETRIES_NUMBER = 2;
    private final int MAX_FIRMWARE_UPDATING_RETRIES = 4;
    private final byte SOH = 0x01;
    private final byte STX = 0x02;
    private final byte EOT = 0x04;
    private final byte ACK = 0x06;
    private final byte NAK = 0x15;
    private final byte CAN = 0x18;
    private final byte CRC = 0x43; //C
    private final byte SUB = 0x1A;
    private final byte KEY[] = {(byte) 0x64, (byte) 0x0E, (byte) 0x1C, (byte) 0x39, (byte) 0x14, (byte) 0x28, (byte) 0x57, (byte) 0xAA};
    private final Object processInputLock = new Object();
    private final Object isUpdatingFirmwareLock = new Object();
    private final Object bluetoothBufferLock = new Object();
    private Queue<ShieldFrame> queuedFrames;
    private LinkedBlockingQueue<Byte> bluetoothBuffer;
    private LinkedBlockingQueue<Byte> serialBuffer;
    private LinkedBlockingQueue<Byte> firmwareUpdateBuffer;
    private BluetoothBufferListeningThread bluetoothBufferListeningThread;
    private SerialBufferListeningThread serialBufferListeningThread;
    private String name;
    private String address;
    private boolean isPaired;
    private BluetoothDevice bluetoothDevice;
    private ConnectedThread connectedThread;
    private volatile boolean isBluetoothBufferWaiting;
    private volatile boolean isSerialBufferWaiting;
    private TimeOut ShieldFrameTimeout;
    private boolean isConnected;
    private OneSheeldManager manager;
    private CopyOnWriteArrayList<OneSheeldConnectionCallback> connectionCallbacks;
    private CopyOnWriteArrayList<OneSheeldErrorCallback> errorCallbacks;
    private CopyOnWriteArrayList<OneSheeldDataCallback> dataCallbacks;
    private CopyOnWriteArrayList<OneSheeldVersionQueryCallback> versionQueryCallbacks;
    private CopyOnWriteArrayList<OneSheeldTestingCallback> testingCallbacks;
    private CopyOnWriteArrayList<OneSheeldRenamingCallback> renamingCallbacks;
    private CopyOnWriteArrayList<OneSheeldBaudRateQueryCallback> baudRateQueryCallbacks;
    private CopyOnWriteArrayList<OneSheeldFirmwareUpdateCallback> firmwareUpdateCallbacks;
    private int arduinoLibraryVersion;
    private Thread exitingCallbacksThread, enteringCallbacksThread;
    private TimeOut callbacksTimeout;
    private long lastTimeCallbacksExited;
    private int waitForData;
    private byte executeMultiByteCommand;
    private byte multiByteChannel;
    private byte[] storedInputData;
    private boolean parsingSysex;
    private int sysexBytesRead;
    private int majorVersion;
    private int minorVersion;
    private boolean isFirmwareVersionQueried;
    private boolean isLibraryVersionQueried;
    private boolean isInACallback;
    private boolean isMuted;
    private int[] digitalOutputData = {0, 0, 0};
    private int[] digitalInputData = {0, 0, 0};
    private boolean isPinDebuggingEnabled;
    private boolean isTypePlus;
    private byte correctTestingChallengeAnswer;
    private boolean hasFirmwareTestStarted;
    private boolean hasLibraryTestStarted;
    private TimeOut firmwareTestingTimeout;
    private TimeOut libraryTestingTimeout;
    private TimeOut renamingBoardTimeout;
    private int renamingRetries;
    private String pendingName;
    private boolean hasRenamingStarted;
    private SupportedBaudRate currentBaudRate;
    private boolean isBaudRateQueried;
    private boolean isUpdatingFirmware;
    private TimeOut firmwareUpdatingTimeOut;
    private Thread firmwareUpdatingThread;
    private AtomicBoolean neglectNextBluetoothResetFrame;


    /**
     * Instantiates a new <tt>OneSheeldDevice</tt> with a specific address.
     * <p>The name of the device will be the same as the address.</p>
     *
     * @param address the Bluetooth address of the device
     * @throws InvalidBluetoothAddressException if the address is incorrect
     * @see InvalidBluetoothAddressException
     */
    public OneSheeldDevice(String address) {
        checkBluetoothAddress(address);
        this.name = address;
        this.address = address;
        this.isPaired = false;
        this.isTypePlus = false;
        initialize();
    }

    /**
     * Instantiates a new <tt>OneSheeldDevice</tt> with a specific address.
     * <p>The name of the device will be the same as the address.</p>
     *
     * @param address    the Bluetooth address of the device
     * @param isTypePlus explicitly specify if this 1Sheeld is the plus version (BLE)
     * @throws InvalidBluetoothAddressException if the address is incorrect
     * @see InvalidBluetoothAddressException
     */
    public OneSheeldDevice(String address, boolean isTypePlus) {
        checkBluetoothAddress(address);
        this.name = address;
        this.address = address;
        this.isPaired = false;
        this.isTypePlus = isTypePlus;
        initialize();
    }

    /**
     * Instantiates a new <tt>OneSheeldDevice</tt> with a specific name and address.
     *
     * @param address the Bluetooth address of the device
     * @param name    the name of the device
     * @throws InvalidBluetoothAddressException if the address is incorrect
     * @see InvalidBluetoothAddressException
     */
    public OneSheeldDevice(String address, String name) {
        checkBluetoothAddress(address);
        this.name = name;
        this.address = address;
        this.isPaired = false;
        this.isTypePlus = false;
        initialize();
    }

    /**
     * Instantiates a new <tt>OneSheeldDevice</tt> with a specific name and address.
     *
     * @param address    the Bluetooth address of the device
     * @param name       the name of the device
     * @param isTypePlus explicitly specify if this 1Sheeld is the plus version (BLE)
     * @throws InvalidBluetoothAddressException if the address is incorrect
     * @see InvalidBluetoothAddressException
     */
    public OneSheeldDevice(String address, String name, boolean isTypePlus) {
        checkBluetoothAddress(address);
        this.name = name;
        this.address = address;
        this.isPaired = false;
        this.isTypePlus = isTypePlus;
        initialize();
    }

    OneSheeldDevice(String address, String name, boolean isPaired, boolean isTypePlus) {
        checkBluetoothAddress(address);
        this.name = name;
        this.address = address;
        this.isPaired = isPaired;
        this.isTypePlus = isTypePlus;
        initialize();
    }

    private void initialize() {
        bluetoothDevice = BluetoothUtils.getBluetoothAdapter().getRemoteDevice(address);
        isConnected = false;
        bluetoothBuffer = new LinkedBlockingQueue<>();
        serialBuffer = new LinkedBlockingQueue<>();
        firmwareUpdateBuffer = new LinkedBlockingQueue<>();
        manager = OneSheeldManager.getInstance();
        connectionCallbacks = new CopyOnWriteArrayList<>();
        errorCallbacks = new CopyOnWriteArrayList<>();
        dataCallbacks = new CopyOnWriteArrayList<>();
        versionQueryCallbacks = new CopyOnWriteArrayList<>();
        testingCallbacks = new CopyOnWriteArrayList<>();
        renamingCallbacks = new CopyOnWriteArrayList<>();
        baudRateQueryCallbacks = new CopyOnWriteArrayList<>();
        firmwareUpdateCallbacks = new CopyOnWriteArrayList<>();
        queuedFrames = new ConcurrentLinkedQueue<>();
        isMuted = false;
        arduinoLibraryVersion = 0;
        majorVersion = 0;
        minorVersion = 0;
        isFirmwareVersionQueried = false;
        isLibraryVersionQueried = false;
        sysexBytesRead = 0;
        parsingSysex = false;
        waitForData = 0;
        executeMultiByteCommand = 0;
        multiByteChannel = 0;
        storedInputData = new byte[MAX_DATA_BYTES];
        isPinDebuggingEnabled = false;
        correctTestingChallengeAnswer = 0;
        hasFirmwareTestStarted = false;
        hasLibraryTestStarted = false;
        hasRenamingStarted = false;
        renamingRetries = MAX_RENAMING_RETRIES_NUMBER;
        currentBaudRate = SupportedBaudRate._115200;
        isBaudRateQueried = false;
        isUpdatingFirmware = false;
        neglectNextBluetoothResetFrame = new AtomicBoolean(false);
    }

    /**
     * Sets the pin debugging logging messages.
     * <p>The OneSheeldSdk.setDebugging() should be enabled first</p>
     * <p>This includes huge messages if the 1Sheeld pins are floating.</p>
     *
     * @param isPinDebuggingEnabled the required state of the flag
     */
    public void setPinsDebugging(boolean isPinDebuggingEnabled) {
        this.isPinDebuggingEnabled = isPinDebuggingEnabled;
    }

    /**
     * Checks whether the pin debugging logging messages is enabled or not.
     *
     * @return the boolean
     */
    public boolean isPinDebuggingEnabled() {
        return isPinDebuggingEnabled;
    }

    /**
     * Checks whether the firmware updating is running or not.
     *
     * @return the boolean
     */
    public boolean isUpdatingFirmware() {
        synchronized (isUpdatingFirmwareLock) {
            return isUpdatingFirmware;
        }
    }

    private void stopBuffersThreads() {
        if (serialBufferListeningThread != null) {
            serialBufferListeningThread.stopRunning();
        }
        if (bluetoothBufferListeningThread != null) {
            bluetoothBufferListeningThread.stopRunning();
        }
    }

    /**
     * Add a connection callback.
     *
     * @param connectionCallback the connection callback
     * @see OneSheeldConnectionCallback
     */
    public void addConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && !connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.add(connectionCallback);
    }

    /**
     * Add a data callback.
     *
     * @param dataCallback the data callback
     * @see OneSheeldDataCallback
     */
    public void addDataCallback(OneSheeldDataCallback dataCallback) {
        if (dataCallback != null && !dataCallbacks.contains(dataCallback))
            dataCallbacks.add(dataCallback);
    }

    /**
     * Add a version query callback.
     *
     * @param versionQueryCallback the version query callback
     * @see OneSheeldVersionQueryCallback
     */
    public void addVersionQueryCallback(OneSheeldVersionQueryCallback versionQueryCallback) {
        if (versionQueryCallback != null && !versionQueryCallbacks.contains(versionQueryCallback))
            versionQueryCallbacks.add(versionQueryCallback);
    }

    /**
     * Add an error callback.
     *
     * @param errorCallback the error callback
     * @see OneSheeldErrorCallback
     */
    public void addErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && !errorCallbacks.contains(errorCallback))
            errorCallbacks.add(errorCallback);
    }

    /**
     * Add a testing callback.
     *
     * @param testingCallback the testing callback
     * @see OneSheeldTestingCallback
     */
    public void addTestingCallback(OneSheeldTestingCallback testingCallback) {
        if (testingCallback != null && !testingCallbacks.contains(testingCallback))
            testingCallbacks.add(testingCallback);
    }

    /**
     * Add a renaming callback.
     *
     * @param renamingCallback the renaming callback
     * @see OneSheeldRenamingCallback
     */
    public void addRenamingCallback(OneSheeldRenamingCallback renamingCallback) {
        if (renamingCallback != null && !renamingCallbacks.contains(renamingCallback))
            renamingCallbacks.add(renamingCallback);
    }

    /**
     * Add a baud rate query callback.
     *
     * @param baudRateQueryCallback the baud rate query callback
     * @see OneSheeldBaudRateQueryCallback
     */
    public void addBaudRateQueryCallback(OneSheeldBaudRateQueryCallback baudRateQueryCallback) {
        if (baudRateQueryCallback != null && !baudRateQueryCallbacks.contains(baudRateQueryCallback))
            baudRateQueryCallbacks.add(baudRateQueryCallback);
    }

    /**
     * Add a firmware update callback.
     *
     * @param firmwareUpdateCallback the firmware update callback
     * @see OneSheeldFirmwareUpdateCallback
     */
    public void addFirmwareUpdateCallback(OneSheeldFirmwareUpdateCallback firmwareUpdateCallback) {
        if (firmwareUpdateCallback != null && !firmwareUpdateCallbacks.contains(firmwareUpdateCallback))
            firmwareUpdateCallbacks.add(firmwareUpdateCallback);
    }

    /**
     * Remove a connection callback.
     *
     * @param connectionCallback the connection callback
     * @see OneSheeldConnectionCallback
     */
    public void removeConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.remove(connectionCallback);
    }

    /**
     * Remove an error callback.
     *
     * @param errorCallback the error callback
     * @see OneSheeldErrorCallback
     */
    public void removeErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && errorCallbacks.contains(errorCallback))
            errorCallbacks.remove(errorCallback);
    }

    /**
     * Remove a renaming callback.
     *
     * @param renamingCallback the renaming callback
     * @see OneSheeldRenamingCallback
     */
    public void removeRenamingCallback(OneSheeldRenamingCallback renamingCallback) {
        if (renamingCallback != null && renamingCallbacks.contains(renamingCallback))
            renamingCallbacks.remove(renamingCallback);
    }

    /**
     * Remove a testing callback.
     *
     * @param testingCallback the testing callback
     * @see OneSheeldTestingCallback
     */
    public void removeTestingCallback(OneSheeldTestingCallback testingCallback) {
        if (testingCallback != null && testingCallbacks.contains(testingCallback))
            testingCallbacks.remove(testingCallback);
    }

    /**
     * Remove a data callback.
     *
     * @param dataCallback the data callback
     * @see OneSheeldDataCallback
     */
    public void removeDataCallback(OneSheeldDataCallback dataCallback) {
        if (dataCallback != null && dataCallbacks.contains(dataCallback))
            dataCallbacks.remove(dataCallback);
    }

    /**
     * Remove a version query callback.
     *
     * @param versionQueryCallback the version query callback
     * @see OneSheeldVersionQueryCallback
     */
    public void removeVersionQueryCallback(OneSheeldVersionQueryCallback versionQueryCallback) {
        if (versionQueryCallback != null && versionQueryCallbacks.contains(versionQueryCallback))
            versionQueryCallbacks.remove(versionQueryCallback);
    }

    /**
     * Remove a baud rate query callback.
     *
     * @param baudRateQueryCallback the baud rate query callback
     * @see OneSheeldBaudRateQueryCallback
     */
    public void removeBaudRateQueryCallback(OneSheeldBaudRateQueryCallback baudRateQueryCallback) {
        if (baudRateQueryCallback != null && baudRateQueryCallbacks.contains(baudRateQueryCallback))
            baudRateQueryCallbacks.remove(baudRateQueryCallback);
    }

    /**
     * Remove a firmware update callback.
     *
     * @param firmwareUpdateCallback the firmware update callback
     * @see OneSheeldFirmwareUpdateCallback
     */
    public void removeFirmwareUpdateCallback(OneSheeldFirmwareUpdateCallback firmwareUpdateCallback) {
        if (firmwareUpdateCallback != null && firmwareUpdateCallbacks.contains(firmwareUpdateCallback))
            firmwareUpdateCallbacks.remove(firmwareUpdateCallback);
    }

    /**
     * Removes all connection, data, version query, board testing, renaming, baud rate query, and firmware update callbacks.
     */
    public void removeAllCallbacks() {
        removeAllConnectionCallbacks();
        removeAllDataCallbacks();
        removeAllVersionQueryCallbacks();
        removeAllTestingCallbacks();
        removeAllRenamingCallbacks();
        removeAllBaudRateQueryCallbacks();
        removeAllErrorCallbacks();
        removeAllFirmwareUpdateCallbacks();
    }

    /**
     * Removes all connection callbacks.
     */
    public void removeAllConnectionCallbacks() {
        connectionCallbacks.clear();
    }

    /**
     * Removes all data callbacks.
     */
    public void removeAllDataCallbacks() {
        dataCallbacks.clear();
    }

    /**
     * Removes all error callbacks.
     */
    public void removeAllErrorCallbacks() {
        errorCallbacks.clear();
    }

    /**
     * Removes all version query callbacks.
     */
    public void removeAllVersionQueryCallbacks() {
        versionQueryCallbacks.clear();
    }

    /**
     * Removes all board testing callbacks.
     */
    public void removeAllTestingCallbacks() {
        testingCallbacks.clear();
    }

    /**
     * Removes all board renaming callbacks.
     */
    public void removeAllRenamingCallbacks() {
        renamingCallbacks.clear();
    }

    /**
     * Removes all baud rate query callbacks.
     */
    public void removeAllBaudRateQueryCallbacks() {
        baudRateQueryCallbacks.clear();
    }

    /**
     * Removes all firmware update callbacks.
     */
    public void removeAllFirmwareUpdateCallbacks() {
        firmwareUpdateCallbacks.clear();
    }


    private void clearAllBuffers() {
        synchronized (bluetoothBufferLock) {
            bluetoothBuffer.clear();

        }
        serialBuffer.clear();
        firmwareUpdateBuffer.clear();
    }

    private byte readByteFromSerialBuffer() throws InterruptedException,
            ShieldFrameNotComplete {
        if (ShieldFrameTimeout != null && ShieldFrameTimeout.isTimeout())
            throw new ShieldFrameNotComplete();
        isSerialBufferWaiting = true;
        byte temp = serialBuffer.take();
        if (ShieldFrameTimeout != null)
            ShieldFrameTimeout.resetTimer();
        return temp;
    }

    private byte readByteFromBluetoothBuffer() throws InterruptedException {
        isBluetoothBufferWaiting = true;
        return bluetoothBuffer.take();
    }

    private void checkBluetoothAddress(String address) {
        if (address == null || !BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new InvalidBluetoothAddressException("Bluetooth address is invalid, are you sure you specified it correctly?");
        }
    }

    BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * Gets the name of the device.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the address of the device.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Is the device paired.
     *
     * @return the boolean
     */
    public boolean isPaired() {
        return isPaired;
    }

    void setPaired(boolean isPaired) {
        this.isPaired = isPaired;
    }

    synchronized void connectUsing(OneSheeldConnection connection) {
        try {
            closeConnection();
            connectedThread = new ConnectedThread(connection);
            connectedThread.start();
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    private void onConnect() {
        manager.onConnect(OneSheeldDevice.this);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onConnect(OneSheeldDevice.this);
        }
    }

    private void onDisconnect() {
        manager.onDisconnect(OneSheeldDevice.this);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onDisconnect(OneSheeldDevice.this);
        }
    }

    void onError(OneSheeldError error) {
        for (OneSheeldErrorCallback errorCallback : errorCallbacks) {
            errorCallback.onError(OneSheeldDevice.this, error);
        }
    }

    void onConnectionRetry(int retryCount) {
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onConnectionRetry(OneSheeldDevice.this, retryCount);
        }
    }

    private void resetProcessInput() {
        synchronized (processInputLock) {
            waitForData = 0;
            executeMultiByteCommand = 0;
            multiByteChannel = 0;
            storedInputData = new byte[MAX_DATA_BYTES];
            parsingSysex = false;
            sysexBytesRead = 0;
        }
    }

    private byte[] getByteAs2SevenBitsBytes(byte data) {
        byte[] temp = new byte[2];
        temp[0] = (byte) ((data & 0xFF) & 127);
        temp[1] = (byte) (((data & 0xFF) >> 7) & 127);
        return temp;
    }

    private byte[] getByteArrayAs2SevenBitsBytesArray(byte[] data) {
        byte[] temp = new byte[data.length * 2];
        for (int i = 0; i < temp.length; i += 2) {
            temp[i] = getByteAs2SevenBitsBytes(data[i / 2])[0];
            temp[i + 1] = getByteAs2SevenBitsBytes(data[i / 2])[1];
        }
        return temp;
    }

    private void sysex(byte command, byte[] bytes) {
        byte[] data = getByteArrayAs2SevenBitsBytesArray(bytes);
        if (data.length > 32)
            return;
        byte[] writeData = new byte[data.length + 3];
        writeData[0] = START_SYSEX;
        writeData[1] = command;
        for (int i = 0; i < data.length; i++) {
            writeData[i + 2] = (byte) (data[i] & 127); // 7bit
        }
        writeData[writeData.length - 1] = END_SYSEX;
        write(writeData);
    }

    private void notifyHardwareOfConnection() {
        Log.i("Device " + this.name + ": Notifying the board with connection.");
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, BT_CONNECTED), true);
    }


    /**
     * Explicitly queue a shield frame for sending after the Arduino exits the callback.
     *
     * @param frame the frame
     * @throws NullPointerException if the passed frame is null
     */
    public void queueShieldFrame(ShieldFrame frame) {
        if (frame == null)
            throw new NullPointerException("The passed frame is null, have you checked its validity?");
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        queuedFrames.add(frame);
        callbackEntered();
    }

    private void callbackEntered() {
        if (enteringCallbacksThread != null && enteringCallbacksThread.isAlive())
            return;
        enteringCallbacksThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (arduinoCallbacksLock) {
                    isInACallback = true;
                }
                if (callbacksTimeout == null || !callbacksTimeout.isAlive()) {
                    callbacksTimeout = new TimeOut(5000, 1000, new TimeOut.TimeOutCallback() {
                        @Override
                        public void onTimeOut() {
                            callbackExited();
                        }

                        @Override
                        public void onTick(long milliSecondsLeft) {

                        }
                    });
                } else
                    callbacksTimeout.resetTimer();
            }
        });
        enteringCallbacksThread.start();
    }

    private void callbackExited() {
        synchronized (arduinoCallbacksLock) {
            isInACallback = false;
            lastTimeCallbacksExited = SystemClock.elapsedRealtime();
        }
        if (callbacksTimeout != null && !callbacksTimeout.isAlive()) callbacksTimeout.stopTimer();
        if (exitingCallbacksThread != null && exitingCallbacksThread.isAlive())
            return;
        exitingCallbacksThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sent;
                while (!queuedFrames.isEmpty()) {
                    sent = false;
                    synchronized (arduinoCallbacksLock) {
                        if (!isInACallback && lastTimeCallbacksExited != 0 && (SystemClock.elapsedRealtime() - lastTimeCallbacksExited > 200)) {
                            sendFrame(queuedFrames.poll());
                            sent = true;
                        }
                    }
                    if (sent) try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        exitingCallbacksThread.start();
    }

    /**
     * Send a shield frame or queue it if Arduino is in a callback.
     *
     * @param frame             the frame
     * @param waitIfInACallback if true the frame will be queued till Arduino exits its callback
     * @throws NullPointerException if passed frame is null
     */
    public void sendShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        if (frame == null)
            throw new NullPointerException("The passed frame is null, have you checked its validity?");
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }

        if (!waitIfInACallback) {
            sendFrame(frame);
            return;
        }

        boolean inACallback;

        synchronized (arduinoCallbacksLock) {
            inACallback = isInACallback;
        }

        if (inACallback) {
            queuedFrames.add(frame);
        } else {
            if (queuedFrames.isEmpty()) {
                sendFrame(frame);
            } else {
                queuedFrames.add(frame);
            }
        }
    }

    /**
     * Send shield frame without waiting.
     *
     * @param frame the frame
     * @throws NullPointerException if passed frame is null
     */
    public void sendShieldFrame(ShieldFrame frame) {
        sendShieldFrame(frame, false);
    }

    private void sendFrame(ShieldFrame frame) {
        if (frame == null) return;
        byte[] frameBytes = frame.getAllFrameAsBytes();
        sendData(frameBytes);
        Log.i("Device " + this.name + ": Frame sent, values: " + frame + ".");
    }

    private void respondToIsAlive() {
        synchronized (sendingDataLock) {
            sysex(IS_ALIVE, new byte[]{});
        }
    }

    /**
     * Send raw serial data.
     *
     * @param data the data
     * @throws NullPointerException if the passed data array is null
     */
    public void sendSerialData(byte[] data) {
        if (data == null)
            throw new NullPointerException("The passed data is null, have you checked its validity?");

        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        sendData(data);
        Log.i("Device " + this.name + ": Serial data sent, values: " + ArrayUtils.toHexString(data) + ".");
    }

    private void sendData(byte[] data) {
        int maxShieldFrameBytes = (MAX_OUTPUT_BYTES - 3) / 2;
        ArrayList<byte[]> subArrays = new ArrayList<>();
        for (int i = 0; i < data.length; i += maxShieldFrameBytes) {
            byte[] subArray = (i + maxShieldFrameBytes > data.length) ? ArrayUtils
                    .copyOfRange(data, i, data.length) : ArrayUtils
                    .copyOfRange(data, i, i + maxShieldFrameBytes);
            subArrays.add(subArray);
        }
        synchronized (sendingDataLock) {
            for (byte[] sub : subArrays)
                sysex(SERIAL_DATA, sub);
        }
    }

    private void enableReporting() {
        Log.i("Device " + this.name + ": Enable digital pins reporting.");
        synchronized (sendingDataLock) {
            for (byte i = 0; i < 3; i++) {
                write(new byte[]{(byte) (REPORT_DIGITAL | i), 1});
            }
        }
    }

    private void queryInputPinsValues() {
        Log.i("Device " + this.name + ": Query the current status of the pins.");
        synchronized (sendingDataLock) {
            sysex(REPORT_INPUT_PINS, new byte[]{});
        }
    }

    /**
     * Rename the board. Note: if the new name does not have "1Sheeld" in its name, it will not be visible in any future scans by the SDK.
     *
     * @param name the new name
     * @return true if the renaming request initiated successfully.
     * @throws NullPointerException if the passed name is null or zero length
     * @see OneSheeldRenamingCallback
     */
    public boolean rename(String name) {
        if (name == null || name.length() <= 0)
            throw new NullPointerException("The passed name is invalid, have you checked its validity?");
        else if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return false;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return false;
        } else if (hasRenamingStarted) {
            Log.i("Device " + this.name + ": Device is in the middle of another renaming request.");
            return false;
        }
        renamingRetries = MAX_RENAMING_RETRIES_NUMBER;
        hasRenamingStarted = true;
        return sendRenamingRequest(name);
    }

    private boolean sendRenamingRequest(String name) {
        if (name == null || name.length() <= 0) return false;
        if (isTypePlus()) name = (name.length() > 11) ? name.substring(0, 11) : name;
        else name = (name.length() > 14) ? name.substring(0, 14) : name;
        Log.i("Device " + this.name + ": Trying to rename the device to \"" + name + "\".");
        pendingName = name;
        synchronized (sendingDataLock) {
            sysex(BOARD_RENAMING, name.getBytes(Charset.forName("US-ASCII")));
        }
        initRenamingBoardTimeOut();
        return true;
    }

    /**
     * Test the board, and make sure the firmware and library are working correctly.
     *
     * @return true if the test sequence initiated successfully.
     * @see OneSheeldTestingCallback
     */
    public boolean test() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return false;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return false;
        } else if (hasFirmwareTestStarted || hasLibraryTestStarted) {
            Log.i("Device " + this.name + ": device is in the middle of another test.");
            return false;
        }
        Log.i("Device " + this.name + ": Testing the device, both firmware and library.");
        String currentMillis = String.valueOf(System.currentTimeMillis());
        byte[] bytes = currentMillis.getBytes(Charset.forName("US-ASCII"));
        int correctAnswer = 0;
        for (byte byteValue : bytes) {
            correctAnswer += (byteValue & 0xFF);

        }
        correctTestingChallengeAnswer = (byte) (correctAnswer % 256);
        hasFirmwareTestStarted = true;
        hasLibraryTestStarted = true;
        ShieldFrame testingFrame = new ShieldFrame(CONFIGURATION_SHIELD_ID, LIBRARY_TESTING_CHALLENGE_REQUEST);
        testingFrame.addArgument("Are you ok?");
        testingFrame.addArgument(bytes);
        synchronized (sendingDataLock) {
            sysex(BOARD_TESTING, bytes);
            sendShieldFrame(testingFrame);
        }
        initFirmwareTestingTimeOut();
        initLibraryTestingTimeOut();
        return true;
    }

    private void setAllPinsAsInput() {
        Log.i("Device " + this.name + ": Set all digital pins as input.");
        for (int i = 0; i < 20; i++) {
            pinMode(i, INPUT);
        }
    }


    /**
     * Digital read from a specific pin.
     *
     * @param pin the pin
     * @return the pin status
     */
    public boolean digitalRead(int pin) {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return false;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return false;
        }
        if (isPinDebuggingEnabled)
            Log.i("Device " + this.name + ": Digital read from pin " + pin + ".");
        if (pin >= 20 || pin < 0)
            throw new IncorrectPinException("The specified pin number is incorrect, are you sure you specified it correctly?");
        return getDigitalPinStatus(pin);
    }

    private boolean getDigitalPinStatus(int pin) {
        return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
    }

    /**
     * Changes a specific pin's mode.
     *
     * @param pin  the pin
     * @param mode the mode
     */
    public void pinMode(int pin, byte mode) {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        if (isPinDebuggingEnabled)
            Log.i("Device " + this.name + ": Change mode of pin " + pin + " to " + mode + ".");
        if (pin >= 20 || pin < 0)
            throw new IncorrectPinException("The specified pin number is incorrect, are you sure you specified it correctly?");
        byte[] writeData = {SET_PIN_MODE, (byte) pin, mode};
        synchronized (sendingDataLock) {
            write(writeData);
        }
    }

    /**
     * Digital write to a specific pin.
     *
     * @param pin   the pin
     * @param value the value
     */
    public void digitalWrite(int pin, boolean value) {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        if (isPinDebuggingEnabled)
            Log.i("Device " + this.name + ": Digital write " + (value ? "High" : "Low") + " to pin " + pin + ".");
        if (pin >= 20 || pin < 0)
            throw new IncorrectPinException("The specified pin number is incorrect, are you sure you specified it correctly?");
        byte portNumber = (byte) ((pin >> 3) & 0x0F);
        if (!value)
            digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
        else
            digitalOutputData[portNumber] |= (1 << (pin & 0x07));
        byte[] writeData = {SET_PIN_MODE, (byte) pin, OUTPUT,
                (byte) (DIGITAL_MESSAGE | portNumber),
                (byte) (digitalOutputData[portNumber] & 0x7F),
                (byte) (digitalOutputData[portNumber] >> 7)};
        synchronized (sendingDataLock) {
            write(writeData);
        }
    }

    /**
     * Analog write to a specific pin.
     *
     * @param pin   the pin
     * @param value the value
     */
    public void analogWrite(int pin, int value) {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        if (isPinDebuggingEnabled)
            Log.i("Device " + this.name + ": Analog write " + value + " to pin " + pin + ".");
        if (pin >= 20 || pin < 0)
            throw new IncorrectPinException("The specified pin number is incorrect, are you sure you specified it correctly?");
        byte[] writeData = {SET_PIN_MODE, (byte) pin, PWM,
                (byte) (ANALOG_MESSAGE | (pin & 0x0F)), (byte) (value & 0x7F),
                (byte) (value >> 7)};
        synchronized (sendingDataLock) {
            write(writeData);
        }
    }

    private void setDigitalInputs(int portNumber, int portData) {
        int portDifference = digitalInputData[portNumber] ^ portData;
        digitalInputData[portNumber] = portData;
        ArrayList<Integer> differentPinNumbers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            if (BitsUtils.isBitSet((byte) portDifference, i)) differentPinNumbers.add(i);
        }

        for (int pinNumber : differentPinNumbers) {
            int actualPinNumber = (portNumber << 3) + pinNumber;
            if (isPinDebuggingEnabled)
                Log.i("Device " + this.name + ": Pin #" + actualPinNumber + " status changed to " + (getDigitalPinStatus(actualPinNumber) ? "High" : "Low") + ".");
            for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
                oneSheeldDataCallback.onDigitalPinStatusChange(OneSheeldDevice.this, actualPinNumber, getDigitalPinStatus(actualPinNumber));
            }
        }
    }

    /**
     * Query the firmware version.
     *
     * @see OneSheeldVersionQueryCallback
     */
    public void queryFirmwareVersion() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        Log.i("Device " + this.name + ": Query firmware version.");
        isFirmwareVersionQueried = false;
        sendFirmwareVersionQueryFrame();
    }

    private void sendFirmwareVersionQueryFrame() {
        synchronized (sendingDataLock) {
            write(new byte[]{REPORT_VERSION});
        }
    }

    /**
     * Query the library version.
     *
     * @see OneSheeldVersionQueryCallback
     */
    public void queryLibraryVersion() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        Log.i("Device " + this.name + ": Query library version.");
        isLibraryVersionQueried = false;
        sendLibraryVersionQueryFrame();
    }

    private void sendLibraryVersionQueryFrame() {
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, QUERY_LIBRARY_VERSION), true);
    }


    /**
     * Checks whether the device responded to the firmware version query.
     *
     * @return the boolean
     */
    public boolean hasRespondedToFirmwareVersionQuery() {
        return isFirmwareVersionQueried;
    }

    /**
     * Checks whether the device responded to the library version query.
     *
     * @return the boolean
     */
    public boolean hasRespondedToLibraryVersionQuery() {
        return isLibraryVersionQueried;
    }

    /**
     * Checks whether the device responded to the current baud rate query.
     *
     * @return the boolean
     */
    public boolean hasRespondedToBaudRateQuery() {
        return isBaudRateQueried;
    }

    /**
     * gets the current baud rate.
     *
     * @return the current baud rate
     */
    public SupportedBaudRate getCurrentBaudRate() {
        return currentBaudRate;
    }

    private void write(byte[] writeData) {
        if (isConnected() && connectedThread != null && connectedThread.isAlive() && !isUpdatingFirmware())
            connectedThread.write(writeData);
    }

    private void write(byte writeData) {
        if (isConnected() && connectedThread != null && connectedThread.isAlive() && !isUpdatingFirmware())
            connectedThread.write(new byte[]{writeData});
    }

    private void writeByteForFirmwareUpdate(byte[] writeData) {
        if (isConnected() && connectedThread != null && connectedThread.isAlive())
            connectedThread.write(writeData);
    }

    private void writeByteForFirmwareUpdate(byte writeData) {
        if (isConnected() && connectedThread != null && connectedThread.isAlive())
            connectedThread.write(new byte[]{writeData});
    }

    private void initFirmware() {
        synchronized (isUpdatingFirmwareLock) {
            isUpdatingFirmware = false;
        }
        isBluetoothBufferWaiting = false;
        isSerialBufferWaiting = false;
        arduinoLibraryVersion = -1;
        isMuted = false;
        stopBuffersThreads();
        clearAllBuffers();
        resetProcessInput();
        bluetoothBufferListeningThread = new BluetoothBufferListeningThread();
        serialBufferListeningThread = new SerialBufferListeningThread();
        while (true) {
            if (isBluetoothBufferWaiting) break;
        }
        while (true) {
            if (isSerialBufferWaiting) break;
        }
        sendInitializationFrames();
    }

    private void sendInitializationFrames() {
        enableReporting();
        setAllPinsAsInput();
        queryInputPinsValues();
        respondToIsAlive();
        sendFirmwareVersionQueryFrame();
        sendUnMuteFrame();
        sendBaudRateQueryFrame();
        sendLibraryVersionQueryFrame();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        notifyHardwareOfConnection();
    }


    /**
     * Query the current baud rate of the device.
     *
     * @see OneSheeldBaudRateQueryCallback
     */
    public void queryBaudRate() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        isBaudRateQueried = false;
        sendBaudRateQueryFrame();
        Log.i("Device " + this.name + ": Baud rate queried.");
    }

    private void sendBaudRateQueryFrame() {
        synchronized (sendingDataLock) {
            sysex(QUERY_BAUD_RATE, new byte[]{});
        }
    }

    /**
     * Change the baud rate of the device.
     *
     * @param baudRate a supported baud rate.
     * @throws NullPointerException if the passed baud rate is null
     */
    public void setBaudRate(SupportedBaudRate baudRate) {
        if (baudRate == null)
            throw new NullPointerException("The passed baud rate is null, have you checked its validity?");
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        sendBaudRateSetFrame(baudRate.getFrameValue());
        currentBaudRate = baudRate;
        Log.i("Device " + this.name + ": Changed communications baud rate to " + baudRate.getBaudRate() + ".");
    }

    private void sendBaudRateSetFrame(byte baudRate) {
        byte randomVal = (byte) (Math.random() * 255);
        byte complement = (byte) (255 - randomVal & 0xFF);
        synchronized (sendingDataLock) {
            sysex(SET_BAUD_RATE, new byte[]{baudRate, randomVal, complement});
        }
    }

    /**
     * Mute all communications with the device.
     */
    public void mute() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        sendMuteFrame();
        Log.i("Device " + this.name + ": Communications muted.");
        isMuted = true;
    }

    private void sendMuteFrame() {
        synchronized (sendingDataLock) {
            sysex(MUTE_FIRMATA, new byte[]{1});
        }
    }

    private void sendUnMuteFrame() {
        synchronized (sendingDataLock) {
            sysex(MUTE_FIRMATA, new byte[]{0});
        }
    }

    /**
     * Unmute all communications with the device.
     */
    public void unMute() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        }
        sendUnMuteFrame();
        Log.i("Device " + this.name + ": Communications unmuted.");
        isMuted = false;
    }

    /**
     * Checks whether the device is muted or not.
     *
     * @return the boolean
     */
    public boolean isMuted() {
        return isMuted;
    }

    /**
     * Checks whether the Arduino is in a callback or not.
     *
     * @return the boolean
     */
    public boolean isArduinoInACallback() {
        synchronized (arduinoCallbacksLock) {
            return isInACallback;
        }
    }

    private void onSysex(byte command, byte[] data) {

    }

    private void onLibraryVersionQueryResponse(int version) {
        for (OneSheeldVersionQueryCallback versionQueryCallback : versionQueryCallbacks) {
            versionQueryCallback.onLibraryVersionQueryResponse(OneSheeldDevice.this, version);
        }
    }

    private void onFirmwareVersionQueryResponse(int majorVersion, int minorVersion) {
        for (OneSheeldVersionQueryCallback versionQueryCallback : versionQueryCallbacks) {
            versionQueryCallback.onFirmwareVersionQueryResponse(OneSheeldDevice.this, new FirmwareVersion(majorVersion, minorVersion));
        }
    }

    private void onBaudRateQueryResponse(SupportedBaudRate supportedBaudRate) {
        for (OneSheeldBaudRateQueryCallback baudRateQueryCallback : baudRateQueryCallbacks) {
            baudRateQueryCallback.onBaudRateQueryResponse(OneSheeldDevice.this, supportedBaudRate);
        }
    }

    private void setVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        Log.i("Device " + this.name + ": Device replied with firmware version, major: " + majorVersion + ", minor:" + minorVersion + ".");
        onFirmwareVersionQueryResponse(majorVersion, minorVersion);
    }

    /**
     * Gets the device firmware version.
     *
     * @return the firmware version
     */
    public FirmwareVersion getFirmwareVersion() {
        return new FirmwareVersion(majorVersion, minorVersion);
    }

    /**
     * Gets the Arudino library version.
     *
     * @return the library version
     */
    public int getLibraryVersion() {
        return arduinoLibraryVersion;
    }

    private void processInput(byte inputData) {
        byte command;
        if (parsingSysex) {
            if (inputData == END_SYSEX) {
                parsingSysex = false;
                byte sysexCommand = storedInputData[0];
                if (sysexBytesRead > 0) {
                    byte[] sysexData = new byte[sysexBytesRead - 1];

                    System.arraycopy(storedInputData, 1, sysexData, 0,
                            sysexBytesRead - 1);

                    byte[] fixedSysexData = null;
                    if (sysexData.length % 2 == 0) {
                        fixedSysexData = new byte[sysexData.length / 2];
                        for (int i = 0; i < sysexData.length; i += 2) {
                            fixedSysexData[i / 2] = (byte) (sysexData[i] | (sysexData[i + 1] << 7));
                        }

                        if (sysexCommand == SERIAL_DATA) {
                            for (byte b : fixedSysexData) {
                                serialBuffer.add(b);
                                for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
                                    oneSheeldDataCallback.onSerialDataReceive(OneSheeldDevice.this, b & 0xFF);
                                }
                            }
                        } else if (sysexCommand == BLUETOOTH_RESET) {
                            byte randomVal = (byte) (Math.random() * 255);
                            byte complement = (byte) (255 - randomVal & 0xFF);
                            synchronized (sendingDataLock) {
                                sysex(BLUETOOTH_RESET, new byte[]{(byte) (neglectNextBluetoothResetFrame.get() ? 0x00 : 0x01), randomVal, complement});
                            }
                            Log.i("Device " + this.name + ": Device requested Bluetooth reset" + (neglectNextBluetoothResetFrame.get() ? ", and it was neglected" : "") + ".");
                            if (!neglectNextBluetoothResetFrame.get()) closeConnection();
                            neglectNextBluetoothResetFrame.set(false);

                        } else if (sysexCommand == IS_ALIVE) {
                            respondToIsAlive();
                        } else if (sysexCommand == BOARD_TESTING) {
                            stopFirmwareTestingTimeOut();
                            hasFirmwareTestStarted = false;
                            boolean isPassed = fixedSysexData.length == 1 && fixedSysexData[0] == correctTestingChallengeAnswer;
                            if (isPassed)
                                Log.i("Device " + OneSheeldDevice.this.name + ": Firmware testing succeeded.");
                            else
                                Log.i("Device " + OneSheeldDevice.this.name + ": Firmware testing failed.");
                            if (isConnected()) {
                                for (OneSheeldTestingCallback oneSheeldTestingCallback : testingCallbacks)
                                    oneSheeldTestingCallback.onFirmwareTestResult(OneSheeldDevice.this, isPassed);
                            }
                        } else if (sysexCommand == BOARD_RENAMING) {
                            Log.i("Device " + this.name + ": Device received the renaming request successfully, it should be renamed to \"" + pendingName + "\" in a couple of seconds.");
                            this.name = pendingName;
                            hasRenamingStarted = false;
                            stopRenamingBoardTimeOut();
                            if (isConnected()) {
                                for (OneSheeldRenamingCallback renamingCallback : renamingCallbacks) {
                                    renamingCallback.onRenamingRequestReceivedSuccessfully(OneSheeldDevice.this);
                                }
                            }
                            closeConnection();
                        } else if (sysexCommand == QUERY_BAUD_RATE && fixedSysexData.length == 1) {
                            boolean isSupported = true;
                            switch (fixedSysexData[0]) {
                                case 0x01:
                                    currentBaudRate = SupportedBaudRate._9600;
                                    break;
                                case 0x02:
                                    currentBaudRate = SupportedBaudRate._14400;
                                    break;
                                case 0x03:
                                    currentBaudRate = SupportedBaudRate._19200;
                                    break;
                                case 0x04:
                                    currentBaudRate = SupportedBaudRate._28800;
                                    break;
                                case 0x05:
                                    currentBaudRate = SupportedBaudRate._38400;
                                    break;
                                case 0x06:
                                    currentBaudRate = SupportedBaudRate._57600;
                                    break;
                                case 0x07:
                                    currentBaudRate = SupportedBaudRate._115200;
                                    break;
                                default:
                                    isSupported = false;
                                    break;
                            }
                            isBaudRateQueried = true;
                            if (isSupported) {
                                Log.i("Device " + this.name + ": Device responded with baud rate: " + currentBaudRate.getBaudRate() + ".");
                                onBaudRateQueryResponse(currentBaudRate);
                            } else {
                                Log.i("Device " + this.name + ": Device responded with an unsupported baud rate.");
                                onBaudRateQueryResponse(null);
                            }
                        } else {
                            onSysex(sysexCommand, sysexData);
                        }
                    }
                } else {
                    onSysex(sysexCommand, new byte[]{});
                }

            } else {
                if (sysexBytesRead < storedInputData.length) {
                    storedInputData[sysexBytesRead] = inputData;
                    sysexBytesRead++;
                }
            }
        } else if (waitForData > 0 && (int) (inputData & 0xFF) < 128) {
            waitForData--;
            storedInputData[waitForData] = inputData;
            if (executeMultiByteCommand != 0 && waitForData == 0) {
                switch (executeMultiByteCommand) {
                    case DIGITAL_MESSAGE:
                        setDigitalInputs(multiByteChannel,
                                (storedInputData[0] << 7) + storedInputData[1]);
                        break;
                    case REPORT_VERSION:
                        setVersion(storedInputData[0], storedInputData[1]);
                        isFirmwareVersionQueried = true;
                        break;
                }
            }
        } else {
            if ((int) (inputData & 0xFF) < 0xF0) {
                command = (byte) (inputData & 0xF0);
                multiByteChannel = (byte) (inputData & 0x0F);
            } else {
                command = inputData;
            }
            switch (command) {
                case START_SYSEX:
                    parsingSysex = true;
                    sysexBytesRead = 0;
                    break;
                case DIGITAL_MESSAGE:
                case REPORT_VERSION:
                    waitForData = 2;
                    executeMultiByteCommand = command;
                    break;
            }
        }
    }

    /**
     * Disconnect the device.
     *
     * @see OneSheeldConnectionCallback
     */
    public void disconnect() {
        Log.i("Device " + this.name + ": Disconnection request received.");
        closeConnection();
    }

    /**
     * Connect to the device.
     *
     * @see OneSheeldConnectionCallback
     */
    public void connect() {
        Log.i("Device " + this.name + ": Delegate the connection request to the manager.");
        manager.connect(OneSheeldDevice.this);
    }

    /**
     * Checks whether the device is connected or not.
     *
     * @return the boolean
     */
    public boolean isConnected() {
        synchronized (isConnectedLock) {
            return isConnected;
        }
    }

    private synchronized void closeConnection() {
        boolean isConnected;
        synchronized (isConnectedLock) {
            isConnected = this.isConnected;
            this.isConnected = false;
        }
        if (isConnected) {
            synchronized (isUpdatingFirmwareLock) {
                isUpdatingFirmware = false;
            }
            neglectNextBluetoothResetFrame.set(false);
            stopBuffersThreads();
            if (connectedThread != null) {
                connectedThread.interrupt();
                connectedThread.cancel();
                connectedThread = null;
            }
            if (callbacksTimeout != null) callbacksTimeout.stopTimer();
            if (exitingCallbacksThread != null && exitingCallbacksThread.isAlive())
                exitingCallbacksThread.interrupt();
            if (enteringCallbacksThread != null && enteringCallbacksThread.isAlive())
                enteringCallbacksThread.interrupt();
            clearAllBuffers();
            queuedFrames.clear();
            synchronized (arduinoCallbacksLock) {
                isInACallback = false;
            }
            Log.i("Device " + this.name + ": Device disconnected.");
            stopRenamingBoardTimeOut();
            stopFirmwareTestingTimeOut();
            stopLibraryTestingTimeOut();
            stopFirmwareUpdateThreads();
            onDisconnect();
        }
    }

    /**
     * Checks whether the device type is plus or classic.
     *
     * @return the boolean
     */
    public boolean isTypePlus() {
        return Build.VERSION.SDK_INT >= 18 && (isTypePlus || bluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE);
    }

    private byte readByteFromBluetoothBufferForFirmware() throws InterruptedException {
        byte returnValue = firmwareUpdateBuffer.take();
        if (firmwareUpdatingTimeOut != null)
            firmwareUpdatingTimeOut.resetTimer();
        return returnValue;
    }

    private int calculateCrc(byte[] data) {
        int crc = 0;
        int crcTable[] = {
                0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
                0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
                0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
                0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
                0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
                0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
                0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
                0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
                0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
                0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
                0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
                0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
                0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
                0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
                0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
                0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
                0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
                0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
                0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
                0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
                0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
                0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
                0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
                0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
                0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
                0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
                0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
                0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
                0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
                0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
                0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
                0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
        };
        for (byte readChar : data)
            crc = (crc << 8) ^ crcTable[((crc >> 8) ^ readChar) & 0xff];
        return crc & 0xffff;
    }

    private void sendFileUsingXmodemProtocol(byte[] fileArray, int maxRetriesNumber) throws InterruptedException {
        ByteArrayInputStream stream = new ByteArrayInputStream(fileArray, 0, fileArray.length);
        initFirmwareUpdatingTimeOut();
        firmwareUpdateBuffer.clear();
        if (readByteFromBluetoothBufferForFirmware() != NAK) {
            writeByteForFirmwareUpdate(CAN);
            onFirmwareUpdateFailure(false, "Firmware updating failed, the board didn't request the authentication key!");
            return;
        }
        writeByteForFirmwareUpdate(KEY);
        final int packetSize = 128;
        int errorCount = 0;
        int crcValue;
        while (!Thread.currentThread().isInterrupted()) {
            byte readChar = readByteFromBluetoothBufferForFirmware();

            if (readChar == CRC) {
                break;
            } else if (readChar == CAN) {
                writeByteForFirmwareUpdate(CAN);
                onFirmwareUpdateFailure(false, "Firmware updating failed, the board canceled the process!");
                return;
            } else {
                writeByteForFirmwareUpdate(CAN);
                Log.i("Device " + OneSheeldDevice.this.name + ": Firmware updating error, expected a response and got another one!");
            }
            errorCount += 1;
            if (errorCount >= maxRetriesNumber) {
                writeByteForFirmwareUpdate(CAN);
                onFirmwareUpdateFailure(false, "Firmware updating failed, too many errors occurred!");
                return;
            }
        }
        errorCount = 0;
        int successCount = 0;
        byte sequence = 1;
        while (!Thread.currentThread().isInterrupted()) {
            byte[] data = new byte[packetSize];
            int readCount;
            readCount = stream.read(data, 0, packetSize);
            if (readCount <= 0) {
                break;
            }
            for (int i = readCount; i < packetSize; i++) {
                data[i] = SUB;
            }
            crcValue = calculateCrc(data);
            firmwareUpdateBuffer.clear();
            while (!Thread.currentThread().isInterrupted()) {
                writeByteForFirmwareUpdate(SOH);
                writeByteForFirmwareUpdate(sequence);
                writeByteForFirmwareUpdate((byte) (0xff - sequence));
                writeByteForFirmwareUpdate(data);
                writeByteForFirmwareUpdate((byte) (crcValue >> 8));
                writeByteForFirmwareUpdate((byte) (crcValue & 0xff));
                byte readChar = readByteFromBluetoothBufferForFirmware();
                if (readChar == ACK) {
                    successCount += 1;
                    onFirmwareUpdateProgress(fileArray.length, (readCount >= 128) ? successCount * readCount : ((successCount - 1) * packetSize + readCount));
                    break;
                }
                if (readChar == NAK) {
                    errorCount += 1;
                    onFirmwareUpdateProgress(fileArray.length, (readCount >= 128) ? successCount * readCount : ((successCount - 1) * packetSize + readCount));
                    if (errorCount >= maxRetriesNumber) {
                        writeByteForFirmwareUpdate(CAN);
                        onFirmwareUpdateFailure(false, "Firmware updating failed, too many errors occurred!");
                        return;
                    }
                    continue;
                }
                writeByteForFirmwareUpdate(CAN);
                onFirmwareUpdateFailure(false, "Firmware updating failed, expected a response and got another one!");
                return;
            }
            sequence = (byte) ((sequence + 1) % 0x100);
        }
        while (!Thread.currentThread().isInterrupted()) {
            writeByteForFirmwareUpdate(EOT);
            byte readChar = readByteFromBluetoothBufferForFirmware();
            if (readChar == ACK) {
                onFirmwareUpdateSuccess();
                break;
            } else {
                errorCount += 1;
                if (errorCount >= maxRetriesNumber) {
                    writeByteForFirmwareUpdate(CAN);
                    onFirmwareUpdateFailure(false, "Firmware updating failed, final response was not received, update aborted!");
                    return;
                }
            }
        }
    }

    /**
     * Updates the board firmware.
     *
     * @param fileArray the firmware in bytes
     * @see OneSheeldFirmwareUpdateCallback
     */
    public void update(final byte[] fileArray) {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (isUpdatingFirmware()) {
            onError(OneSheeldError.FIRMWARE_UPDATE_IN_PROGRESS);
            return;
        } else if (fileArray == null || fileArray.length <= 0)
            throw new NullPointerException("The passed firmware is invalid, have you checked its validity?");
        stopFirmwareUpdateThreads();
        firmwareUpdatingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    onFirmwareUpdateStart();
                    prepareForFirmwareUpdateStart();
                    sendFileUsingXmodemProtocol(fileArray, MAX_FIRMWARE_UPDATING_RETRIES);
                } catch (InterruptedException ignored) {
                    prepareForFirmwareUpdateEnd();
                    stopFirmwareUpdatingTimeOut();
                    Thread.currentThread().interrupt();
                }
            }
        });
        firmwareUpdatingThread.start();
    }

    /**
     * Cancel the in-progress firmware update.
     */
    public void cancelUpdate() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        }
        stopFirmwareUpdateThreads();
        prepareForFirmwareUpdateEnd();
    }

    private void stopFirmwareUpdateThreads() {
        if (firmwareUpdatingThread != null && firmwareUpdatingThread.isAlive()) {
            firmwareUpdatingThread.interrupt();
            firmwareUpdatingThread = null;
        }
        stopFirmwareUpdatingTimeOut();
    }

    private void onFirmwareUpdateFailure(boolean isTimeOut, String errorMessage) {
        Log.i("Device " + OneSheeldDevice.this.name + ": " + errorMessage);
        stopFirmwareUpdateThreads();
        prepareForFirmwareUpdateEnd();
        for (OneSheeldFirmwareUpdateCallback firmwareUpdateCallback : firmwareUpdateCallbacks) {
            firmwareUpdateCallback.onFailure(this, isTimeOut);
        }
    }

    private void onFirmwareUpdateStart() {
        Log.i("Device " + OneSheeldDevice.this.name + ": Firmware updating started.");
        for (OneSheeldFirmwareUpdateCallback firmwareUpdateCallback : firmwareUpdateCallbacks) {
            firmwareUpdateCallback.onStart(this);
        }
    }

    private void onFirmwareUpdateProgress(int totalBytes, int sentBytes) {
        Log.i("Device " + OneSheeldDevice.this.name + ": Firmware updating is progressing (" + ((int) ((float) sentBytes / totalBytes * 100)) + "%).");
        for (OneSheeldFirmwareUpdateCallback firmwareUpdateCallback : firmwareUpdateCallbacks) {
            firmwareUpdateCallback.onProgress(this, totalBytes, sentBytes);
        }
    }

    private void onFirmwareUpdateSuccess() {
        Log.i("Device " + OneSheeldDevice.this.name + ": Firmware updating succeeded.");
        stopFirmwareUpdateThreads();
        prepareForFirmwareUpdateEnd();
        for (OneSheeldFirmwareUpdateCallback firmwareUpdateCallback : firmwareUpdateCallbacks) {
            firmwareUpdateCallback.onSuccess(this);
        }
    }

    private void prepareForFirmwareUpdateStart() {
        synchronized (isUpdatingFirmwareLock) {
            sendMuteFrame();
            resetBoard();
            isUpdatingFirmware = true;
            clearAllBuffers();
            resetProcessInput();
        }
    }

    private void prepareForFirmwareUpdateEnd() {
        synchronized (isUpdatingFirmwareLock) {
            if (isUpdatingFirmware) {
                neglectNextBluetoothResetFrame.set(true);
                isMuted = false;
                serialBuffer.clear();
                synchronized (bluetoothBufferLock) {
                    ArrayList<Byte> pendingBytes = new ArrayList<>();
                    for (byte dataByte : firmwareUpdateBuffer) {
                        pendingBytes.add(dataByte);
                    }
                    for (byte dataByte : bluetoothBuffer) {
                        pendingBytes.add(dataByte);
                    }
                    bluetoothBuffer.clear();
                    for (byte dataByte : pendingBytes) {
                        bluetoothBuffer.add(dataByte);
                    }
                    isUpdatingFirmware = false;
                    resetProcessInput();
                }
                firmwareUpdateBuffer.clear();
            }
        }
        sendInitializationFrames();
    }

    private void stopRenamingBoardTimeOut() {
        if (renamingBoardTimeout != null)
            renamingBoardTimeout.stopTimer();
        hasRenamingStarted = false;
    }

    private void initRenamingBoardTimeOut() {
        stopRenamingBoardTimeOut();
        renamingBoardTimeout = new TimeOut(2000, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                if (renamingRetries > 0 && isConnected()) {
                    renamingRetries--;
                    Log.i("Device " + OneSheeldDevice.this.name + ": Board renaming time-outed, retrying again.");
                    for (OneSheeldRenamingCallback renamingCallback : renamingCallbacks) {
                        renamingCallback.onRenamingAttemptTimeOut(OneSheeldDevice.this);
                    }
                    sendRenamingRequest(pendingName);
                } else {
                    renamingRetries = MAX_RENAMING_RETRIES_NUMBER;
                    hasRenamingStarted = false;
                    Log.i("Device " + OneSheeldDevice.this.name + ": All attempts to rename the board time-outed. Aborting.");
                    if (isConnected()) {
                        for (OneSheeldRenamingCallback renamingCallback : renamingCallbacks) {
                            renamingCallback.onAllRenamingAttemptsTimeOut(OneSheeldDevice.this);
                        }
                    }
                }
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
    }

    private void stopFirmwareUpdatingTimeOut() {
        if (firmwareUpdatingTimeOut != null)
            firmwareUpdatingTimeOut.stopTimer();
    }

    private void initFirmwareUpdatingTimeOut() {
        stopFirmwareUpdatingTimeOut();
        firmwareUpdatingTimeOut = new TimeOut(3000, 1000, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                onFirmwareUpdateFailure(true, "Firmware updating failed, time out occurred because the board didn't respond in a timely fashion.");
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
    }

    private void stopFirmwareTestingTimeOut() {
        if (firmwareTestingTimeout != null)
            firmwareTestingTimeout.stopTimer();
        hasFirmwareTestStarted = false;
    }

    private void initFirmwareTestingTimeOut() {
        stopFirmwareTestingTimeOut();
        firmwareTestingTimeout = new TimeOut(4000, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                hasFirmwareTestStarted = false;
                Log.i("Device " + OneSheeldDevice.this.name + ": Firmware testing time-outed.");
                if (isConnected()) {
                    for (OneSheeldTestingCallback oneSheeldTestingCallback : testingCallbacks)
                        oneSheeldTestingCallback.onFirmwareTestTimeOut(OneSheeldDevice.this);
                }
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
    }

    private void stopLibraryTestingTimeOut() {
        if (libraryTestingTimeout != null)
            libraryTestingTimeout.stopTimer();
        hasLibraryTestStarted = false;
    }

    private void initLibraryTestingTimeOut() {
        stopLibraryTestingTimeOut();
        libraryTestingTimeout = new TimeOut(4000, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                hasLibraryTestStarted = false;
                Log.i("Device " + OneSheeldDevice.this.name + ": Library testing time-outed.");
                if (isConnected()) {
                    for (OneSheeldTestingCallback oneSheeldTestingCallback : testingCallbacks)
                        oneSheeldTestingCallback.onLibraryTestTimeOut(OneSheeldDevice.this);
                }
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
    }

    private void resetBoard() {
        synchronized (sendingDataLock) {
            sysex(RESET_MICRO, new byte[]{});
        }
    }

    private class ConnectedThread extends Thread {
        private final OneSheeldConnection connection;

        ConnectedThread(OneSheeldConnection connection) {
            this.connection = connection;
            if (connection != null)
                connection.setConnectionCloseCallback(new BluetoothConnectionCloseCallback() {
                    @Override
                    public void onConnectionClose() {
                        closeConnection();
                    }
                });
            setName("OneSheeldConnectedReadThread: " + OneSheeldDevice.this.getName());
        }

        @Override
        public void run() {
            if (connection == null) return;
            Log.i("Device " + OneSheeldDevice.this.name + ": Establishing connection.");
            synchronized (isConnectedLock) {
                isConnected = true;
            }
            Log.i("Device " + OneSheeldDevice.this.name + ": Initializing board and querying its information.");
            initFirmware();
            Log.i("Device " + OneSheeldDevice.this.name + ": Device connected, initialized and ready for communication.");
            onConnect();
            while (!this.isInterrupted()) {
                byte[] readBytes = connection.read();
                synchronized (bluetoothBufferLock) {
                    for (byte readByte : readBytes) {
                        bluetoothBuffer.add(readByte);
                    }
                }
            }
        }

        private synchronized void write(final byte[] buffer) {
            if (connection != null) connection.write(buffer);
        }

        private synchronized void cancel() {
            if (connection != null) connection.close();
        }
    }

    private class BluetoothBufferListeningThread extends Thread {
        BluetoothBufferListeningThread() {
            setName("BluetoothBufferListeningThread: " + OneSheeldDevice.this.getName());
            start();
        }

        private void stopRunning() {
            if (this.isAlive())
                this.interrupt();
        }

        @Override
        public void run() {
            byte input;
            while (!this.isInterrupted()) {
                try {
                    input = readByteFromBluetoothBuffer();
                    if (isUpdatingFirmware()) {
                        firmwareUpdateBuffer.add(input);
                    } else {
                        synchronized (processInputLock) {
                            processInput(input);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private class SerialBufferListeningThread extends Thread {
        SerialBufferListeningThread() {
            setName("SerialBufferListeningThread: " + OneSheeldDevice.this.getName());
            start();
        }

        private void stopRunning() {
            if (this.isAlive())
                this.interrupt();
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    while (true) {
                        if (readByteFromSerialBuffer() == ShieldFrame.START_OF_FRAME)
                            break;
                    }
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    ShieldFrameTimeout = new TimeOut(2000);
                    int tempArduinoLibVersion = readByteFromSerialBuffer();
                    byte shieldId = readByteFromSerialBuffer();
                    byte verificationByte = readByteFromSerialBuffer();
                    if ((((verificationByte & 0xF0) >> 4) & (verificationByte & 0x0F)) != 0) {
                        Log.i("Device " + OneSheeldDevice.this.name + ": Frame is incorrect, canceling what we've read so far.");
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        continue;
                    }
                    byte functionId = readByteFromSerialBuffer();
                    ShieldFrame frame = new ShieldFrame(shieldId, functionId);
                    int argumentsNumber = readByteFromSerialBuffer() & 0xFF;
                    int argumentsNumberVerification = (255 - (readByteFromSerialBuffer() & 0xFF));
                    if (argumentsNumber != argumentsNumberVerification) {
                        Log.i("Device " + OneSheeldDevice.this.name + ": Frame is incorrect, canceling what we've read so far.");
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        continue;
                    }
                    boolean continueRequested = false;
                    for (int i = 0; i < argumentsNumber; i++) {
                        int length = readByteFromSerialBuffer() & 0xFF;
                        int lengthVerification = (255 - (readByteFromSerialBuffer() & 0xFF));
                        if (length != lengthVerification || length <= 0) {
                            Log.i("Device " + OneSheeldDevice.this.name + ": Frame is incorrect, canceling what we've read so far.");
                            if (ShieldFrameTimeout != null)
                                ShieldFrameTimeout.stopTimer();
                            continueRequested = true;
                            break;
                        }
                        byte[] data = new byte[length];
                        for (int j = 0; j < length; j++) {
                            data[j] = readByteFromSerialBuffer();
                        }
                        frame.addArgument(data);
                    }
                    if (continueRequested) continue;
                    if ((readByteFromSerialBuffer()) != ShieldFrame.END_OF_FRAME) {
                        Log.i("Device " + OneSheeldDevice.this.name + ": Frame is incorrect, canceling what we've read so far.");
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        continue;
                    }
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    if (arduinoLibraryVersion != tempArduinoLibVersion) {
                        arduinoLibraryVersion = tempArduinoLibVersion;
                        isLibraryVersionQueried = true;
                        Log.i("Device " + OneSheeldDevice.this.name + ": Device replied with library version: " + arduinoLibraryVersion + ".");
                        onLibraryVersionQueryResponse(arduinoLibraryVersion);
                    }

                    if (shieldId == CONFIGURATION_SHIELD_ID) {
                        switch (functionId) {
                            case LIBRARY_VERSION_RESPONSE:
                                break;
                            case IS_HARDWARE_CONNECTED_QUERY:
                                notifyHardwareOfConnection();
                                break;
                            case IS_CALLBACK_ENTERED:
                                callbackEntered();
                                break;
                            case IS_CALLBACK_EXITED:
                                callbackExited();
                                break;
                            case LIBRARY_TESTING_CHALLENGE_RESPONSE:
                                hasLibraryTestStarted = false;
                                boolean isTestResultCorrect = false;
                                try {
                                    if (frame.getArguments().size() == 2) {
                                        if (frame.getArgumentAsString(0).equals("Yup, I'm feeling great!")) {
                                            if (frame.getArgument(1).length == 1 && frame.getArgument(1)[0] == correctTestingChallengeAnswer) {
                                                isTestResultCorrect = true;
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                                if (isTestResultCorrect)
                                    Log.i("Device " + OneSheeldDevice.this.name + ": Library testing succeeded.");
                                else
                                    Log.i("Device " + OneSheeldDevice.this.name + ": Library testing failed.");
                                stopLibraryTestingTimeOut();
                                if (isConnected()) {
                                    for (OneSheeldTestingCallback oneSheeldTestingCallback : testingCallbacks)
                                        oneSheeldTestingCallback.onLibraryTestResult(OneSheeldDevice.this, isTestResultCorrect);
                                }
                                break;
                        }
                    } else {
                        Log.i("Device " + OneSheeldDevice.this.name + ": Frame received, values: " + frame + ".");
                        for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
                            oneSheeldDataCallback.onShieldFrameReceive(OneSheeldDevice.this, frame);
                            if (OneSheeldSdk.getKnownShields().contains(shieldId) &&
                                    OneSheeldSdk.getKnownShields().getKnownShield(shieldId).getKnownFunctions().contains(KnownFunction.getFunctionWithId(functionId)))
                                oneSheeldDataCallback.onKnownShieldFrameReceive(OneSheeldDevice.this, OneSheeldSdk.getKnownShields().getKnownShield(shieldId), frame);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ShieldFrameNotComplete e) {
                    Log.i("Device " + OneSheeldDevice.this.name + ": Frame wasn't completed in 2 seconds, canceling what we've read so far.");
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    ShieldFrameTimeout = null;
                }
            }
        }
    }

    private class ShieldFrameNotComplete extends Exception {
    }
}

