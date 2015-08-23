package com.integreight.onesheeld.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class OneSheeldDevice {
    public static final byte INPUT = 0;
    public static final byte OUTPUT = 1;
    public static final byte PWM = 3;
    public static final boolean LOW = false;
    public static final boolean HIGH = true;
    public static final int A0 = 14;
    public static final int A1 = 15;
    public static final int A2 = 16;
    public static final int A3 = 17;
    public static final int A4 = 18;
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
    private final byte REPORT_INPUT_PINS = (byte) 0x5F;
    private final byte BLUETOOTH_RESET = (byte) 0x61;
    private final byte IS_ALIVE = (byte) 0x62;
    private final byte MUTE_FIRMATA = (byte) 0x64;
    private final byte SERIAL_DATA = (byte) 0x66;
    private final byte CONFIGURATION_SHIELD_ID = (byte) 0x00;
    private final byte BT_CONNECTED = (byte) 0x01;
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
    private boolean isBluetoothBufferWaiting;
    private boolean isSerialBufferWaiting;
    private TimeOut ShieldFrameTimeout;
    private boolean isConnected;
    private OneSheeldManager manager;
    private CopyOnWriteArrayList<OneSheeldConnectionCallback> connectionCallbacks;
    private CopyOnWriteArrayList<OneSheeldErrorCallback> errorCallbacks;
    private CopyOnWriteArrayList<OneSheeldDataCallback> dataCallbacks;
    private CopyOnWriteArrayList<OneSheeldVersionQueryCallback> versionQueryCallbacks;
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

    public OneSheeldDevice(String address) {
        checkBluetoothAddress(address);
        name = null;
        this.address = address;
        isPaired = false;
        initialize();
    }

    public OneSheeldDevice(String address, String name) {
        checkBluetoothAddress(address);
        this.name = name;
        this.address = address;
        isPaired = false;
        initialize();
    }

    OneSheeldDevice(String address, String name, boolean isPaired) {
        checkBluetoothAddress(address);
        this.name = name;
        this.address = address;
        this.isPaired = isPaired;
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
    }

    private void stopBuffersThreads() {
        if (serialBufferListeningThread != null) {
            serialBufferListeningThread.stopRunning();
        }
        if (bluetoothBufferListeningThread != null) {
            bluetoothBufferListeningThread.stopRunning();
        }
    }

    public void addConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && !connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.add(connectionCallback);
    }

    public void addDataCallback(OneSheeldDataCallback dataCallback) {
        if (dataCallback != null && !dataCallbacks.contains(dataCallback))
            dataCallbacks.add(dataCallback);
    }

    public void addVersionQueryCallback(OneSheeldVersionQueryCallback versionQueryCallback) {
        if (versionQueryCallback != null && !versionQueryCallbacks.contains(versionQueryCallback))
            versionQueryCallbacks.add(versionQueryCallback);
    }

    public void addErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && !errorCallbacks.contains(errorCallback))
            errorCallbacks.add(errorCallback);
    }

    public void removeConnectionCallback(OneSheeldConnectionCallback connectionCallback) {
        if (connectionCallback != null && connectionCallbacks.contains(connectionCallback))
            connectionCallbacks.remove(connectionCallback);
    }

    public void removeErrorCallback(OneSheeldErrorCallback errorCallback) {
        if (errorCallback != null && errorCallbacks.contains(errorCallback))
            errorCallbacks.remove(errorCallback);
    }

    public void removeDataCallback(OneSheeldDataCallback dataCallback) {
        if (dataCallback != null && dataCallbacks.contains(dataCallback))
            dataCallbacks.remove(dataCallback);
    }

    public void removeVersionQueryCallback(OneSheeldVersionQueryCallback versionQueryCallback) {
        if (versionQueryCallback != null && versionQueryCallbacks.contains(versionQueryCallback))
            versionQueryCallbacks.remove(versionQueryCallback);
    }

    public void addCallbacks(OneSheeldConnectionCallback connectionCallback, OneSheeldDataCallback dataCallback, OneSheeldVersionQueryCallback versionQueryCallback, OneSheeldErrorCallback errorCallback) {
        addConnectionCallback(connectionCallback);
        addErrorCallback(errorCallback);
        addDataCallback(dataCallback);
        addVersionQueryCallback(versionQueryCallback);
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
        byte temp = serialBuffer.take().byteValue();
        if (ShieldFrameTimeout != null)
            ShieldFrameTimeout.resetTimer();
        return temp;
    }

    private int readByteFromBluetoothBuffer() throws InterruptedException {
        isBluetoothBufferWaiting = true;
        return bluetoothBuffer.take().byteValue();
    }

    private void checkBluetoothAddress(String address) {
        if (address == null || (address != null && !BluetoothAdapter.checkBluetoothAddress(address))) {
            throw new OneSheeldException("Bluetooth address is invalid, are you sure you specified it correctly?");
        }
    }

    BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isPaired() {
        return isPaired;
    }

    void setPaired(boolean isPaired) {
        this.isPaired = isPaired;
    }

    synchronized void connectUsing(BluetoothSocket socket) {
        try {
            closeConnection();
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    private void onConnect() {
        manager.onConnect(OneSheeldDevice.this);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            if (connectionCallback != null)
                connectionCallback.onConnect(this);
        }
    }

    private void onDisconnect() {
        manager.onDisconnect(this);
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            if (connectionCallback != null)
                connectionCallback.onDisconnect(this);
        }
    }

    void onError(OneSheeldError error) {
        for (OneSheeldErrorCallback errorCallback : errorCallbacks) {
            if (errorCallback != null)
                errorCallback.onError(this, error);
        }
    }

    void onConnectionRetry(int retryCount) {
        for (OneSheeldConnectionCallback connectionCallback : connectionCallbacks) {
            if (connectionCallback != null)
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
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, BT_CONNECTED));
    }


    public void queueShieldFrame(ShieldFrame frame) {
        if (queuedFrames != null) {
            queuedFrames.add(frame);
            callbackEntered();
        }
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
                if (callbacksTimeout == null || (callbacksTimeout != null && !callbacksTimeout.isAlive())) {
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
                while (queuedFrames != null && !queuedFrames.isEmpty()) {
                    sent = false;
                    synchronized (arduinoCallbacksLock) {
                        if (!isInACallback && lastTimeCallbacksExited != 0 && (SystemClock.elapsedRealtime() - lastTimeCallbacksExited > 200)) {
                            sendFrame(queuedFrames.poll());
                            sent = true;
                        }
                    }
                    if (sent)
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                }
            }
        });
        exitingCallbacksThread.start();
    }

    public void sendShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        if (!waitIfInACallback) {
            sendFrame(frame);
            return;
        }

        boolean inACallback;

        synchronized (arduinoCallbacksLock) {
            inACallback = isInACallback;
        }

        if (inACallback) {
            if (queuedFrames == null)
                queuedFrames = new ConcurrentLinkedQueue<>();
            queuedFrames.add(frame);
        } else {
            if (queuedFrames != null) {
                if (queuedFrames.isEmpty()) {
                    sendFrame(frame);
                } else {
                    queuedFrames.add(frame);
                }
            } else {
                sendFrame(frame);
            }
        }
    }

    public void sendShieldFrame(ShieldFrame frame) {
        sendShieldFrame(frame, false);
    }

    private void sendFrame(ShieldFrame frame) {
        if (frame == null) return;
        byte[] frameBytes = frame.getAllFrameAsBytes();
        sendSerialData(frameBytes);
    }

    private void respondToIsAlive() {
        synchronized (sendingDataLock) {
            sysex(IS_ALIVE, new byte[]{});
        }
    }

    public void sendSerialData(byte[] data) {
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
        synchronized (sendingDataLock) {
            for (byte i = 0; i < 3; i++) {
                write(new byte[]{(byte) (REPORT_DIGITAL | i), 1});
            }
        }
    }

    private void reportInputPinsValues() {
        synchronized (sendingDataLock) {
            sysex(REPORT_INPUT_PINS, new byte[]{});
        }
    }

    private void setAllPinsAsInput() {
        for (int i = 0; i < 20; i++) {
            pinMode(i, INPUT);
        }
    }


    public boolean digitalRead(int pin) {
        if (pin >= 20 || pin < 0)
            throw new OneSheeldException("The specified pin number is incorrect, are you sure you specified it correctly?");
        return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
    }

    public void pinMode(int pin, byte mode) {
        if (pin >= 20 || pin < 0)
            throw new OneSheeldException("The specified pin number is incorrect, are you sure you specified it correctly?");
        byte[] writeData = {SET_PIN_MODE, (byte) pin, mode};
        synchronized (sendingDataLock) {
            write(writeData);
        }
    }

    public void digitalWrite(int pin, boolean value) {
        if (pin >= 20 || pin < 0)
            throw new OneSheeldException("The specified pin number is incorrect, are you sure you specified it correctly?");
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

    public void analogWrite(int pin, int value) {
        if (pin >= 20 || pin < 0)
            throw new OneSheeldException("The specified pin number is incorrect, are you sure you specified it correctly?");
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
        ArrayList<Integer> differentPinNumbers = new ArrayList();
        for (int i = 0; i < 8; i++) {
            if (BitsUtils.isBitSet((byte) portDifference, i)) differentPinNumbers.add(i);
        }

        for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
            for (int pinNumber : differentPinNumbers) {
                int actualPinNumber = (portNumber << 3) + pinNumber;
                oneSheeldDataCallback.onDigitalPinStatusChange(actualPinNumber, digitalRead(actualPinNumber));
            }
        }
    }

    public void queryFirmwareVersion() {
        isFirmwareVersionQueried = false;
        write(REPORT_VERSION);
    }

    public void queryLibraryVersion() {
        isLibraryVersionQueried = false;
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, QUERY_LIBRARY_VERSION));
    }


    public boolean hasRespondedToFirmwareVersionQuery() {
        return isFirmwareVersionQueried;
    }

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

    private void initFirmata() {
        isBluetoothBufferWaiting = false;
        isSerialBufferWaiting = false;
        isMuted = false;
        arduinoLibraryVersion = -1;
        stopBuffersThreads();
        clearAllBuffers();
        resetProcessInput();
        bluetoothBufferListeningThread = new BluetoothBufferListeningThread();
        serialBufferListeningThread = new SerialBufferListeningThread();
        while (!isBluetoothBufferWaiting) ;
        while (!isSerialBufferWaiting) ;
        enableReporting();
        setAllPinsAsInput();
        reportInputPinsValues();
        respondToIsAlive();
        queryFirmwareVersion();
        notifyHardwareOfConnection();
        queryLibraryVersion();
    }

    public void mute() {
        synchronized (sendingDataLock) {
            sysex(MUTE_FIRMATA, new byte[]{1});
        }
        isMuted = true;
    }

    public void unMute() {
        synchronized (sendingDataLock) {
            sysex(MUTE_FIRMATA, new byte[]{0});
        }
        isMuted = false;
    }

    public boolean isMuted() {
        return isMuted;
    }

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
        onFirmwareVersionQueryResponse(majorVersion, minorVersion);
    }

    public FirmwareVersion getFirmwareVersion() {
        return new FirmwareVersion(majorVersion, minorVersion);
    }

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

                        if (sysexCommand == SERIAL_DATA && fixedSysexData != null) {
                            for (byte b : fixedSysexData) {
                                serialBuffer.add(b);
                            }
                        } else if (sysexCommand == BLUETOOTH_RESET) {
                            byte randomVal = (byte) (Math.random() * 255);
                            byte complement = (byte) (255 - randomVal & 0xFF);
                            synchronized (sendingDataLock) {
                                sysex(BLUETOOTH_RESET, new byte[]{0x01, randomVal, complement});
                            }
                            closeConnection();
                        } else if (sysexCommand == IS_ALIVE) {
                            respondToIsAlive();
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

    public void disconnect() {
        closeConnection();
    }

    public void connect() {
        manager.connect(this);
    }

    public boolean isConnected() {
        synchronized (isConnectedLock) {
            return isConnected;
        }
    }

    private synchronized void closeConnection() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread.interrupt();
            connectedThread = null;
        }
        boolean isConnected;
        synchronized (isConnectedLock) {
            isConnected = this.isConnected;
            this.isConnected = false;
        }
        if (callbacksTimeout != null) callbacksTimeout.stopTimer();
        if (exitingCallbacksThread != null && exitingCallbacksThread.isAlive())
            exitingCallbacksThread.interrupt();
        if (enteringCallbacksThread != null && enteringCallbacksThread.isAlive())
            enteringCallbacksThread.interrupt();
        if (queuedFrames != null) queuedFrames.clear();
        synchronized (arduinoCallbacksLock) {
            isInACallback = false;
        }
        if (isConnected) {
            onDisconnect();
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        Handler writeHandler;
        Looper writeHandlerLooper;
        Thread LooperThread;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            setName("OneSheeldConnectedThread: " + socket.getRemoteDevice().getAddress());
        }

        public void run() {
            if (mmSocket == null) return;
            LooperThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Looper.prepare();
                    writeHandlerLooper = Looper.myLooper();
                    writeHandler = new Handler();
                    Looper.loop();
                }
            });
            LooperThread.start();
            while (!LooperThread.isAlive()) ;
            synchronized (isConnectedLock) {
                isConnected = true;
            }
            initFirmata();
            onConnect();
            byte[] buffer = new byte[1024];
            int bufferLength;

            while (!this.isInterrupted()) {
                try {
                    bufferLength = mmInStream.read(buffer, 0, buffer.length);
                    bufferLength = bufferLength >= buffer.length ? buffer.length : bufferLength;
                    for (int i = 0; i < bufferLength; i++) {
                        bluetoothBuffer.add(buffer[i]);
                    }
                } catch (IOException e) {
                    if (writeHandlerLooper != null)
                        writeHandlerLooper.quit();
                    closeConnection();
                    break;
                }
            }
        }

        private synchronized void write(final byte[] buffer) {
            if (writeHandler == null)
                return;
            writeHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        mmOutStream.write(buffer);
                    } catch (IOException e) {
                        if (writeHandlerLooper != null)
                            writeHandlerLooper.quit();
                        closeConnection();
                    }
                }
            });
        }

        public synchronized void cancel() {
            if (mmInStream != null)
                try {
                    mmInStream.close();
                } catch (IOException e) {
                }
            if (mmOutStream != null)
                try {
                    mmOutStream.close();
                } catch (IOException e) {
                }
            if (mmSocket != null)
                try {
                    mmSocket.close();
                } catch (IOException e) {
                }
        }
    }

    private class BluetoothBufferListeningThread extends Thread {
        public BluetoothBufferListeningThread() {
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
                    for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
                        oneSheeldDataCallback.onSerialDataReceive(input);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private class SerialBufferListeningThread extends Thread {
        public SerialBufferListeningThread() {
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
                    ShieldFrameTimeout = new TimeOut(1000);
                    int tempArduinoLibVersion = readByteFromSerialBuffer();
                    byte shieldId = readByteFromSerialBuffer();
                    byte instanceId = readByteFromSerialBuffer();
                    byte functionId = readByteFromSerialBuffer();
                    ShieldFrame frame = new ShieldFrame(shieldId, instanceId,
                            functionId);
                    int argumentsNumber = readByteFromSerialBuffer() & 0xFF;
                    int argumentsNumberVerification = (255 - (readByteFromSerialBuffer() & 0xFF));
                    if (argumentsNumber != argumentsNumberVerification) {
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        serialBuffer.clear();
                        continue;
                    }
                    for (int i = 0; i < argumentsNumber; i++) {
                        int length = readByteFromSerialBuffer() & 0xFF;
                        int lengthVerification = (255 - (readByteFromSerialBuffer() & 0xFF));
                        if (length != lengthVerification || length <= 0) {
                            if (ShieldFrameTimeout != null)
                                ShieldFrameTimeout.stopTimer();
                            serialBuffer.clear();
                            continue;
                        }
                        byte[] data = new byte[length];
                        for (int j = 0; j < length; j++) {
                            data[j] = readByteFromSerialBuffer();
                        }
                        frame.addArgument(data);
                    }
                    if ((readByteFromSerialBuffer()) != ShieldFrame.END_OF_FRAME) {
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
                        onLibraryVersionQueryResponse(arduinoLibraryVersion);
                    }
//                    printFrameToLog(frame.getAllFrameAsBytes(), "Rec");
                    if (shieldId == CONFIGURATION_SHIELD_ID) {
                        if (functionId == LIBRARY_VERSION_RESPONSE) {

                        } else if (functionId == IS_HARDWARE_CONNECTED_QUERY) {
                            notifyHardwareOfConnection();
                        } else if (functionId == IS_CALLBACK_ENTERED) {
                            callbackEntered();
                        } else if (functionId == IS_CALLBACK_EXITED) {
                            callbackExited();
                        }
                    } else {
                        for (OneSheeldDataCallback oneSheeldDataCallback : dataCallbacks) {
                            oneSheeldDataCallback.onShieldFrameReceive(frame);
                        }
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (ShieldFrameNotComplete e) {
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

//    @Override
//    public boolean equals(Object o) {
//        if(o!=null && o instanceof OneSheeldDevice)
//        return this.getAddress().equals(((OneSheeldDevice) o).getAddress());
//        else return false;
//    }
}
