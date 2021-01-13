package org.apache.cordova.bridgefy.framework.transmitter;

import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;
import com.bridgefy.sdk.framework.exceptions.MessageException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.bridgefy.framework.mvp.Provider;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageProvider extends MessageListener implements Provider{

    private final String MESSAGE_OUTGOING_DATA_PROGRESS = "outgoing_data_progress",
                         MESSAGE_INCOMING = "incoming_message",
                         MESSAGE_INCOMING_BROADCAST = "incoming_message_broadcast",
                         MESSAGE_SENT = "message_sent",
                         MESSAGE_ERROR = "message_error";

    private CallbackContext callbackContext;

    public MessageProvider(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public CallbackContext getCallbackCOntext() {
        return callbackContext;
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
    public void onMessageDataProgress(UUID message, long progress, long fullSize) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getMessage(message.toString(), MESSAGE_OUTGOING_DATA_PROGRESS, progress, fullSize));
        pluginResult.setKeepCallback(true);
    }

    @Override
    public void onMessageSent(String messageId) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getMessage(messageId, MESSAGE_SENT));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public void onBroadcastMessageReceived(Message message) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getMessage(message, MESSAGE_INCOMING_BROADCAST));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public void onMessageReceived(Message message) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getMessage(message, MESSAGE_INCOMING));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    @Override
    public void onMessageFailed(Message message, MessageException e) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, getMessageError(e));
        pluginResult.setKeepCallback(true);
        sendPluginResult(pluginResult);
    }

    JSONObject getMessage(Message message, String type) {
        Map response = new HashMap();
        Map messageMap = new HashMap();
        messageMap.put("content", message.getContent());
        messageMap.put("data", message.getData());
        messageMap.put("uuid", message.getUuid());
        messageMap.put("dateSent", message.getDateSent());
        messageMap.put("senderId", message.getSenderId());
        messageMap.put("receiverId", message.getReceiverId());
        messageMap.put("hop", message.getHop());
        messageMap.put("hops", message.getHops());

        response.put("message", messageMap);
        response.put("action", type);

        return new JSONObject(response);
    }

    JSONObject getMessage(String uuid, String type, long progress, long fullSize) {
        Map response = new HashMap();
        response.put("uuid", uuid);
        response.put("action", type);
        response.put("progress", progress);
        response.put("fullSize", fullSize);
        return new JSONObject(response);
    }

    JSONObject getMessage(String uuid, String type) {
        Map response = new HashMap();
        response.put("uuid", uuid);
        response.put("action", type);
        return new JSONObject(response);
    }

    JSONObject getMessageError(MessageException e) {
        Map response = new HashMap();
        response.put("error", e.getMessage());
        response.put("action", MESSAGE_ERROR);
        return new JSONObject(response);
    }
}
