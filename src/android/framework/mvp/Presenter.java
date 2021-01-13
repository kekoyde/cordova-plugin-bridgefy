package org.apache.cordova.bridgefy.framework.mvp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;

public interface Presenter {
    public void execute(String action, JSONArray args, CallbackContext callbackContext);
    public CordovaInterface getCordova();
    public CordovaWebView getWebView();
}
