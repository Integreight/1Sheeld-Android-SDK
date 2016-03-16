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

import java.util.concurrent.atomic.AtomicBoolean;

class TimeOut extends Thread {

    private boolean isTimeout;
    private long milliSecondsLeft;
    private long totalMilliSeconds;
    private long milliStep;
    private TimeOutCallback callback;
    private AtomicBoolean isSleeping;
    private AtomicBoolean isStopRequested;
    private final Object isTimeOutLock;


    TimeOut(long milliSeconds, long milliStep, TimeOutCallback callback) {
        this.isTimeout = false;
        this.totalMilliSeconds = milliSeconds;
        this.callback = callback;
        this.milliStep = milliStep <= milliSeconds && milliStep > 0 ? milliStep : milliSeconds;
        this.isSleeping = new AtomicBoolean(false);
        this.isStopRequested = new AtomicBoolean(false);
        this.isTimeOutLock = new Object();
        start();
    }

    TimeOut(long milliSeconds) {
        this.isTimeout = false;
        this.totalMilliSeconds = milliSeconds;
        this.milliStep = milliSeconds;
        this.isSleeping = new AtomicBoolean(false);
        this.isStopRequested = new AtomicBoolean(false);
        this.isTimeOutLock = new Object();
        start();
    }

    void resetTimer() {
        milliSecondsLeft = totalMilliSeconds;
    }

    boolean isTimeout() {
        synchronized (isTimeOutLock) {
            return isTimeout;
        }
    }

    void stopTimer() {
        synchronized (isTimeOutLock) {
            if (!isTimeout) {
                if (isAlive() && isSleeping.get())
                    this.interrupt();
                isStopRequested.set(true);
            }
        }
    }

    @Override
    public synchronized void start() {
        resetTimer();
        super.start();
    }

    @Override
    public void run() {
        do {
            isSleeping.set(true);
            try {
                Thread.sleep(milliStep);
            } catch (InterruptedException e) {
                isSleeping.set(false);
                return;
            }
            isSleeping.set(false);
            if (callback != null && milliSecondsLeft != 0)
                callback.onTick(milliSecondsLeft);
            milliSecondsLeft -= milliStep;
        } while (milliSecondsLeft > 0 && !isStopRequested.get());
        synchronized (isTimeOutLock) {
            isTimeout = true;
        }
        if (callback != null && !isStopRequested.get()) callback.onTimeOut();
    }

    interface TimeOutCallback {
        void onTimeOut();

        void onTick(long milliSecondsLeft);
    }
}
