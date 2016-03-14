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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
    private final byte BOARD_TESTING = (byte) 0x5D;
    private final byte BOARD_RENAMING = (byte) 0x5E;
    private final byte REPORT_INPUT_PINS = (byte) 0x5F;
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
    private Queue<ShieldFrame> queuedFrames;
    private LinkedBlockingQueue<Byte> bluetoothBuffer;
    private LinkedBlockingQueue<Byte> serialBuffer;
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
    private CopyOnWriteArrayList<OneSheeldBoardTestingCallback> testingCallbacks;
    private CopyOnWriteArrayList<OneSheeldBoardRenamingCallback> renamingCallbacks;
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
    private boolean hasBoardRenamingStarted;
    private final int MAX_RENAMING_RETRIES_NUMBER = 2;


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
        manager = OneSheeldManager.getInstance();
        connectionCallbacks = new CopyOnWriteArrayList<>();
        errorCallbacks = new CopyOnWriteArrayList<>();
        dataCallbacks = new CopyOnWriteArrayList<>();
        versionQueryCallbacks = new CopyOnWriteArrayList<>();
        testingCallbacks = new CopyOnWriteArrayList<>();
        renamingCallbacks = new CopyOnWriteArrayList<>();
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
        hasBoardRenamingStarted = false;
        renamingRetries = MAX_RENAMING_RETRIES_NUMBER;
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
     */
    public void addConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && !connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.add(connectionCallback);
    }

    /**
     * Add a data callback.
     *
     * @param dataCallback the data callback
     */
    public void addDataCallback(OneSheeldDataCallback dataCallback) {
        if (dataCallback != null && !dataCallbacks.contains(dataCallback))
            dataCallbacks.add(dataCallback);
    }

    /**
     * Add a version query callback.
     *
     * @param versionQueryCallback the version query callback
     */
    public void addVersionQueryCallback(OneSheeldVersionQueryCallback versionQueryCallback) {
        if (versionQueryCallback != null && !versionQueryCallbacks.contains(versionQueryCallback))
            versionQueryCallbacks.add(versionQueryCallback);
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
     * Add a testing callback.
     *
     * @param testingCallback the testing callback
     */
    public void addTestingCallback(OneSheeldBoardTestingCallback testingCallback) {
        if (testingCallback != null && !testingCallbacks.contains(testingCallback))
            testingCallbacks.add(testingCallback);
    }

    /**
     * Add a renaming callback.
     *
     * @param renamingCallback the renaming callback
     */
    public void addRenamingCallback(OneSheeldBoardRenamingCallback renamingCallback) {
        if (renamingCallback != null && !renamingCallbacks.contains(renamingCallback))
            renamingCallbacks.add(renamingCallback);
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
     * Remove an error callback.
     *
     * @param errorCallback the error callback
     */
    public void removeErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && errorCallbacks.contains(errorCallback))
            errorCallbacks.remove(errorCallback);
    }

    /**
     * Remove a renaming callback.
     *
     * @param renamingCallback the renaming callback
     */
    public void removeRenamingCallback(OneSheeldBoardRenamingCallback renamingCallback) {
        if (renamingCallback != null && renamingCallbacks.contains(renamingCallback))
            renamingCallbacks.remove(renamingCallback);
    }

    /**
     * Remove a testing callback.
     *
     * @param testingCallback the testing callback
     */
    public void removeTestingCallback(OneSheeldBoardTestingCallback testingCallback) {
        if (testingCallback != null && testingCallbacks.contains(testingCallback))
            testingCallbacks.remove(testingCallback);
    }

    /**
     * Remove a data callback.
     *
     * @param dataCallback the data callback
     */
    public void removeDataCallback(OneSheeldDataCallback dataCallback) {
        if (dataCallback != null && dataCallbacks.contains(dataCallback))
            dataCallbacks.remove(dataCallback);
    }

    /**
     * Remove a version query callback.
     *
     * @param versionQueryCallback the version query callback
     */
    public void removeVersionQueryCallback(OneSheeldVersionQueryCallback versionQueryCallback) {
        if (versionQueryCallback != null && versionQueryCallbacks.contains(versionQueryCallback))
            versionQueryCallbacks.remove(versionQueryCallback);
    }

    /**
     * Add all of the device callbacks in one method call.
     *
     * @param connectionCallback   the connection callback
     * @param dataCallback         the data callback
     * @param versionQueryCallback the version query callback
     * @param errorCallback        the error callback
     * @param testingCallback      the testing callback
     * @param renamingCallback     the renaming callback
     */
    public void addCallbacks(OneSheeldConnectionCallback connectionCallback, OneSheeldDataCallback dataCallback, OneSheeldVersionQueryCallback versionQueryCallback, OneSheeldErrorCallback errorCallback, OneSheeldBoardTestingCallback testingCallback, OneSheeldBoardRenamingCallback renamingCallback) {
        addConnectionCallback(connectionCallback);
        addErrorCallback(errorCallback);
        addDataCallback(dataCallback);
        addVersionQueryCallback(versionQueryCallback);
        addTestingCallback(testingCallback);
        addRenamingCallback(renamingCallback);
    }

    private void clearAllBuffers() {
        bluetoothBuffer.clear();
        serialBuffer.clear();
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

    private int readByteFromBluetoothBuffer() throws InterruptedException {
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
        manager.onConnect(this);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onConnect(this);
        }
    }

    private void onDisconnect() {
        manager.onDisconnect(this);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onDisconnect(this);
        }
    }

    void onError(OneSheeldError error) {
        for (OneSheeldErrorCallback errorCallback : errorCallbacks) {
            errorCallback.onError(this, error);
        }
    }

    void onConnectionRetry(int retryCount) {
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            connectionCallback.onConnectionRetry(this, retryCount);
        }
    }

    private void resetProcessInput() {
        waitForData = 0;
        executeMultiByteCommand = 0;
        multiByteChannel = 0;
        storedInputData = new byte[MAX_DATA_BYTES];
        parsingSysex = false;
        sysexBytesRead = 0;
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
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, BT_CONNECTED));
    }


    /**
     * Explicitly Queue a shield frame for sending after the Arduino exits the callback.
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

    public void rename(String name) {
        if (name == null || name.length() <= 0)
            return;
        else if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (hasBoardRenamingStarted) {
            Log.i("Device " + this.name + ": Device is in the middle of another renaming request.");
            return;
        }
        renamingRetries = MAX_RENAMING_RETRIES_NUMBER;
        hasBoardRenamingStarted = true;
        sendBoardRenamingRequest(name);
    }

    private void sendBoardRenamingRequest(String name) {
        if (name == null || name.length() <= 0) return;
        if (isTypePlus()) name = (name.length() > 11) ? name.substring(0, 11) : name;
        else name = (name.length() > 14) ? name.substring(0, 14) : name;
        Log.i("Device " + this.name + ": Trying to rename the device to \"" + name + "\".");
        pendingName = name;
        synchronized (sendingDataLock) {
            sysex(BOARD_RENAMING, name.getBytes(Charset.forName("US-ASCII")));
        }
        initRenamingBoardTimeOut();
    }

    public void test() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        } else if (hasFirmwareTestStarted || hasLibraryTestStarted) {
            Log.i("Device " + this.name + ": device is in the middle of another test.");
            return;
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
        synchronized (sendingDataLock) {
            sysex(BOARD_TESTING, bytes);
        }
        initFirmwareTestingTimeOut();
        hasLibraryTestStarted = true;
        ShieldFrame testingFrame = new ShieldFrame(CONFIGURATION_SHIELD_ID, LIBRARY_TESTING_CHALLENGE_REQUEST);
        testingFrame.addArgument("Are you ok?");
        testingFrame.addArgument(bytes);
        sendShieldFrame(testingFrame);
        initLibraryTestingTimeOut();
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
                oneSheeldDataCallback.onDigitalPinStatusChange(actualPinNumber, getDigitalPinStatus(actualPinNumber));
            }
        }
    }

    /**
     * Query the firmware version.
     */
    public void queryFirmwareVersion() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        }
        Log.i("Device " + this.name + ": Query firmware version.");
        isFirmwareVersionQueried = false;
        write(REPORT_VERSION);
    }

    /**
     * Query the library version.
     */
    public void queryLibraryVersion() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
            return;
        }
        Log.i("Device " + this.name + ": Query library version.");
        isLibraryVersionQueried = false;
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, QUERY_LIBRARY_VERSION));
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

    private void write(byte[] writeData) {
        if (isConnected() && connectedThread != null && connectedThread.isAlive())
            connectedThread.write(writeData);
    }

    private void write(byte writeData) {
        if (isConnected() && connectedThread != null && connectedThread.isAlive())
            connectedThread.write(new byte[]{writeData});
    }

    private void initFirmware() {
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
        enableReporting();
        setAllPinsAsInput();
        queryInputPinsValues();
        respondToIsAlive();
        queryFirmwareVersion();
        sendUnMuteFrame();
        notifyHardwareOfConnection();
        queryLibraryVersion();
    }

    /**
     * Mute all communications with the device.
     */
    public void mute() {
        if (!isConnected()) {
            onError(OneSheeldError.DEVICE_NOT_CONNECTED);
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
            versionQueryCallback.onLibraryVersionQueryResponse(version);
        }
    }

    private void onFirmwareVersionQueryResponse(int majorVersion, int minorVersion) {
        for (OneSheeldVersionQueryCallback versionQueryCallback : versionQueryCallbacks) {
            versionQueryCallback.onFirmwareVersionQueryResponse(new FirmwareVersion(majorVersion, minorVersion));
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
                                    oneSheeldDataCallback.onSerialDataReceive(b & 0xFF);
                                }
                            }
                        } else if (sysexCommand == BLUETOOTH_RESET) {
                            byte randomVal = (byte) (Math.random() * 255);
                            byte complement = (byte) (255 - randomVal & 0xFF);
                            synchronized (sendingDataLock) {
                                sysex(BLUETOOTH_RESET, new byte[]{0x01, randomVal, complement});
                            }
                            Log.i("Device " + this.name + ": Device requested Bluetooth reset.");
                            closeConnection();
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
                            for (OneSheeldBoardTestingCallback oneSheeldBoardTestingCallback : testingCallbacks)
                                oneSheeldBoardTestingCallback.onFirmwareTestResult(isPassed);
                        } else if (sysexCommand == BOARD_RENAMING) {
                            Log.i("Device " + this.name + ": Device received the renaming request successfully, it should be renamed to \"" + pendingName + "\" in a couple of seconds.");
                            this.name = pendingName;
                            hasBoardRenamingStarted = false;
                            stopRenamingBoardTimeOut();
                            for (OneSheeldBoardRenamingCallback renamingCallback : renamingCallbacks) {
                                renamingCallback.onRenamingRequestReceivedSuccessfully();
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
     */
    public void disconnect() {
        Log.i("Device " + this.name + ": Disconnection request received.");
        closeConnection();
    }

    /**
     * Connect to the device.
     */
    public void connect() {
        Log.i("Device " + this.name + ": Delegate the connection request to the manager.");
        manager.connect(this);
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
            queuedFrames.clear();
            synchronized (arduinoCallbacksLock) {
                isInACallback = false;
            }

            Log.i("Device " + this.name + ": Device disconnected.");
            onDisconnect();
        }
    }

    public boolean isTypePlus() {
        return Build.VERSION.SDK_INT >= 18 && (isTypePlus || bluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE);
    }

    private void stopRenamingBoardTimeOut() {
        if (renamingBoardTimeout != null)
            renamingBoardTimeout.stopTimer();
    }

    private void initRenamingBoardTimeOut() {
        stopRenamingBoardTimeOut();
        renamingBoardTimeout = new TimeOut(2000, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                if (renamingRetries > 0 && isConnected()) {
                    renamingRetries--;
                    Log.i("Device " + OneSheeldDevice.this.name + ": Board renaming time-outed, retrying again.");
                    for (OneSheeldBoardRenamingCallback renamingCallback : renamingCallbacks) {
                        renamingCallback.onRenamingAttemptTimeOut();
                    }
                    sendBoardRenamingRequest(pendingName);
                } else {
                    renamingRetries = MAX_RENAMING_RETRIES_NUMBER;
                    hasBoardRenamingStarted = false;
                    Log.i("Device " + OneSheeldDevice.this.name + ": All attempts to rename the board time-outed. Aborting.");
                    for (OneSheeldBoardRenamingCallback renamingCallback : renamingCallbacks) {
                        renamingCallback.onAllRenamingAttemptsTimeOut();
                    }
                }
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
    }

    private void stopFirmwareTestingTimeOut() {
        if (firmwareTestingTimeout != null)
            firmwareTestingTimeout.stopTimer();
    }

    private void initFirmwareTestingTimeOut() {
        stopFirmwareTestingTimeOut();
        firmwareTestingTimeout = new TimeOut(2000, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                hasFirmwareTestStarted = false;
                Log.i("Device " + OneSheeldDevice.this.name + ": Firmware testing time-outed.");
                for (OneSheeldBoardTestingCallback oneSheeldBoardTestingCallback : testingCallbacks)
                    oneSheeldBoardTestingCallback.onFirmwareTestTimeOut();
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
    }

    private void stopLibraryTestingTimeOut() {
        if (libraryTestingTimeout != null)
            libraryTestingTimeout.stopTimer();
    }

    private void initLibraryTestingTimeOut() {
        stopLibraryTestingTimeOut();
        libraryTestingTimeout = new TimeOut(2000, 100, new TimeOut.TimeOutCallback() {
            @Override
            public void onTimeOut() {
                hasLibraryTestStarted = false;
                Log.i("Device " + OneSheeldDevice.this.name + ": Library testing time-outed.");
                for (OneSheeldBoardTestingCallback oneSheeldBoardTestingCallback : testingCallbacks)
                    oneSheeldBoardTestingCallback.onLibraryTestTimeOut();
            }

            @Override
            public void onTick(long milliSecondsLeft) {

            }
        });
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
                for (byte readByte : readBytes) {
                    bluetoothBuffer.add(readByte);
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
            int input;
            while (!this.isInterrupted()) {
                try {
                    input = readByteFromBluetoothBuffer();
                    processInput((byte) input);
                } catch (InterruptedException e) {
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
                    while ((readByteFromSerialBuffer()) != ShieldFrame.START_OF_FRAME)
                        ;
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    ShieldFrameTimeout = new TimeOut(2000);
                    int tempArduinoLibVersion = readByteFromSerialBuffer();
                    byte shieldId = readByteFromSerialBuffer();
                    byte instanceId = readByteFromSerialBuffer();
                    byte functionId = readByteFromSerialBuffer();
                    ShieldFrame frame = new ShieldFrame(shieldId, functionId);
                    int argumentsNumber = readByteFromSerialBuffer() & 0xFF;
                    int argumentsNumberVerification = (255 - (readByteFromSerialBuffer() & 0xFF));
                    if (argumentsNumber != argumentsNumberVerification) {
                        Log.i("Device " + OneSheeldDevice.this.name + ": Frame is incorrect, canceling what we've read so far.");
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        serialBuffer.clear();
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
                            serialBuffer.clear();
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
                        serialBuffer.clear();
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
                        if (functionId == LIBRARY_VERSION_RESPONSE) {
                        } else if (functionId == IS_HARDWARE_CONNECTED_QUERY) {
                            notifyHardwareOfConnection();
                        } else if (functionId == IS_CALLBACK_ENTERED) {
                            callbackEntered();
                        } else if (functionId == IS_CALLBACK_EXITED) {
                            callbackExited();
                        } else if (functionId == LIBRARY_TESTING_CHALLENGE_RESPONSE) {
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
                            for (OneSheeldBoardTestingCallback oneSheeldBoardTestingCallback : testingCallbacks)
                                oneSheeldBoardTestingCallback.onLibraryTestResult(isTestResultCorrect);
                        }
                    } else {
                        Log.i("Device " + OneSheeldDevice.this.name + ": Frame received, values: " + frame + ".");
                        for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
                            oneSheeldDataCallback.onShieldFrameReceive(frame);
                            if (OneSheeldSdk.getKnownShields().contains(shieldId) &&
                                    OneSheeldSdk.getKnownShields().getKnownShield(shieldId).getKnownFunctions().contains(KnownFunction.getFunctionWithId(functionId)))
                                oneSheeldDataCallback.onKnownShieldFrameReceive(OneSheeldSdk.getKnownShields().getKnownShield(shieldId), frame);
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (ShieldFrameNotComplete e) {
                    Log.i("Device " + OneSheeldDevice.this.name + ": Frame wasn't completed in 1 second, canceling what we've read so far.");
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    ShieldFrameTimeout = null;
                    continue;
                }
            }
        }
    }

    private class ShieldFrameNotComplete extends Exception {
    }
}

