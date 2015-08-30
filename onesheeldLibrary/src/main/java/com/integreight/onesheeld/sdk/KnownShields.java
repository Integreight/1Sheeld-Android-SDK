package com.integreight.onesheeld.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a list of all implemented shields in 1Sheeld app.
 * <p>All of the methods for adding or removing elements from list throws
 * <tt>UnsupportedOperationException</tt></p>
 * @see KnownShield
 * @see ArgumentType
 * @see KnownFunction
 * @see KnownArgument
 */
public class KnownShields implements List<KnownShield> {
    private static KnownShields instance = null;
    /**
     * A {@link KnownShield} instance for the keypad shield.
     */
    public final KnownShield KEYPAD_SHIELD = new KnownShield((byte) 0x09, "Keypad Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the GPS shield.
     */
    public final KnownShield GPS_SHIELD = new KnownShield((byte) 0x1C, "GPS Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the slider shield.
     */
    public final KnownShield SLIDER_SHIELD = new KnownShield((byte) 0x01, "Slider Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "SliderShield#setValue", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Value", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the push button shield.
     */
    public final KnownShield PUSH_BUTTON_SHIELD = new KnownShield((byte) 0x03, "Push Button Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the toggle button shield.
     */
    public final KnownShield TOGGLE_BUTTON_SHIELD = new KnownShield((byte) 0x04, "Toggle Button Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the gamepad shield.
     */
    public final KnownShield GAMEPAD_SHIELD = new KnownShield((byte) 0x0C, "Gamepad Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the proximity sensor shield.
     */
    public final KnownShield PROXIMITY_SENSOR_SHIELD = new KnownShield((byte) 0x13, "Proximity Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the mic shield.
     */
    public final KnownShield MIC_SHIELD = new KnownShield((byte) 0x18, "Mic Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the temperature sensor shield.
     */
    public final KnownShield TEMPERATURE_SENSOR_SHIELD = new KnownShield((byte) 0x12, "Temperature Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the light sensor shield.
     */
    public final KnownShield LIGHT_SENSOR_SHIELD = new KnownShield((byte) 0x10, "Light Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the pressure sensor shield.
     */
    public final KnownShield PRESSURE_SENSOR_SHIELD = new KnownShield((byte) 0x11, "Pressure Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the gravity sensor shield.
     */
    public final KnownShield GRAVITY_SENSOR_SHIELD = new KnownShield((byte) 0x14, "Gravity Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the accelerometer sensor shield.
     */
    public final KnownShield ACCELEROMETER_SENSOR_SHIELD = new KnownShield((byte) 0x0B, "Accelerometer Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the gyroscope sensor shield.
     */
    public final KnownShield GYROSCOPE_SENSOR_SHIELD = new KnownShield((byte) 0x0E, "Gyroscope Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the orientation shield.
     */
    public final KnownShield ORIENTATION_SENSOR_SHIELD = new KnownShield((byte) 0x0F, "Orientation Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the magnetometer sensor shield.
     */
    public final KnownShield MAGNETOMETER_SENSOR_SHIELD = new KnownShield((byte) 0x0A, "Magnetometer Sensor Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the phone shield.
     */
    public final KnownShield PHONE_SHIELD = new KnownShield((byte) 0x20, "Phone Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "PhoneShield#call", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Number", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the sms shield.
     */
    public final KnownShield SMS_SHIELD = new KnownShield((byte) 0x0D, "SMS Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "SMSShield#send", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Number", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the clock shield.
     */
    public final KnownShield CLOCK_SHIELD = new KnownShield((byte) 0x21, "Clock Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "ClockShield#queryDateAndTime", new ArrayList<KnownArgument>()));
    }});
    /**
     * A {@link KnownShield} instance for the keyboard shield.
     */
    public final KnownShield KEYBOARD_SHIELD = new KnownShield((byte) 0x22, "Keyboard Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for Twitter shield.
     */
    public final KnownShield TWITTER_SHIELD = new KnownShield((byte) 0x1A, "Twitter Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "TwitterShield#tweet", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Tweet", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x02, "TwitterShield#sendMessage", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Username", ArgumentType.STRING, true, false));
            add(new KnownArgument("Message", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x03, "TwitterShield#tweetLastPicture", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Picture Text", ArgumentType.STRING, true, false));
            add(new KnownArgument("Picture Source", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x04, "TwitterShield#trackKeyword", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Keyword", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x05, "TwitterShield#untrackKeyword", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Keyword", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the voice recognizer shield.
     */
    public final KnownShield VOICE_RECOGNIZER_SHIELD = new KnownShield((byte) 0x24, "Voice Recognizer Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "VoiceRecognitionShield#start", new ArrayList<KnownArgument>()));
    }});
    /**
     * A {@link KnownShield} instance for the terminal shield.
     */
    public final KnownShield TERMINAL_SHIELD = new KnownShield((byte) 0x26, "Terminal Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for Facebook shield.
     */
    public final KnownShield FACEBOOK_SHIELD = new KnownShield((byte) 0x19, "Facebook Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "FacebookShield#post", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Status", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x02, "FacebookShield#postLastPicture", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Picture Description", ArgumentType.STRING, true, false));
            add(new KnownArgument("Picture Source", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the notifications shield.
     */
    public final KnownShield NOTIFICATION_SHIELD = new KnownShield((byte) 0x06, "Notification Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "NotificationShield#notifyPhone", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Message", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the seven segment shield.
     */
    public final KnownShield SEVEN_SEGMENT_SHIELD = new KnownShield((byte) 0x07, "Seven Segment Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "SeventSegmentShield#setValue", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Segments Values", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x02, "SeventSegmentShield#setDot", new ArrayList<KnownArgument>()));

    }});
    /**
     * A {@link KnownShield} instance for Skype shield.
     */
    public final KnownShield SKYPE_SHIELD = new KnownShield((byte) 0x1F, "Skype Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "SkypeShield#call", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Username", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x02, "SkypeShield#videoCall", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Username", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the music player shield.
     */
    public final KnownShield MUSIC_PLAYER_SHIELD = new KnownShield((byte) 0x1D, "Music Player Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "MusicPlayerShield#stop", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x02, "MusicPlayerShield#play", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x03, "MusicPlayerShield#pause", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x04, "MusicPlayerShield#previous", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x05, "MusicPlayerShield#next", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x06, "MusicPlayerShield#seekForward", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Seconds", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x07, "MusicPlayerShield#seekBackward", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Seconds", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x08, "MusicPlayerShield#setVolume", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Value", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the email shield.
     */
    public final KnownShield EMAIL_SHIELD = new KnownShield((byte) 0x1E, "Email Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "EmailShield#send", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Email Address", ArgumentType.STRING, true, false));
            add(new KnownArgument("Subject", ArgumentType.STRING, true, false));
            add(new KnownArgument("Message", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x02, "EmailShield#attachLastPicture", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Email Address", ArgumentType.STRING, true, false));
            add(new KnownArgument("Subject", ArgumentType.STRING, true, false));
            add(new KnownArgument("Message", ArgumentType.STRING, true, false));
            add(new KnownArgument("Picture Source", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the Foursquare shield.
     */
    public final KnownShield FOURSQUARE_SHIELD = new KnownShield((byte) 0x1B, "Foursquare Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "FoursquareShield#checkIn", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Place Id", ArgumentType.STRING, true, false));
            add(new KnownArgument("Message", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the camera shield.
     */
    public final KnownShield CAMERA_SHIELD = new KnownShield((byte) 0x15, "Camera Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "CameraShield#rearCapture", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x02, "CameraShield#setFlash", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Value", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x03, "CameraShield#frontCapture", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x04, "CameraShield#setQuality", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Value", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the buzzer shield.
     */
    public final KnownShield BUZZER_SHIELD = new KnownShield((byte) 0x08, "Buzzer Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "BuzzerShield#setValue", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Value", ArgumentType.BOOLEAN, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the led shield.
     */
    public final KnownShield LED_SHIELD = new KnownShield((byte) 0x02, "LED Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "LedShield#setValue", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Value", ArgumentType.BOOLEAN, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the LCD shield.
     */
    public final KnownShield LCD_SHIELD = new KnownShield((byte) 0x17, "LCD Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x02, "LCDShield#clear", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x03, "LCDShield#home", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x04, "LCDShield#noBlink", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x05, "LCDShield#blink", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x06, "LCDShield#noCursor", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x07, "LCDShield#cursor", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x08, "LCDShield#scrollDisplayLeft", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x09, "LCDShield#scrollDisplayRight", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x0A, "LCDShield#leftToRight", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x0B, "LCDShield#rightToLeft", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x0C, "LCDShield#autoScroll", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x0D, "LCDShield#noAutoScroll", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x0E, "LCDShield#setCursor", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("X", ArgumentType.BYTE, 1, false));
            add(new KnownArgument("Y", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the text to speech shield.
     */
    public final KnownShield TEXT_TO_SPEECH_SHIELD = new KnownShield((byte) 0x23, "Text To Speech Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "TTSShield#say", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Text", ArgumentType.STRING, true, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the data logger shield.
     */
    public final KnownShield DATA_LOGGER_SHIELD = new KnownShield((byte) 0x25, "Data Logger Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "DataLoggerShield#start", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("File Name", ArgumentType.STRING, true, true));
        }}));
        add(new KnownFunction((byte) 0x02, "DataLoggerShield#stop", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x03, "DataLoggerShield#add", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Key", ArgumentType.STRING, true, false));
            add(new KnownArgument("Float Value", ArgumentType.FLOAT, 4, false));
        }}));
        add(new KnownFunction((byte) 0x04, "DataLoggerShield#add", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Key", ArgumentType.STRING, true, false));
            add(new KnownArgument("String Value", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x05, "DataLoggerShield#log", new ArrayList<KnownArgument>()));
    }});
    /**
     * A {@link KnownShield} instance for the pattern shield.
     */
    public final KnownShield PATTERN_SHIELD = new KnownShield((byte) 0x27, "Pattern Shield", new ArrayList<KnownFunction>());
    /**
     * A {@link KnownShield} instance for the Internet shield.
     */
    public final KnownShield INTERNET_SHIELD = new KnownShield((byte) 0x29, "Internet Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "HttpRequest", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Url", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x02, "HttpRequest#setUrl", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Url", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x03, "HttpRequest#addHeader", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Header Name", ArgumentType.STRING, true, false));
            add(new KnownArgument("Header Value", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x04, "HttpRequest#addParameter", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Parameter Name", ArgumentType.STRING, true, false));
            add(new KnownArgument("Parameter Value", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x15, "HttpRequest#addRawData", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Data", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x05, "HttpRequest#deleteHeaders", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
        }}));
        add(new KnownFunction((byte) 0x06, "HttpRequest#deleteParameters", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
        }}));
        add(new KnownFunction((byte) 0x07, "HttpRequest#setContentType", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Content Type", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x16, "HttpRequest#setParametersContentEncoding", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Parameters Content Encoding", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x08, "HttpRequest#ignoreResponse", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
        }}));
        add(new KnownFunction((byte) 0x12, "HttpResponse#getTheseBytes", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Start", ArgumentType.INTEGER, 4, false));
            add(new KnownArgument("Size", ArgumentType.INTEGER, 2, false));
        }}));
        add(new KnownFunction((byte) 0x11, "HttpResponse#dispose", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
        }}));
        add(new KnownFunction((byte) 0x13, "HttpResponse#getHeader", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Header Name", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x09, "InternetShield#performGet", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Callbacks Requested", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x0A, "InternetShield#performPost", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Callbacks Requested", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x0B, "InternetShield#performPut", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Callbacks Requested", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x0C, "InternetShield#performDelete", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Callbacks Requested", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x0D, "InternetShield#cancelAllRequests", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x0E, "InternetShield#setBasicAuthentication", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Username", ArgumentType.STRING, true, false));
            add(new KnownArgument("Password", ArgumentType.STRING, true, false));
        }}));
        add(new KnownFunction((byte) 0x0F, "InternetShield#clearBasicAuthentication", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x10, "InternetShield#setIntialResponseMaxBytesCount", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Maximum Bytes Count", ArgumentType.INTEGER, 2, false));
        }}));
        add(new KnownFunction((byte) 0x14, "JsonKeyChain#query", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Types", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Json Array Number", ArgumentType.INTEGER, 2, true, true));
            add(new KnownArgument("Json Key", ArgumentType.STRING, true, true, true));
        }}));
        add(new KnownFunction((byte) 0x17, "JsonKeyChain#queryArrayLength", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Request Id", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Types", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Json Array Number", ArgumentType.INTEGER, 2, true, true));
            add(new KnownArgument("Json Key", ArgumentType.STRING, true, true, true));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the color detector shield.
     */
    public final KnownShield COLOR_DETECTOR_SHIELD = new KnownShield((byte) 0x05, "Color Detector Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "ColorDetectorShield#setPalette", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Range", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x02, "ColorDetectorShield#enableFullOperation", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x03, "ColorDetectorShield#enableNormalOperation", new ArrayList<KnownArgument>()));
        add(new KnownFunction((byte) 0x04, "ColorDetectorShield#setCalculationMode", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Mode", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x05, "ColorDetectorShield#setPatchSize", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Size", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the NFC shield.
     */
    public final KnownShield NFC_SHIELD = new KnownShield((byte) 0x16, "NFC Shield", new ArrayList<KnownFunction>() {{
        add(new KnownFunction((byte) 0x01, "NFCRecord#queryData", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Record Number", ArgumentType.BYTE, 1, false));
            add(new KnownArgument("Start", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Size", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x02, "NFCRecord#queryType", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Record Number", ArgumentType.BYTE, 1, false));
            add(new KnownArgument("Start", ArgumentType.INTEGER, 2, false));
            add(new KnownArgument("Size", ArgumentType.BYTE, 1, false));
        }}));
        add(new KnownFunction((byte) 0x03, "NFCRecord#queryParsedData", new ArrayList<KnownArgument>() {{
            add(new KnownArgument("Record Number", ArgumentType.BYTE, 1, false));
        }}));
    }});
    /**
     * A {@link KnownShield} instance for the GLCD shield.
     */
    public final KnownShield GLCD_SHIELD = new KnownShield((byte) 0x28, "GLCD Shield", new ArrayList<KnownFunction>());
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
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public void add(int location, KnownShield object) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public boolean add(KnownShield object) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public boolean addAll(int location, Collection<? extends KnownShield> collection) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public boolean addAll(Collection<? extends KnownShield> collection) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
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
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public KnownShield remove(int location) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }
    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
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

    /**
     * Checks whether the shields list contains a shield with a specific id.
     *
     * @param shieldId the id of the shield
     * @return the boolean
     */
    public boolean contains(byte shieldId) {
        return shieldsIds.contains(shieldId);
    }

    /**
     * Gets the shield with a specific id.
     *
     * @param shieldId the shield id
     * @return the known shield or null if the shield can't be found.
     */
    public KnownShield getKnownShield(byte shieldId) {
        for (int i = 0; i < knownShields.size(); i++)
            if (knownShields.get(i).getId() == shieldId) return knownShields.get(i);
        return null;
    }
}
