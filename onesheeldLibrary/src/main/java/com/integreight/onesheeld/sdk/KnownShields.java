package com.integreight.onesheeld.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class KnownShields implements List<KnownShield> {
    private static KnownShields instance = null;
    public KnownShield KEYPAD_SHIELD = new KnownShield((byte) 0x00, "Keypad Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x00, "", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("", ArgumentType.BYTE, 10, true));
        }}, true, true));
    }});
    public KnownShield GPS_SHIELD;
    public KnownShield SLIDER_SHIELD;
    public KnownShield PUSH_BUTTON_SHIELD;
    public KnownShield TOGGLE_BUTTON_SHIELD;
    public KnownShield GAMEPAD_SHIELD;
    public KnownShield PROXIMITY_SENSOR_SHIELD;
    public KnownShield MIC_SHIELD;
    public KnownShield TEMPERATURE_SENSOR_SHIELD;
    public KnownShield LIGHT_SENSOR_SHIELD;
    public KnownShield PRESSURE_SENSOR_SHIELD;
    public KnownShield GRAVITY_SENSOR_SHIELD;
    public KnownShield ACCELEROMETER_SENSOR_SHIELD;
    public KnownShield GYROSCOPE_SENSOR_SHIELD;
    public KnownShield ORIENTATION_SENSOR_SHIELD;
    public KnownShield MAGNETOMETER_SENSOR_SHIELD;
    public KnownShield PHONE_SHIELD;
    public KnownShield SMS_SHIELD;
    public KnownShield CLOCK_SHIELD;
    public KnownShield KEYBOARD_SHIELD;
    public KnownShield TWITTER_SHIELD;
    public KnownShield VOICE_RECOGNIZER_SHIELD;
    public KnownShield TERMINAL_SHIELD;
    public KnownShield FACEBOOK_SHIELD;
    public KnownShield NOTIFICATION_SHIELD;
    public KnownShield SEVEN_SEGMENT_SHIELD;
    public KnownShield SKYPE_SHIELD;
    public KnownShield MUSIC_PLAYER_SHIELD;
    public KnownShield EMAIL_SHIELD;
    public KnownShield FOURSQUARE_SHIELD;
    public KnownShield CAMERA_SHIELD;
    public KnownShield BUZZER_SHIELD;
    public KnownShield LED_SHIELD;
    public KnownShield LCD_SHIELD;
    public KnownShield TEXT_TO_SPEECH_SHIELD;
    public KnownShield DATA_LOGGER_SHIELD;
    public KnownShield PATTERN_SHIELD;
    public KnownShield INTERNET_SHIELD;
    public KnownShield COLOR_DETECTOR_SHIELD;
    public KnownShield NFC_SHIELD;
    public KnownShield GLCD_SHIELD;
    private List<KnownShield> knownShields;
    private ArrayList<Byte> shieldsIds;

    private KnownShields() {
        knownShields = new ArrayList<>();
        knownShields.add(KEYPAD_SHIELD);
        knownShields.add(GPS_SHIELD);
        knownShields.add(SLIDER_SHIELD);
        knownShields.add(PUSH_BUTTON_SHIELD);
        knownShields.add(TOGGLE_BUTTON_SHIELD);
        knownShields.add(GAMEPAD_SHIELD);
        knownShields.add(PROXIMITY_SENSOR_SHIELD);
        knownShields.add(MIC_SHIELD);
        knownShields.add(TEMPERATURE_SENSOR_SHIELD);
        knownShields.add(LIGHT_SENSOR_SHIELD);
        knownShields.add(PRESSURE_SENSOR_SHIELD);
        knownShields.add(GRAVITY_SENSOR_SHIELD);
        knownShields.add(ACCELEROMETER_SENSOR_SHIELD);
        knownShields.add(GYROSCOPE_SENSOR_SHIELD);
        knownShields.add(ORIENTATION_SENSOR_SHIELD);
        knownShields.add(MAGNETOMETER_SENSOR_SHIELD);
        knownShields.add(PHONE_SHIELD);
        knownShields.add(SMS_SHIELD);
        knownShields.add(CLOCK_SHIELD);
        knownShields.add(KEYBOARD_SHIELD);
        knownShields.add(TWITTER_SHIELD);
        knownShields.add(VOICE_RECOGNIZER_SHIELD);
        knownShields.add(TERMINAL_SHIELD);
        knownShields.add(FACEBOOK_SHIELD);
        knownShields.add(NOTIFICATION_SHIELD);
        knownShields.add(SEVEN_SEGMENT_SHIELD);
        knownShields.add(SKYPE_SHIELD);
        knownShields.add(MUSIC_PLAYER_SHIELD);
        knownShields.add(EMAIL_SHIELD);
        knownShields.add(FOURSQUARE_SHIELD);
        knownShields.add(CAMERA_SHIELD);
        knownShields.add(BUZZER_SHIELD);
        knownShields.add(LED_SHIELD);
        knownShields.add(LCD_SHIELD);
        knownShields.add(TEXT_TO_SPEECH_SHIELD);
        knownShields.add(DATA_LOGGER_SHIELD);
        knownShields.add(PATTERN_SHIELD);
        knownShields.add(INTERNET_SHIELD);
        knownShields.add(COLOR_DETECTOR_SHIELD);
        knownShields.add(NFC_SHIELD);
        knownShields.add(GLCD_SHIELD);

        shieldsIds = new ArrayList<>();
        for (KnownShield knownShield : knownShields) {
            shieldsIds.add(knownShield.getId());
        }
    }

    static KnownShields getInstance() {
        if (instance == null) {
            synchronized (KnownShields.class) {
                if (instance == null) {
                    instance = new KnownShields();
                }
            }
        }
        return instance;
    }

    @Override
    public void add(int location, KnownShield object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(KnownShield object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int location, Collection<? extends KnownShield> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends KnownShield> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object) {
        return knownShields.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return knownShields.containsAll(collection);
    }

    @Override
    public KnownShield get(int location) {
        return knownShields.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return knownShields.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return knownShields.isEmpty();
    }

    @Override
    public Iterator<KnownShield> iterator() {
        return knownShields.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return knownShields.lastIndexOf(object);
    }

    @Override
    public ListIterator<KnownShield> listIterator() {
        return knownShields.listIterator();
    }

    @Override
    public ListIterator<KnownShield> listIterator(int location) {
        return knownShields.listIterator(location);
    }

    @Override
    public KnownShield remove(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KnownShield set(int location, KnownShield object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return knownShields.size();
    }

    @Override
    public List<KnownShield> subList(int start, int end) {
        return knownShields.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return knownShields.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return knownShields.toArray(array);
    }

    public boolean contains(byte shieldId) {
        return shieldsIds.contains(shieldId);
    }

    public KnownShield getKnownShield(byte shieldId) {
        for (int i = 0; i < knownShields.size(); i++)
            if (knownShields.get(i).getId() == shieldId) return knownShields.get(i);
        return null;
    }
}
