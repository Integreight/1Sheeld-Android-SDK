# 1Sheeld SDK for Android [![Build Status](https://travis-ci.org/Integreight/1Sheeld-Android-SDK.svg?branch=master)](https://travis-ci.org/Integreight/1Sheeld-Android-SDK) #

## Overview ##

The 1Sheeld SDK for Android is a library that allows you to scan, connect and interact with 1Sheeld boards through your custom Android app. The SDK is compatible with Android 2.3 or above and requires devices with Bluetooth connectivity.

It allows you to:
- Scan for 1Sheelds (either classic or plus).
- Connect to multiple 1Sheelds at once, up to seven different ones. (either classic or plus)
- Send, receive and broadcast raw data and frames.
- Test, rename and change the communications baud rate of the connected boards.

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

Make sure JCenter is added to your project gradle.build file's list of repositories. (It is added by default for new projects created with the latest version of Android Studio)

```groovy
repositories {
    jcenter()
}
```

to use the JCenter repository, add the following line to the dependencies section of your module's ```build.gradle```.

```groovy
dependencies {
    ...
    compile 'com.integreight.onesheeld:sdk:2.0.0'
    ...
}
```

You can replace 2.0.0 with the version you wish to use. (Review the [releases page](https://github.com/Integreight/1Sheeld-Android-SDK/releases) to know the version numbers of the old releases.)

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
// Optional, enable debugging messages.
OneSheeldSdk.setDebugging(true);

// Get the manager instance
OneSheeldManager manager = OneSheeldSdk.getManager();
// Set the connection failing retry count to 1
manager.setConnectionRetryCount(1);
// Set the automatic connecting retries to true, this will use 3 different methods for connecting
manager.setAutomaticConnectingRetriesForClassicConnections(true);

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

![Screenshot of the SDK's sample application](http://i.imgur.com/epuaEFd.png)

The sample application allows you to:
- Scan, and connect to multiple 1Sheeld devices at once.
- Control each device digital pins.
- Simulate the [push button shield](http://1sheeld.com/shields/push-button-shield/) of the official app and send/broadcast its on/off frames.
- Query and change the board’s communications baud rate.
- Send and receive serial data.
- Rename the board with a random name.
- Test the board, both firmware and library.

## Building The SDK ##

The repo is a generic Gradle project, it was built and tested using the latest stable version of Android Studio.

To build the project and generate the release and debug aar(s), run this command on the root of the repo:

```
.\gradlew assemble
```

## Required Android Permissions ##

*android.permission.BLUETOOTH*: Required for connecting and communicating with paired Bluetooth devices.

*android.permission.BLUETOOTH_ADMIN*: Required for discovering and pairing Bluetooth devices.

*android.permission.ACCESS_COARSE_LOCATION*: Required for Bluetooth discovery starting from Android 6.0.

The first two permissions are implicitly added for you when you include the SDK in you project, you don't have to add them to your manifest.

## Contribution ##

Contributions are welcomed, please follow this pattern:
- Fork the repo.
- Open an issue with your proposed feature or bug fix.
- Commit and push code to a new branch in your forked repo.
- Submit a pull request to our *development* branch.

Don't forget to drop us an email, post on our forum, or mention us on Twitter or Facebook about what you have did with the SDK, we would love to hear about it.

## Learn More ##
 - [JavaDoc documentation](http://1sheeld.com/AndroidSDK/JavaDocs/2.0.0/).
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
