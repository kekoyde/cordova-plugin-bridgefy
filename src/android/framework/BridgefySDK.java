package org.apache.cordova.bridgefy.framework;

import android.util.Log;

import com.bridgefy.sdk.client.BFBleProfile;
import com.bridgefy.sdk.client.BFEnergyProfile;
import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.BridgefyClient;
import com.bridgefy.sdk.client.Config;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.RegistrationListener;
import com.bridgefy.sdk.framework.controller.DeviceManager;
import com.bridgefy.sdk.framework.controller.Session;
import com.bridgefy.sdk.framework.controller.SessionManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.bridgefy.framework.device.DeviceProvider;
import org.apache.cordova.bridgefy.framework.transmitter.MessageProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.bridgefy.sdk.cordova.BuildConfig;

public class BridgefySDK {
    private CallbackContext callbackContext;
    private static String api_key;
    private static BridgefySDK instance;
    private boolean bridgefyFrameworkStarted = false;
    private static final String BRIDGEFY_SERVICE = "BridgefyCordova";
    private static CordovaInterface cordova;
    private static CordovaWebView webView;
    private MessageProvider messageProvider;
    private DeviceProvider deviceProvider;
    private Config defaultConfig = new Config.Builder()
            .setAntennaType(Config.Antenna.BLUETOOTH_LE)
            .setAutoConnect(true)
            .setEncryption(true)
            .setMaxConnectionRetries(10)
            .setBleProfile(BFBleProfile.EXTENDED_RANGE)
            .setEnergyProfile(BFEnergyProfile.BALANCED)
            .setEngineProfile(BFEngineProfile.BFConfigProfileLongReach)
            .build();

    private boolean debugMode = false;

    private RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void onRegistrationSuccessful(BridgefyClient bfyClient) {
            Map client = new HashMap();
            client.put("api_key", bfyClient.getApiKey());
            client.put("bundleId", bfyClient.getBundleId());
            client.put("public_key", bfyClient.getPublicKey());
            client.put("secret_key", bfyClient.getSecretKey());
            client.put("userUuid", bfyClient.getUserUuid());
            Map deviceProfile = new HashMap();
            deviceProfile.put("deviceEvaluation", bfyClient.getDeviceProfile().getDeviceEvaluation());
            deviceProfile.put("rating", bfyClient.getDeviceProfile().getRating());
            client.put("deviceprofile", deviceProfile);
            callbackContext.success(new JSONObject(client));
        }

        @Override
        public void onRegistrationFailed(int errorCode, String message) {
            bridgefyFrameworkStarted = false;
            callbackContext.error(String.format("onRegistrationFailed messgae: %s error code: $d", message, errorCode));
        }
    };

    private BridgefySDK(String api_key, CordovaInterface cordova, CordovaWebView webView, CallbackContext callbackContext, boolean debugMode) {
        this.api_key = api_key;
        this.cordova = cordova;
        this.webView = webView;
        this.callbackContext = callbackContext;
        this.debugMode = debugMode;
    }

    public void init() {
        Bridgefy.debug = this.debugMode;
        Bridgefy.initialize(cordova.getContext().getApplicationContext(), api_key, registrationListener);
    }

    public void start(CallbackContext callbackContext, Config options) {
        if (!bridgefyFrameworkStarted && deviceProvider != null && messageProvider != null) {
            Bridgefy.start(messageProvider, deviceProvider, options);
            callbackContext.success("Birdgefy SDK has started");
            bridgefyFrameworkStarted = true;
        } else {
            if (bridgefyFrameworkStarted)
                callbackContext.error("Bridgefy already started.");
            else
                callbackContext.error("Required callback of device listener and message listener.");
        }
    }

    public void messageActions(CallbackContext callbackContext) {
        if (messageProvider == null) {
            messageProvider = new MessageProvider(callbackContext);
        } else {
            messageProvider.updateCallbackContext(callbackContext);
        }
    }

    public void deviceActions(CallbackContext callbackContext) {
        if (deviceProvider == null) {
            deviceProvider = new DeviceProvider(callbackContext);
        } else {
            deviceProvider.updateCallbackContext(callbackContext);
        }
    }

    public void stop(CallbackContext callbackContext) {
        if (bridgefyFrameworkStarted) {
            Bridgefy.stop();
            bridgefyFrameworkStarted = false;
            callbackContext.success("Birdgefy SDK has stopped.");
        } else {
            callbackContext.error("Error stopped bridgefy SDK.");
        }
    }

    public void sendMessage(CallbackContext callbackContext, String device, String message) {
        try {
            JSONObject jsonObject = new JSONObject(device);
            Device bridgefyDevice = DeviceManager.getDeviceByUserId(jsonObject.getString("userId"));
            JSONObject messageObject = new JSONObject(message);
            callbackContext.success(bridgefyDevice.sendMessage(new HashMap<>(Utils.jsonToMap(messageObject))));
        } catch (JSONException e) {
            Log.e("BridgefySDK", "Error to send message: ", e);
            Map error = new HashMap();
            error.put("error", e.getMessage());
            error.put("device", device);
            error.put("message", message);
            callbackContext.error(new JSONObject(error));
        }
    }

    public void sendBroadcastMessage(CallbackContext callbackContext, String message) {
        try {
            JSONObject messageObject = new JSONObject(message);
            callbackContext.success(Bridgefy.sendBroadcastMessage(new HashMap<>(Utils.jsonToMap(messageObject))));
        } catch (JSONException e) {
            Log.e("BridgefySDK", "Error to send broadcast message: ", e);
            Map error = new HashMap();
            error.put("error", e.getMessage());
            error.put("message", message);
            callbackContext.error(new JSONObject(error));
        }
    }

    public void connectDevice(CallbackContext callbackContext, String userId) {
        Device device = deviceProvider.findDevice(userId);
        if (device != null)
        {
            Bridgefy.getInstance().connectToBridgefyDevice(device);
            Map<String, String> success = new HashMap<>();
            success.put("message", "Trying to connect device: " + userId);
            success.put("userId", userId);
            callbackContext.success(new JSONObject(success));
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Device not found, try again when DeviceListener reported device detected.");
            error.put("userId", userId);
            callbackContext.error(new JSONObject(error));
        }
    }

    public void disconnectDevice(CallbackContext callbackContext, String userId) {
        Session session = SessionManager.getSession(userId);
        if (session != null)
        {
            session.disconnect();
            Map<String, String> success = new HashMap<>();
            success.put("message", "Device disconnected userId: " + userId);
            success.put("userId", userId);
            callbackContext.success(new JSONObject(success));
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Device not connected.");
            error.put("userId", userId);
            callbackContext.error(new JSONObject(error));
        }
    }

    public static class Builder {
        String apiKey = null;
        CordovaInterface cordova;
        CordovaWebView webView;
        CallbackContext callbackContext;
        boolean debugMode = BuildConfig.DEBUG;

        public Builder() {}

        public Builder setApiKey(String API_KEY) {
            this.apiKey = API_KEY;
            return this;
        }

        public Builder setCordovaInterface(CordovaInterface context) {
            cordova = context;
            return this;
        }

        public Builder setCallbackContext(CallbackContext callback) {
            callbackContext = callback;
            return this;
        }

        public Builder setCordovaWebView(CordovaWebView webView) {
            this.webView = webView;
            return this;
        }

        public Builder setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public BridgefySDK build() {
            if (instance == null)
                instance = new BridgefySDK(apiKey, cordova, webView, callbackContext, debugMode);

            return instance;
        }
    }
}
