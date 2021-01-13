/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.bridgefy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import com.bridgefy.sdk.client.Config;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.apache.cordova.bridgefy.framework.BridgefySDK;
import org.apache.cordova.bridgefy.framework.Utils;
import org.json.JSONArray;
import org.json.JSONException;

import me.bridgefy.sdk.cordova.BuildConfig;

public class Bridgefy extends CordovaPlugin {

    public static final String TAG = "BridgefyPlugin";
    protected final static String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final String BRIDGEFY_INITIALIZE = "initialize";
    private static final String BRIDGEFY_START = "start";
    private static final String BRIDGEFY_STOP = "stop";
    private static final String BRIDGEFY_DEVICE_LISTENER = "deviceListener";
    private static final String BRIDGEFY_MESSAGE_LISTENER = "messageListener";
    private static final String BRIDGEFY_MESSAGE_SEND = "sendMessage";
    private static final String BRIDGEFY_MESSAGE_SEND_BROADCAST = "sendBroadcastMessage";
    private static final String BRIDGEFY_CONNECT_DEVICE = "connectDevice";
    private static final String BRIDGEFY_DISCONNECT_DEVICE = "disconnectDevice";

    public static final int PERMISSION_DENIED_ERROR = 403;
    public static final int START_ACTION = 0;
    public static final int SAVE_TO_ALBUM_SEC = 1;

    public CallbackContext callbackContext;

    private String api_key = "";

    private BridgefySDK bridgefySDK;
    private CordovaInterface cordova;
    private CordovaWebView webView;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        this.cordova = cordova;
        this.webView = webView;
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, String.format("execute action: %s, args: %s ", action, args.toString()));
        this.callbackContext = callbackContext;
        switch (action) {
            case BRIDGEFY_INITIALIZE: {
                this.api_key = args.getString(0);
                if (checkPermissionsGranted()) {
                    cordova.getThreadPool().execute(() -> { initSDK(api_key); });
                } else {
                    PermissionHelper.requestPermissions(Bridgefy.this, START_ACTION, permissions);
                }
                break;
            }
            case BRIDGEFY_START: {
                cordova.getThreadPool().execute(() -> { startSDK(callbackContext, args); });
                break;
            }
            case BRIDGEFY_STOP: {
                stopSDK(callbackContext);
                break;
            }
            case BRIDGEFY_DEVICE_LISTENER: {
                deviceActions(callbackContext);
                break;
            }
            case BRIDGEFY_MESSAGE_LISTENER: {
                messageActions(callbackContext);
                break;
            }
            case BRIDGEFY_MESSAGE_SEND: {
                sendMessage(callbackContext, args.getString(0), args.getString(1));
                break;
            }
            case BRIDGEFY_MESSAGE_SEND_BROADCAST: {
                sendBroadcastMessage(callbackContext, args.getString(0));
                break;
            }

            case BRIDGEFY_CONNECT_DEVICE: {
                connectDevice(callbackContext, args.getString(0));
                break;
            }

            case BRIDGEFY_DISCONNECT_DEVICE: {
                disconnectDevice(callbackContext, args.getString(0));
                break;
            }
            default:
                return false;
        }
        return true;
    }

    /**
     *  Check permissions granted
     * @return checkPermissionsGranted
     */
    private boolean checkPermissionsGranted() {
        for(int index = 0; index < permissions.length; index++) {
            if (!PermissionHelper.hasPermission(this, permissions[index])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        switch (requestCode) {
            case START_ACTION:
                initSDK(this.api_key);
                break;
        }
    }

    /**
     * Bridgefy init SDK with API KEY
     * @param apikey
     */
    private void initSDK(String apikey) {
        if (bridgefySDK == null) {
            this.bridgefySDK = new BridgefySDK.Builder()
                    .setApiKey(apikey)
                    .setCallbackContext(callbackContext)
                    .setCordovaInterface(cordova)
                    .setCordovaWebView(webView)
                    .setDebugMode(BuildConfig.DEBUG)
                    .build();
        }

        bridgefySDK.init();
    }

    /**
     * Start bridgefy operations
     * @param callbackContext
     * @param args
     */
    private void startSDK(CallbackContext callbackContext, JSONArray args) {
        Config options = Utils.toBridgefyOptions(args);
        bridgefySDK.start(callbackContext, options);
    }

    /**
     * Stop Bridgefy operations
     * @param callbackContext
     */
    private void  stopSDK(CallbackContext callbackContext) {
        bridgefySDK.stop(callbackContext);
    }

    /**
     * Set device actions
     * @param callbackContext
     */
    void deviceActions(CallbackContext callbackContext) {
        bridgefySDK.deviceActions(callbackContext);
    }

    /**
     * Set message actions
     * @param callbackContext
     */
    void messageActions(CallbackContext callbackContext) {
        bridgefySDK.messageActions(callbackContext);
    }

    /**
     * Send message
     * @param callbackContext
     * @param device
     * @param message
     */
    void sendMessage(CallbackContext callbackContext, String device, String message) {
        bridgefySDK.sendMessage(callbackContext, device, message);
    }

    /**
     * Send Broadcast message
     * @param callbackContext
     * @param message
     */
    void sendBroadcastMessage(CallbackContext callbackContext, String message) {
        bridgefySDK.sendBroadcastMessage(callbackContext, message);
    }

    /**
     * Connect device by user Id
     * @param callbackContext
     * @param userId
     */
    void connectDevice(CallbackContext callbackContext, String userId) {
        bridgefySDK.connectDevice(callbackContext, userId);
    }

    /**
     * Disconnect device by user Id
     * @param callbackContext
     * @param userId
     */
    void disconnectDevice(CallbackContext callbackContext, String userId) {
        bridgefySDK.disconnectDevice(callbackContext, userId);
    }
}
