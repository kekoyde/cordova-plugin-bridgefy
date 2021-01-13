package org.apache.cordova.bridgefy.framework.mvp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public interface Provider {
    CallbackContext getCallbackCOntext();

    void updateCallbackContext(CallbackContext callbackContext);

    /**
     * Send a plugin result back to JavaScript.
     * (This is a convenience method)
     *
     * @param result
     */
    void sendPluginResult(PluginResult result);
}
