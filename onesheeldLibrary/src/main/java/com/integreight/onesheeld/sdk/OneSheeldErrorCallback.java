package com.integreight.onesheeld.sdk;

/**
 * Represents an error event.
 */
public abstract class OneSheeldErrorCallback {
    /**
     * This method gets called when an error occurs in either
     * {@link OneSheeldDevice} or {@link OneSheeldManager}.
     *
     * @param device the device if the error is device related or null it is not.
     * @param error the error
     */
    public void onError(OneSheeldDevice device, OneSheeldError error) {

    }
}
