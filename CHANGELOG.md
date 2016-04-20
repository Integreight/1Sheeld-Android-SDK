## v2.1.1(160420) (April 20, 2016)
  - Fixed a bug where BLE connection attempts get stuck in progress indefinitely sometimes.

## v2.1.0(160404) (April 4, 2016)
  - Made sure that the verification byte in the received frames is correct.
  - Added the ability to update the board's firmware.
  - Made general refactoring and improvements.

## v2.0.0(160330) (March 30, 2016)
  - Build SDK using the android-23 api.
  - Added the ability to connect to 1Sheeld+ boards (BLE).
  - Made the logs printed using the information tag not the debug tag.
  - Added the ability to rename the boards.
  - Added the ability to test the boards (firmware and library).
  - Added the ability to query and change the boards’ communications baud rate.
  - Made callbacks send with each callback the corresponding device where the event occurred.
  - Added methods to detect the board type. (either classic or plus)
  - Made sending data in the same calling thread not in a dedicated one.
  - Performed various bug fixes and improvements.

## v1.1.0(151108) (November 8, 2015)
  - Updated the project's build-tools, targetSdk, compileSdk and Gradle versions.
  - Fixed a bug in the sample app's pins spinner.
  - Added the location permission to the sample app's manifest.
  - Fixed a bug that prevented the sdk from connecting to 1Sheelds on Android 6.0.
  - Fixed a bug in the parsing 1Sheeld frames.
  - Added the required verification byte to sent frames instead of the instance id.

## v1.0.0(150831) (August 31, 2015)
 - Initial version.