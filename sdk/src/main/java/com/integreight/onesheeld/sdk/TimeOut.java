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
