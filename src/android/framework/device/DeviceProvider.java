package org.apache.cordova.bridgefy.framework.device;
import com.bridgefy.sdk.client.Device;
import com.bridgefy.sdk.client.Session;
import com.bridgefy.sdk.client.StateListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.bridgefy.framework.mvp.Provider;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceProvider extends StateListener implements Provider {

    private final String TAG = "DeviceProvider";
    private final Object object = new Object();
    private CallbackContext callbackContext;
    private final String DEVICE_CONNECTED = "connected",
            DEVICE_LOST = "disconnected",
            DEVICE_DETECTED = "detected",
            DEVICE_UNAVAILABLE = "unavailable";

    private List<Device> deviceDetected = new ArrayList<>();

    public DeviceProvider(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onDeviceConnected(Device device, Session session) {
        removeDevice(device);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getResponse(device, DEVICE_CONNECTED));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public void onDeviceLost(Device device) {
        removeDevice(device);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getResponse(device, DEVICE_LOST));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public void onDeviceDetected(Device device) {
        addDevice(device);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getResponse(device, DEVICE_DETECTED));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public void onDeviceUnavailable(Device device) {
        removeDevice(device);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getResponse(device, DEVICE_UNAVAILABLE));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public CallbackContext getCallbackCOntext() {
        return this.callbackContext;
    }

    @Override
    public void updateCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void sendPluginResult(PluginResult result) {
        getCallbackCOntext().sendPluginResult(result);
    }

    @Override
    public void onStopped() {
        synchronized (object) {
            deviceDetected.clear();
        }
    }

    public Device findDevice(String userId) {
        synchronized (object) {
            for (Device deviceQueued : deviceDetected) {
                if (deviceQueued.getUserId().equalsIgnoreCase(userId.trim())) {
                    return deviceQueued;
                }
            }
            return null;
        }
    }

    private void removeDevice(Device device) {
        synchronized (object) {
            deviceDetected.remove(device);
        }
    }

    private void addDevice(Device device) {
        synchronized (object) {
            deviceDetected.add(device);
        }
    }

    JSONObject getResponse(Device device, String type) {
        Map deviceMap = new HashMap();
        deviceMap.put("crc", device.getCrc());
        deviceMap.put("deviceAddress", device.getDeviceAddress());
        deviceMap.put("antennaType", device.getAntennaType().toString());
        deviceMap.put("deviceName", device.getDeviceName());
        deviceMap.put("sessionId", device.getSessionId());
        deviceMap.put("userId", device.getUserId());

        Map response = new HashMap();
        response.put("device", deviceMap);
        response.put("action", type);

        return new JSONObject(response);
    }
}
