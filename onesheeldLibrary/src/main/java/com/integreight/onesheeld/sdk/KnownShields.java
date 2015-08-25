package com.integreight.onesheeld.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class KnownShields implements List<KnownShield> {
    private static KnownShields instance = null;
    public final KnownShield KEYPAD_SHIELD = new KnownShield((byte) 0x75, "Keypad Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x75, "", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("", ArgumentType.BYTE, 10, true));
        }}, true, true));
    }});
    public final KnownShield GPS_SHIELD = null;
    public final KnownShield SLIDER_SHIELD = null;
    public final KnownShield PUSH_BUTTON_SHIELD = null;
    public final KnownShield TOGGLE_BUTTON_SHIELD = null;
    public final KnownShield GAMEPAD_SHIELD = null;
    public final KnownShield PROXIMITY_SENSOR_SHIELD = null;
    public final KnownShield MIC_SHIELD = null;
    public final KnownShield TEMPERATURE_SENSOR_SHIELD = null;
    public final KnownShield LIGHT_SENSOR_SHIELD = null;
    public final KnownShield PRESSURE_SENSOR_SHIELD = null;
    public final KnownShield GRAVITY_SENSOR_SHIELD = null;
    public final KnownShield ACCELEROMETER_SENSOR_SHIELD = null;
    public final KnownShield GYROSCOPE_SENSOR_SHIELD = null;
    public final KnownShield ORIENTATION_SENSOR_SHIELD = null;
    public final KnownShield MAGNETOMETER_SENSOR_SHIELD = null;
    public final KnownShield PHONE_SHIELD = null;
    public final KnownShield SMS_SHIELD = null;
    public final KnownShield CLOCK_SHIELD = null;
    public final KnownShield KEYBOARD_SHIELD = null;
    public final KnownShield TWITTER_SHIELD = null;
    public final KnownShield VOICE_RECOGNIZER_SHIELD = null;
    public final KnownShield TERMINAL_SHIELD = null;
    public final KnownShield FACEBOOK_SHIELD = null;
    public final KnownShield NOTIFICATION_SHIELD = null;
    public final KnownShield SEVEN_SEGMENT_SHIELD = null;
    public final KnownShield SKYPE_SHIELD = null;
    public final KnownShield MUSIC_PLAYER_SHIELD = null;
    public final KnownShield EMAIL_SHIELD = null;
    public final KnownShield FOURSQUARE_SHIELD = null;
    public final KnownShield CAMERA_SHIELD = null;
    public final KnownShield BUZZER_SHIELD = null;
    public final KnownShield LED_SHIELD = null;
    public final KnownShield LCD_SHIELD = null;
    public final KnownShield TEXT_TO_SPEECH_SHIELD = null;
    public final KnownShield DATA_LOGGER_SHIELD = null;
    public final KnownShield PATTERN_SHIELD = null;
    public final KnownShield INTERNET_SHIELD = null;
    public final KnownShield COLOR_DETECTOR_SHIELD = null;
    public final KnownShield NFC_SHIELD = null;
    public final KnownShield GLCD_SHIELD = null;
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
            if (knownShield != null) shieldsIds.add(knownShield.getId());
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
