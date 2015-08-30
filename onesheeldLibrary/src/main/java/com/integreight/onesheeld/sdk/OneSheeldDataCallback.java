package com.integreight.onesheeld.sdk;

/**
 * Represents various data events for {@link OneSheeldDevice}.
 * <p>Should be extended and passed to {@link OneSheeldDevice} to get notified
 * about pins status change, raw data receive or shield frame receive.</p>
 * @see OneSheeldDevice
 */
public abstract class OneSheeldDataCallback {
    /**
     * This method gets called once an input pin in 1Sheeld board changes its
     * value.
     *
     * @param pinNumber the pin number
     * @param newValue the new value
     */
    public void onDigitalPinStatusChange(int pinNumber, boolean newValue) {

    }

    /**
     * This method gets called for every byte received on the serial pins of
     * 1Sheeld board.
     *
     * @param data the received byte
     */
    public void onSerialDataReceive(int data) {

    }

    /**
     * This method gets called for every {@link ShieldFrame} received on the
     * serial pins of 1Sheeld board.
     *
     * @param frame the frame
     * @see ShieldFrame
     */
    public void onShieldFrameReceive(ShieldFrame frame) {

    }

    /**
     * This method gets called for every {@link ShieldFrame} of a
     * {@link KnownShield} received on the serial pins of 1Sheeld board.
     *
     * <p>For a <tt>ShieldFrame</tt> to be known, its shield id and function id
     * should be present in one of {@link KnownShield}s in {@link KnownShields}
     * list. </p>
     *
     * @param knownShield the known shield
     * @param frame the frame
     * @see ShieldFrame
     * @see KnownShield
     * @see KnownShields
     */
    public void onKnownShieldFrameReceive(KnownShield knownShield, ShieldFrame frame) {

    }
}
