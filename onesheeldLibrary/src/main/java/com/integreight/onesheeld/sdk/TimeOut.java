package com.integreight.onesheeld.sdk;

class TimeOut extends Thread {

    boolean isTimeout;
    long milliSecondsLeft;
    long totalMilliSeconds;
    long milliStep;
    TimeOutCallback callback;

    TimeOut(long milliSeconds, long milliStep, TimeOutCallback callback) {
        isTimeout = false;
        this.totalMilliSeconds = milliSeconds;
        this.callback = callback;
        this.milliStep = milliStep;
        stopTimer();
        start();
    }

    TimeOut(long milliSeconds) {
        isTimeout = false;
        this.totalMilliSeconds = milliSeconds;
        this.milliStep = milliSeconds;
        stopTimer();
        start();
    }

    void resetTimer() {
        milliSecondsLeft = totalMilliSeconds;
    }

    boolean isTimeout() {
        return isTimeout;
    }

    void stopTimer() {
        if (isAlive()) this.interrupt();
    }

    @Override
    public synchronized void start() {
        resetTimer();
        super.start();
    }

    @Override
    public void run() {
        try {
            do {
                Thread.sleep(milliStep);
                if (callback != null && milliSecondsLeft != 0) callback.onTick(milliSecondsLeft);
                milliSecondsLeft -= milliStep;
            } while (milliSecondsLeft >= 0);
            isTimeout = true;
            if (callback != null) callback.onTimeOut();
        } catch (InterruptedException e) {
            return;
        }
    }

    interface TimeOutCallback {
        void onTimeOut();

        void onTick(long milliSecondsLeft);
    }
}
