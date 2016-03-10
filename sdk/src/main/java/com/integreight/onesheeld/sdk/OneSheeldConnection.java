package com.integreight.onesheeld.sdk;

//import android.os.Handler;
//import android.os.Looper;

/**
 * Created by iSsO on 2/29/16.
 */
abstract class OneSheeldConnection {
    private BluetoothConnectionCallback connectionCallback;
    private BluetoothConnectionErrorCallback connectionErrorCallback;
    private OneSheeldDevice device;

    //    private Handler backgroundHandler;
//    private Looper backgroundHandlerLooper;
//    private Handler mainThreadHandler;
//
//    private boolean connectOnMainThread;
    private boolean isConnectionCallbackCalled;

    protected OneSheeldConnection(OneSheeldDevice device/*, boolean connectOnMainThread*/) {
        this.device = device;
//        this.connectOnMainThread = connectOnMainThread;
//        mainThreadHandler = new Handler(Looper.getMainLooper());
//        initialize();
    }

//    private void initialize() {
//        Thread looperThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                backgroundHandlerLooper = Looper.myLooper();
//                backgroundHandler = new Handler();
//                Looper.loop();
//            }
//        });
//        looperThread.setName("OneSheeldConnectedBackgroundThread: " + device.getName());
//        looperThread.start();
//        while (!looperThread.isAlive()) ;
//    }

    public final void initiate() {
        close();
//        initialize();
        isConnectionCallbackCalled = false;
//        if (connectOnMainThread) {
//            runOnMainThread(new Runnable() {
//                @Override
//                public void run() {
//                    if(onConnectionInitiationRequest()){
//                        connectionSuccess();
//                    }
//                    else connectionFailure();
//                }
//            });
//        } else {
        if (onConnectionInitiationRequest()) {
            connectionSuccess();
        } else connectionFailure();
//        }
    }

    abstract boolean write(byte[] buffer);

    abstract byte[] read();

    final void setConnectionCallback(BluetoothConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    final void setConnectionErrorCallback(BluetoothConnectionErrorCallback bluetoothConnectionErrorCallback) {
        this.connectionErrorCallback = bluetoothConnectionErrorCallback;
    }

//    protected final void runOnBackgroundThread(Runnable runnable) {
//        if (runnable != null && backgroundHandler != null) backgroundHandler.post(runnable);
//    }

//    protected final void runOnMainThread(Runnable runnable) {
//        if (isRunningOnMainThread())
//            runnable.run();
//        else {
//            mainThreadHandler.post(runnable);
//        }
//    }

//    private boolean isRunningOnMainThread() {
//        return Looper.getMainLooper().getThread() == Thread.currentThread();
//    }

    public final void close() {
//        if (backgroundHandlerLooper != null) {
//            backgroundHandlerLooper.quit();
//        }
        onClose();
    }

    protected final void connectionSuccess() {
        if (connectionCallback != null && !isConnectionCallbackCalled) {
            connectionCallback.onConnectionSuccess();
            isConnectionCallbackCalled = true;
        }
    }

    protected final void connectionError() {
        close();
        if (connectionErrorCallback != null) connectionErrorCallback.onConnectionError();
    }

    protected final void connectionFailure() {
        if (!isConnectionCallbackCalled) {
            close();
            if (connectionCallback != null) connectionCallback.onConnectionFailure();
            isConnectionCallbackCalled = true;
        }
    }

    protected final void connectionInterrupt() {
        if (!isConnectionCallbackCalled) {
            close();
            if (connectionCallback != null) connectionCallback.onConnectionInterrupt();
            isConnectionCallbackCalled = true;
        }
    }

    protected final boolean isConnectionCallbackCalled() {
        return isConnectionCallbackCalled;
    }

    protected abstract boolean onConnectionInitiationRequest();

    protected final OneSheeldDevice getDevice() {
        return device;
    }

    protected abstract void onClose();
}
