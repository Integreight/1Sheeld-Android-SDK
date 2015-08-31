# 1Sheeld SDK for Android #

## Overview ##

The 1Sheeld SDK for Android is a library that allows you to scan, connect and interact with 1Sheeld boards through your custom Android app. The SDK is compatible with Android 2.3 or above and requires devices with Bluetooth connectivity.

It allows for:
- Scanning for 1Sheelds.
- Connecting to multiple 1Sheelds at once. (Up to seven devices)
- Send, receive and broadcast raw data and frames.

**IMPORTANT:  By default, this library will only connect to Bluetooth devices that has 1Sheeld in its name.**

Learn more:
 - [JavaDoc documentation](http://1sheeld.com/AndroidSDK/JavaDocs/).
 - Play with [Our Sample SDK App](https://github.com/Integreight/1Sheeld-Android-SDK/tree/master/sampleApplication) (includes scanning, connecting, and communicating with 1Sheeld boards).
 - Download [1Sheeld App](https://play.google.com/store/apps/details?id=com.integreight.onesheeld) from Play Store.
 - Check our [1Sheeld Forums](http://www.1sheeld.com/forum) where you can post your questions and get answers.

## Installation ##

Make sure JCenter is added to your build file's list of repositories.

```groovy
repositories {
    jcenter()
}
```

to use the JCenter Repository

```groovy
dependencies {
    ...
    compile 'com.integreight.onesheeld.sdk:android-sdk:1.0.0'
    ...
}
```

or replace 1.0.0 with the version you wish to use.

Then initialize the SDK in the onCreate() method of your application class or main activity.

    ```java
    //  Pass the context to the init method.
    OneSheeldSdk.init(applicationContext);
    // Optional, enable dubbing messages.
    OneSheeldSdk.setDebugging(true);
    ```

## Usage ##

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

  OneSheeldScanningCallback scanningCallback = new OneSheeldScanningCallback() {
                                                  @Override
                                                  public void onScanStart() {

                                                  }

                                                  @Override
                                                  public void onDeviceFind(OneSheeldDevice device) {
                                                      OneSheeldSdk.getManager().cancelScanning();
                                                      device.connect();
                                                  }

                                                  @Override
                                                  public void onScanFinish(List<OneSheeldDevice> foundDevices) {

                                                  }
                                              };

  OneSheeldConnectionCallback connectionCallback = new OneSheeldConnectionCallback() {
                                                  @Override
                                                  public void onConnect(OneSheeldDevice device) {
                                                      // Output high on pin 13
                                                      device.digitalWrite(13,true);

                                                      // Read the value of pin 12
                                                      boolean isHigh=device.digitalRead(12);
                                                  }

                                                  @Override
                                                  public void onDisconnect(OneSheeldDevice device) {

                                                  }

                                                  @Override
                                                  public void onConnectionRetry(OneSheeldDevice device, int retryCount) {

                                                  }
                                              };

  manager.addConnectionCallback(connectionCallback);
  manager.addScanningCallback(scanningCallback);
  manager.scan();

}
```

## Building The SDK ##

Just clone the repo and open it with the latest version of Android Studio.

## Contribution ##

Contributions are welcomed, please follow this pattern:
- Fork the repo.
- Open an issue with your proposed feature or bug fix.
- Commit and push code to your forked repo.
- Submit a pull request.

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
