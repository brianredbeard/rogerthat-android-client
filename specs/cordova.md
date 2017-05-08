# How to use Cordova based brandings

## What is a branding
Basically its just a **HTML app** where you can use some native features of our apps.

## Whats the difference with Cordova brandings?
A cordova branding is just like all other brandings. However you don't need to import **rogerthat.js** to use our native app features.

Cordova has lots of plugins you can use, however by default we only package our **RogerthatPlugin**. If you need another plugin for your application please contact us.

All these plugins expose native api's via a Javascript API. You have camera, contacts and so many other plugins allowing you to make more advanced applications.

## Zip layout
- cordova.html
- css
  - app.css
- js
  - app.js

## cordova.html
Import Cordova -> ```<script type="text/javascript" src="cordova.js"></script>```

As you see in the zip layout you don't need to provide this Javascipt file.

## app.js
A **deviceready** event will be triggered when Cordova is fully loaded.

```javascript
var app = {
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },
    onDeviceReady: function() {
      // Cordova is fully loaded
    },
};

app.initialize();
```

If your HTML app uses jquery it can be that jquery is ready loading before or after the deviceready event.

# RogerthatPlugin

Source can be found at [RogerthatPlugin.js](https://github.com/rogerthat-platform/cordova-rogerthat-plugin/blob/master/www/RogerthatPlugin.js)

## Functions

- [rogerthat.user](#rogerthat.user)
- [rogerthat.service](#rogerthat.service)
- [rogerthat.system](#rogerthat.system)
- [rogerthat.message](#rogerthat.message)
- [rogerthat.camera](#rogerthat.camera)
- [rogerthat.security](#rogerthat.security)
- [rogerthat.features](#rogerthat.features)
- [rogerthat.ui](#rogerthat.ui)
- [rogerthat.util](#rogerthat.util)
- [rogerthat.callbacks](#rogerthat.callbacks)
- [rogerthat.api](#rogerthat.api)


## <a name="rogerthat.user"></a>rogerthat.user
```javascript
{
    "name": "John Doe",
    "account": "john.doe@foo.com",
    "avatarUrl": "https://rogerth.at/unauthenticated/mobi/cached/avatar/4824964683268096",
    "language": "pt_BR",
    "data": {"username": "john", "password": "doe"}
}
```
- **rogerthat.user.name**: retrieve the user name
- **rogerthat.user.account**: retrieve the user account
- **rogerthat.user.avatarUrl**: retrieve the user avatar
- **rogerthat.user.language**: retrieve the user language. format: ```[ISO 639-1 language code]_[ISO 3166-2 region code]```
- **rogerthat.user.data**: retrieve the user data
- **rogerthat.user.put()**: save all you have put in user data before

Example:
```javascript
rogerthat.user.data.username = "John";
rogerthat.user.data.password = "Doe";
rogerthat.user.put();
```

## <a name="rogerthat.service"></a>rogerthat.service
```javascript
{
    "name": "Service identity 1",
    "account": "s1@foo.com/i1",
    "data": {"room": "chatroom1"}
}
```
- **rogerthat.service.name**: retrieve service name
- **rogerthat.service.account**: retrieve the service account
- **rogerthat.service.data**: retrieve the service data
- **rogerthat.service.getBeaconsInReach(function(beacons){})**: list beacons which are in reach

Example:
```javascript
var name = rogerthat.service.name;
var account = rogerthat.sevice.account;
var data = rogerthat.service.data;
var room = rogerthat.service.data.room;
rogerthat.service.getBeaconInReach(function(beacons){
    console.log(beacons);
}, function(error){
     console.log("Error occurred while checking if beacon was in range... (Should never happen)");
});
```

beacons: array with beacon objects:
- **uuid**: string which contains beacon universal unique identifier
- **major**: string which contains the major of the beacon
- **minor**: string which contains the minor of the beacon
- **tag**: string which contains beacon tag
- **proximity**: integer which defines the proximity:
  - BEACON_PROXIMITY_UNKNOWN = 0
  - BEACON_PROXIMITY_IMMEDIATE = 1
  - BEACON_PROXIMITY_NEAR = 2
  - BEACON_PROXIMITY_FAR = 3

## <a name="rogerthat.system"></a>rogerthat.system
```javascript
{
    "os": "ios",
    "version": "7.1",
    "appVersion": "1.0.150.I"
}
```

- **rogerthat.system.os**: retrieve the system os, can be:
  - ios
  - android
  - unknown
- **rogerthat.system.version**: retrieve system version (“unknown” if the version is not available)
- **rogerthat.system.appVersion**: retrieve the version of the Rogerthat application of your system, the result will be “unknown” if the version is previous than the 1.0.150.I for iOS or 1.0.1002.A for Android
- **rogerthat.system.onBackendConnectivityChanged(function(result) {})**: start receiving Internet connectivity changes.

Example:
```javascript
var os = rogerthat.system.os;
var version = rogerthat.system.version;
var appVersion = rogerthat.system.appVersion;

rogerthat.callbacks.onBackendConnectivityChanged(function(isConnected) {
    if (isConnected) {
        console.log('We are now connected to the Internet');
    } else {
        console.log('There is no Internet connectivity');
    }
});

rogerthat.system.onBackendConnectivityChanged(function(result) {
    // From now on, we will receive updates on rogerthat.callbacks.onBackendConnectivityChanged
    if (result.connected) {
        console.log('We are connected to the Internet');
    } else {
        console.log('There is no Internet connectivity');
    }
});
```

## <a name="rogerthat.message"></a>rogerthat.message
- **rogerthat.message.open(messageKey, function(){}, function(){})**: open an existing conversation

Example:
```javascript
var messageKey = "...";  // The message key. Could be a chat or a flow or any other message key.
var onSuccess = function() {
    console.log("The message is successfully opened by the app");
};
var onError = function(error) {
    // error: {"type": "MessageNotFound"}
    console.log("The message is not found");
};

rogerthat.message.open(messageKey, onSuccess, onError);
```

## <a name="rogerthat.camera"></a>rogerthat.camera
- **rogerthat.camera.startScanningQrCode(cameraType, onSuccess, onError)**: open the camera to start scanning for QR codes.
- **rogerthat.camera.stopScanningQrCode(cameraType, onSuccess, onError)**: close the camera to stop scanning for QR codes.

The **cameraType** parameter is unused for the moment but we added it the be conform with **rogerthat.js**

See [rogerthat.callbacks](#rogerthat.callbacks) for an example.

## <a name="rogerthat.security"></a>rogerthat.security
- **rogerthat.security.sign(message, payload, forcePin, onSuccess, onError)**: Sign the hash of the provided payload
  - **message**: An optional string containing the message which is shown at the moment the user is asked to enter his PIN code.
  - **payload**: A string containing the date that needs to be signed.
  - **forcePin**: Normally the user shouldn’t enter his PIN more than once in 5 minutes. Within these 5 minutes the payload is automatically signed. You can overrule this behavior by setting forcePin to true.
  - **onSuccess**: The method that will be called with the signed payload.
  - **onError**: The method that will be called when something went wrong.
- **rogerthat.security.verify(payload, signature, onSuccess, onError)**: Verify a signature for a certain payload.

Example:
```javascript
var message = 'To confirm this payment please enter your pin code';
var payload = JSON.stringify({amount: 500, fromUser: "jane@foo.com", toUser: "john@foo.com"});

var onError = function(error) {
    // error: {"exception": "<The error message>"}
    console.log(error);
};

rogerthat.security.sign(message, payload, true, function(signature) {
    console.log('The payload has been successfully signed.');

    rogerthat.security.verify(payload, signature, function(result) {
        // result: {"valid": true}
        if (result.valid) {
            console.log('The signature has been successfully verified.');
        } else {
            console.log('The signature was not valid!');
        }
    }, onError);
}, onError);
```

## <a name="rogerthat.features"></a>rogerthat.features
- **rogerthat.features.base64URI**: check if the user’s device supports loading images via base64 encoded data
- **rogerthat.features.backgroundSize**: check if the user’s device supports CSS3
- **rogerthat.features.beacons**: check if the user’s device has iBeacon support
- **rogerthat.features.callback**: a callback which will be called after the availability of a feature has been verified

The result can take these values:
- FEATURE_CHECKING = 0
- FEATURE_SUPPORTED = 1
- FEATURE_NON_SUPPORTED = 2

Example:
```javascript
function alertAfterFeaturesChecked (feature) {
    if (feature) {
        var supported =  rogerthat.features[feature] == FEATURE_SUPPORTED;
        console.log("Feature " + feature + " is " + (supported ? "" : "not") + "  supported!");
    }

    if (rogerthat.features.base64URI === FEATURE_CHECKING
            || rogerthat.features.backgroundSize === FEATURE_CHECKING
            || rogerthat.features.beacons === FEATURE_CHECKING) {

        // wait until all features are verified
        rogerthat.features.callback = alertAfterFeaturesChecked;
        return;
    }

    alert('All necessary features are verified.')
};

rogerthat.callbacks.ready(function () {
    alertAfterFeaturesChecked();
});
```

## <a name="rogerthat.ui"></a>rogerthat.ui
- **rogerthat.ui.hideKeyboard()**: Hides the android keyboard.

## <a name="rogerthat.util"></a>rogerthat.util
- **rogerthat.util.uuid()**: Generate a random UUID.
- **rogerthat.util.playAudio(path, callback)**: Play a sound file which is located in the branding zip.
- **rogerthat.util.isConnectedToInternet(callback)**: Check the Internet connectivity.

Example:
```javascript
var guid = rogerthat.util.uuid(); // Eg. 1d50c98d-9314-4e5d-8abc-be6373e027e2

rogerthat.util.playAudio('sounds/notification.mp3', function() {
    console.log('You should be able to hear the sound right now');
});

rogerthat.util.isConnectedToInternet(function(result) {
    console.log('Connected to Internet? ' + result.connected);
    console.log('Connected to WiFi? ' + result.connectedToWifi);
});
```

## <a name="rogerthat.callbacks"></a>rogerthat.callbacks
- **rogerthat.callbacks.ready(function(){})**: Rogerthat user and service data has been set
- **rogerthat.callbacks.userDataUpdated(function(){})**: The app received an update and rogerthat.user.data is updated.
- **rogerthat.callbacks.serviceDataUpdated(function(){})**: The app received an update and rogerthat.service.data is updated.
- **rogerthat.callbacks.onBackendConnectivityChanged(function(result){})**: The device its Internet connectivity has changed.
- **rogerthat.callbacks.onBeaconInReach(function(beacon){})**: The app detected a beacon.
- **rogerthat.callbacks.onBeaconOutOfReach(function(beacon){})**: The user went out of reach of a beacon.
- **rogerthat.callbacks.qrCodeScanned(function(result){})**: A QR code has been scanned as result of - rogerthat.camera.startScanningQrCode

Example:
```javascript
rogerthat.callbacks.ready(function(){
    console.log("You received a ready callback");
});

rogerthat.callbacks.userDataUpdated(function(){
    console.log("User data updated");
});

rogerthat.callbacks.serviceDataUpdated(function(){
    console.log("Service data updated");
});

// --- Internet ----------------------------

rogerthat.callbacks.onBackendConnectivityChanged(function(isConnected) {
    console.log(isConnected ? 'We are connected to the Internet' : 'There is no Internet connectivity');
});

rogerthat.system.onBackendConnectivityChanged(function(result) {
    // From now on, we will receive updates on rogerthat.callbacks.onBackendConnectivityChanged
    console.log(result.connected ? 'We are connected to the Internet' : 'There is no Internet connectivity');
});

// --- Beacons ----------------------------

rogerthat.callbacks.onBeaconInReach(function(beacon){
    console.log(beacon)
};

rogerthat.callbacks.onBeaconOutOfReach(function(beacon){
    console.log(beacon);
});

// --- Camera ----------------------------

var onCameraError = function(error) {
    // error: {"exception": "<The error message>"}
    console.log("An error occurred,", error);
};
var onCameraStarted = function() {
    console.log("The camera has been opened. We can soon expect the qrCodeScanned callback to be triggered.");
};
var onCameraStopped = function() {
    console.log("The camera has been closed.");
};

rogerthat.callbacks.qrCodeScanned(function(result) {
    /*
    This method is called twice. If the smartphone is connected to the Internet,
    the app will request the details of the scanned QR code.

    Example result for the first invocation:
    {
        "status": "resolving",
        "content": "https://rogerth.at/S/-I44M9SX411"
    }

    Example result for the second invocation:
    {
        "status": "resolved",
        "content": "https://rogerth.at/S/-I44M9SX411",
        "userDetails": {
            "appId": "rogerthat",
            "name": "Jane Doe",
            "email": "jane@foo.com"
        }
    }

    Example result in case of error:
    {
        "status": "error",
        "content": "<The error message>"
    }
    */
    console.log("The camera detected a QR code.", result);
});

rogerthat.camera.startScanningQrCode("back", onCameraStarted, onCameraError);
```

## <a name="rogerthat.api"></a>rogerthat.api
This method will result in triggering the [system.api_call](http://www.rogerthat.net/developers/api-reference/#TPS_receives_a_API_call) service callback, wether the user is online on invocation or not. If the user is not online on invocation, the api call will be executed as soon as the user is connected to the internet again. The result of the system.api_call service callback, is delivered via the resultReceived callback, documented below.

- **rogerthat.api.call(method,params,tag)**: call the API
  - **method**: string with the call method
  - **params**: JSON string with the call params
  - **tag**: string with the call tag

```javascript
rogerthat.api.call("add_to_calender",{"eventId": "5754903989321728"}, "tag1")
```

- **rogerthat.api.callbacks.resultReceived(function(method,result,error,tag){})**: receive the call result
  - **method**: string with the call method
  - **result**: JSON string with the result of the call
  - **error**: string with code error
  - **tag**: string with the call tag

```javascript
rogerthat.api.callbacks.resultReceived(function(method,result,error,tag){
    console.log(method); // "add_to_calendar"
    console.log(result); // {"success": "true"}
    console.log(error); // null
    console.log(tag); // "tag1"
}),
```
