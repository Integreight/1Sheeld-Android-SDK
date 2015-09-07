# 1Sheeld SDK for Android #

## Overview ##

The 1Sheeld SDK for Android is a library that allows you to scan, connect and interact with 1Sheeld boards through your custom Android app. The SDK is compatible with Android 2.3 or above and requires devices with Bluetooth connectivity.

It allows you to:
- Scan for 1Sheelds.
- Connect to multiple 1Sheelds at once. (Up to seven devices)
- Send, receive and broadcast raw data and frames.

In Addition to:
- Automatic connection retry in case of connection failure with 3 different approaches.
- Query Arduino library and firmware versions.
- Knowing if the received frame is one of the known shields frames.
- Control digital pins status and read their values.
- Send and receive raw bytes.
- Mute and Unmute communication with 1Sheeld.
- Broadcast frames of raw data to multiple devices at once.
- Queue frames in case Arduino is in a callback.

...and more.

**IMPORTANT:  By default, this library will only connect to Bluetooth devices that has 1Sheeld in its name.**

## Installation ##

Make sure JCenter is added to your build file's list of repositories. (It is added by default for new projects created with the latest version of Android Studio)

```groovy
repositories {
    jcenter()
}
```

to use the JCenter Repository

```groovy
dependencies {
    ...
    compile 'com.integreight.onesheeld:sdk:1.0.0'
    ...
}
```

or replace 1.0.0 with the version you wish to use.

Then initialize the SDK in the onCreate() method of your application class or main activity.

```java
// Pass the context to the init method.
OneSheeldSdk.init(applicationContext);
// Optional, enable dubbing messages.
OneSheeldSdk.setDebugging(true);
```

## Usage ##

Here is an example that scan, connect to the first found device, and controls its digital pins.

```java
// Init the SDK with context
OneSheeldSdk.init(this);
// Optional, enable dubbing messages.
OneSheeldSdk.setDebugging(true);

// Get the manager instance
OneSheeldManager manager = OneSheeldSdk.getManager();
// Set the connection failing retry count to 1
manager.setConnectionRetryCount(1);
// Set the automatic connecting retries to true, this will use 3 different methods for connecting
manager.setAutomaticConnectingRetries(true);

// Construct a new OneSheeldScanningCallback callback and override onDeviceFind method
OneSheeldScanningCallback scanningCallback = new OneSheeldScanningCallback() {
                                              @Override
                                              public void onDeviceFind(OneSheeldDevice device) {
                                                  // Cancel scanning before connecting
                                                  OneSheeldSdk.getManager().cancelScanning();
                                                  // Connect to the found device
                                                  device.connect();
                                              }
                                          };

// Construct a new OneSheeldConnectionCallback callback and override onConnect method
OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
                                              @Override
                                              public void onConnect(OneSheeldDevice device) {
                                                  // Output high on pin 13
                                                  device.digitalWrite(13,true);

                                                  // Read the value of pin 12
                                                  boolean isHigh=device.digitalRead(12);
                                              }
                                          };

// Add the connection and scanning callbacks
manager.addConnectionCallback(connectionCallback);
manager.addScanningCallback(scanningCallback);

// Initiate the Bluetooth scanning
manager.scan();
```

## Sample Application ##

![Screenshot of the SDK's sample application](http://i.imgur.com/JLoBce4.png)

The sample application allows you to:
- Scan, and connect to multiple 1Sheeld devices at once.
- Control each device digital pins.
- Simulate the [push button shield](http://1sheeld.com/shields/push-button-shield/) of the official app and send/broadcast its on/off frames.

## Building The SDK ##

Just clone the repo and open it with the latest version of Android Studio.

## Contribution ##

Contributions are welcomed, please follow this pattern:
- Fork the repo.
- Open an issue with your proposed feature or bug fix.
- Commit and push code to your forked repo.
- Submit a pull request.

Don't forget to drop us an email, post on our forum, or mention us on Twitter or Facebook about what you have did with the SDK, we would love to hear about it.

## Learn More ##
 - [JavaDoc documentation](http://1sheeld.com/AndroidSDK/JavaDocs/1.0.0/).
 - Play with [Our Sample SDK App](https://github.com/Integreight/1Sheeld-Android-SDK/tree/master/sampleApplication)
 - Download [1Sheeld App](https://play.google.com/store/apps/details?id=com.integreight.onesheeld) from Play Store.
 - Check our [1Sheeld Forums](http://www.1sheeld.com/forum) where you can post your questions and get answers.

## Changelog ##

To see what has changed in recent versions of 1Sheeld Android SDK, see the [Change Log](CHANGELOG.md).

## License and Copyright ##

```
This code is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License version 3 only, as
published by the Free Software Foundation.

This code is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
version 3 for more details (a copy is included in the LICENSE file that
accompanied this code).

Please contact Integreight, Inc. at info@integreight.com or post on our
support forums www.1sheeld.com/forum if you need additional information
or have any questions.
```
