package com.integreight.onesheeld.sdk;

public class TimeOut extends Thread {

    boolean isTimeout;
    long milliSecondsLeft;
    long totalMilliSeconds;
    long milliStep;
    TimeOutCallback callback;

    public TimeOut(long milliSeconds, long milliStep, TimeOutCallback callback) {
        isTimeout = false;
        this.totalMilliSeconds = milliSeconds;
        this.callback = callback;
        this.milliStep = milliStep;
        stopTimer();
        start();
    }

    public TimeOut(long milliSeconds) {
        isTimeout = false;
        this.totalMilliSeconds = milliSeconds;
        this.milliStep = milliSeconds;
        stopTimer();
        start();
    }

    public void resetTimer() {
        milliSecondsLeft = totalMilliSeconds;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void stopTimer() {
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

    public interface TimeOutCallback {
        void onTimeOut();

        void onTick(long milliSecondsLeft);
    }
}
