<p align="center"><a href="https://bridgefy.me" target="_blank" rel="noopener noreferrer"><img width="400" src="https://admin.bridgefy.me/assets/img/bridgefy-logo.svg" alt="Vue logo"></a></p>

![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=http%3A%2F%2F104.196.228.98%3A8081%2Fartifactory%2Flibs-release-local%2Fcom%2Fbridgefy%2Fandroid-sdk%2Fmaven-metadata.xml) ![Cocoapods](https://img.shields.io/cocoapods/v/BFTransmitter) ![GitHub package.json version](https://img.shields.io/github/package-json/v/bridgefy/cordova-plugin-bridgefy) ![GitHub last commit](https://img.shields.io/github/last-commit/bridgefy/cordova-plugin-bridgefy) ![GitHub issues](https://img.shields.io/github/issues-raw/bridgefy/cordova-plugin-bridgefy?style=plastic)

# cordova-plugin-bridgefy

This plugin allows the client to transmit data to nearby peers without the necessity of an Internet connection or an access point. 
It also implements a mesh-network transmission algorithm that enables devices to send data to peers that are so far away that they aren't visible to the client device.

```js
document.addEventListener("deviceready", onDeviceReady, false);
function onDeviceReady() {
    console.log(bridgefy.version);
}
```

## Installation

    cordova plugin add cordova-plugin-bridgefy

## Supported Platforms

- Android
- iOS

### Android Quirks

This guide will show you the necessary steps to start using the Bridgefy SDK on your app. The first step is to generate an API key at http://bridgefy.me

##### Added AndroidX Support
Bridgefy requires AndroidX support, you can enable this feature by setting the `AndroidXEnabled` preference to true in `config.xml`.
```xml
<preference name="AndroidXEnabled" value="true" />
```
**App Requirements**

The Bridgefy SDK supports Android 5.0 (**API Level 21**) or higher and the following permissions are required:

```java
android.permission.BLUETOOTH
android.permission.BLUETOOTH_ADMIN
android.permission.INTERNET
```
Internet access is required during the first run in order to check for a valid license in our servers.

If you're targeting devices with Android  6.0 (**API Level 23**) or higher, either one of the following permissions is also required:

```java
android.permission.ACCESS_FINE_LOCATION
```
```java
android.permission.ACCESS_COARSE_LOCATION
```

**Note.- Devices with Android 6.0 (API Level 23) or higher also need to have Location Services enabled**


**Hardware requirements**

This version is fine-tuned for Bluetooth Low Energy (BLE) capable devices. While it is not required, it is preferable that the _BLE advertising mode_ is also supported. The Bridgefy SDK will let you know during initialization if your devices support _BLE advertising_ or not. At least one device should support advertising mode in order for connections and, thus, messaging functionality, to be successful.

### iOS Quirks 
Go to your Bridgefy Dashboard, then go to your Licence List and download the .txt file inside, which contains a String. 

![Dashboard licence](https://raw.githubusercontent.com/bridgefy/bridgefy-android-samples/master/img/dashboard.png)  

Copy the certificate string that was generated along with your app's API Key. 

In info.plist, create an entry named `com.bridgefy.SDKLicense` and paste the text value. If there is no Internet connection available, the Transmitter Framework will use this certificate to validate your licence.

![Dashboard licence](https://raw.githubusercontent.com/bridgefy/bridgefy-ios-developer/master/img/cert.png)

---

#### Setting Permissions
We need to define a value for the `NSBluetoothAlwaysUsageDescription` key if using iOS13 or `NSBluetoothPeripheralUsageDescription` if using iOS12 or earlier. This is the message that is displayed to the user when asking for Bluetooth permission for the app. If we are designing for apps to be used on phones with iOS13 and previous versions as well then it makes sense to define values for both of these keys. This needs to be done in the *Info.plist* file.

![Permissions](https://raw.githubusercontent.com/bridgefy/bridgefy-android-samples/master/img/Picture14.png)

## Methods

- bridgefy.version
- bridgefy.init
- bridgefy.start
- bridgefy.stop
- bridgefy.deviceListener
- bridgefy.messageListener
- bridgefy.sendMessage
- bridgefy.sendBroadcastMessage

## bridgefy.cordova

Get the version of Cordova running on the device.

## bridgefy.init

The Bridgefy Plugin needs only a call to the `bridgefy.init` in order to create all required objects and to be ready to start operations.

#### Quick Example

```js
// Function successCallback The function to call when the heading data is available
// Function errorCallback The function to call when there is an error getting the heading data.
// ApiKey (Required) generate an API key at https://bridgefy.me
//
                
bridgefy.init( 
    (response) => { // successCallback
            console.log('Bridgefy client: ' + JSON.stringify(response));
        }, 
    (e) => { // errorCallback
        console.error(e);
        }, 
        'YOUR_API_KEY');
```

## bridgefy.start

Before you start, you must `assign listeners` that will report the events of nearby devices and the events of sending packages.
Once the Bridgefy SDK has been correctly registered we are now ready to start the Bridgefy SDK. 
Use the following method to begin the process of nearby devices discovery as well as to advertise presence to other devices.

```javascript
// Function successCallback The function to call when the heading data is available
// Function errorCallback The function to call when there is an error getting the heading data.
// You can also provide a custom Config object to set additional options 
bridgefy.start(successCallback, errorCallback, options);
```

Bridgefy SDK options, for more details, see [Bridgefy Options](https://github.com/bridgefy/bridgefy-android-samples#configuration-options)  - only android

#### Quick Example
```javascript
// Add Device Listener
bridgefy.deviceListener(successCallback, errorCallback);
// Add Message Listener
bridgefy.messageListener(successCallback, errorCallback);

// Start operations
let options = bridgefy.config;
    options.autoConnect = true; // tells Bridgefy if we want Automatic on On-demand mode
    options.encryption = true; // All communicaiton is secure
    options.energyProfile = 'HIGH_PERFORMANCE'; // Android only, see https://github.com/bridgefy/bridgefy-android-samples#engine-profile
    options.engineProfile = 'BFConfigProfileLongReach'; // Android only, see https://github.com/bridgefy/bridgefy-android-samples#energy-profile

bridgefy.start(
    () => {     //   successCallback
                //   Your code
    }, (e)=> {  //   errorCallback
        console.log('Bridgefy error ', JSON.stringify(e));
    },
    options);   //   Options configured
```

## bridgefy.stop

Once you've ready to start the plug-in, always make sure to stop the Bridgefy instance in order to free up device resources.

```js
// Function successCallback The function to call when the stop bridgefy successfully
// Function errorCallback The function to call when there is an error to stop bridgefy.
bridgefy.stop(successCallback, errorCallback);
```

#### Quick Example

```js
// Strop bridgefy
bridgefy.stop(
    () => {
        // Your code
    }, (e) => {
        console.error(e);
    });
```

## Automatic and On-demand modes
You can use Bridgefy in two different modes: "Automatic" and "On-demand".

More information about those modes for [iOS](https://github.com/bridgefy/bridgefy-ios-developer#operation-modes) and [Android](https://github.com/bridgefy/bridgefy-android-samples#starting-operations).

### Automatic mode
When using Bridgefy in Automatic mode, you do not have to manage connecting to users, it is done automatically for you by the Bridgefy SDK.

Each time a user will be connected to you, the `onDeviceConnected` event will be thown along with the user information.

Similarly, the `onDeviceLost` event will be raised every time a user disconnects from you.

### On-demand mode
In the On-demand mode, you will have to connect to other devices once you've detected them. You know when a device is detected when the `onDeviceDetected` event is raised (along with the device information).

When a device is detected, you can call the `connectDevice` function to connect it and be then able to send messages to it:

```javascript

bridgefy.connectDevice(
        (resp) => { },  // --> Success Callback 
        (e)=> { },      // --> Error Callback
        device.userId); // --> User ID

```

If you want to disconnect from a device, you call:
```javascript
bridgefy.disconnectDevice(
        (resp) => { },  // --> Success Callback 
        (e)=> { },      // --> Error Callback
        device.userId); // --> User ID
```

You know when you are connected to a device when the `onDeviceConnected` event is raised (similarly to the automatic mode). You also know when you are disconnected from a device when the `onDeviceLost` event is raised.

When a device becomes unavailable (un-detected), the `onDeviceUnavailable` event is raised along with the device information.


### bridgefy.deviceListener

When a peer is detected, the connection is made automatically. The following successCallback is invoked when a peer has established connection:

    bridgefy.deviceListener(successCallback, errorCallback);

#### Response description
- action: 
    - connected 
    - disconnected
    - detected
    - unavailable
- device 
    - *userId* (UUID) *in iOS this attribute only
    - *deviceAddress* 
    - *sessionId*
    - *antennaType*
    - *deviceName*

### Quick Example

```js
//
// Function successCallback The function to call when exist a event.
// Function errorCallback The function to call when there is an error.
//
bridgefy.deviceListener((response) => {
    // response has a action and device
    // response = {
    //  action: "STATUS", -> Must be "connected" or "disconnected"
    //  device: Object
    // }
    console.log(JSON.stringify(response));
}, (e) => {
    console.error(e);
});
```

## bridgefy.messageListener

When a packet has been received, the following method will be invoked:

    bridgefy.messageListener(successCallback, errorCallback);

#### Response description

- action: 
    - incoming_message 
    - incoming_message_broadcast 
    - message_sent 
    - message_error
    
- message
    - *senderId* - current client UUID 
    - *receiverId* - receiver UUID
    - *data* - binary data of message
    - *hop* - maximum number of jumps
    - *hops* - hops in the mesh network
    - *dateSent* - date time sample: 1575654108172
    - *uuid* - message UUID
    - *content* - received dictionary
    - *progress* - progress 0 to fullSize
    - *fullSize* - outgoing binary data size 

### Quick Example

```js

bridgefy.messageListener((response) => { // successCallback
    console.log('Receive message event: ' + response.action);
    console.log('Receive message content: ' + JSON.stringify(response.message));
}, (e) => { // errorCallback
    console.error(e);
});
```

## bridgefy.sendMessage

The following method is used to send data to another peer. 
This method performs an initial validation over the received parameters, and if everything is correct, 
generates a packet identifier that is returned to the client. 
All of this initial process is synchronous. 
After this, the packet enters to an asynchronous process for the transmission of the packet.

You can send messages to other devices even if they haven't been reported as connected or in-range. 
The Bridgefy SDK will do its best effort to deliver the message to its recipient through intermediate devices. 
Message content is secured through a 256-bit encryption which is managed seamlessly for you so you don't have to worry about other users tapping into your private messages.
```js
bridgefy.sendMessage(successCallback, errorCallback, device, message);
```

#### Parameters

- Function successCallback(`uuid`)
- Function errorCallback(`error`)
- Object device
- Objacet message

#### Quick Example

```js
bridgefy.sendMessage(
                    (res)=>{    // Success Callback
                        console.log('Message sent: ' + res)
                    }, (e)=> { // Error Callback
                        console.error('Error to send message: ', e);
                    },
                    deviceDetected, // Sent to device
                    { message: 'Test' }); // Message
```

## bridgefy.sendBroadcastMessage

You can also send public messages which will be propagated to all nearby devices. Those are even easier to send:

    bridgefy.sendBroadcastMessage(successCallback, errorCallback, message);


#### Parameters

- Function successCallback(`uuid`)
- Function errorCallback(`error`)
- Objacet message

#### Quick Example

```js
bridgefy.sendBroadcastMessage(
                    (res)=>{    // Success Callback
                        console.log('Message sent: ' + res)
                    }, (e)=> { // Error Callback
                        console.error('Error to send message: ', e);
                    },
                    { message: 'Test' }); // Message
```

## Documentation
* [Android documentation](https://github.com/bridgefy/bridgefy-android-samples)
* [iOS documentation](https://github.com/bridgefy/bridgefy-ios-developer)

### Contact ###

* contact@bridgefy.me
