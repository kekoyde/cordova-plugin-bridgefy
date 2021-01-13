package org.apache.cordova.bridgefy.framework;

import com.bridgefy.sdk.client.BFEnergyProfile;
import com.bridgefy.sdk.client.BFEngineProfile;
import com.bridgefy.sdk.client.Config;
import com.bridgefy.sdk.client.Message;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {
    private Utils(){}

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static Config toBridgefyOptions(JSONArray optionsArray) {
        Config.Builder builder = new Config.Builder();
        try {
            JSONObject options = optionsArray.getJSONObject(0);
            boolean autoConnect = options.getBoolean("autoConnect");
            boolean encryption = options.getBoolean("encryption");
            String engineProfile = options.getString("engineProfile");
            String energyProfile = options.getString("energyProfile");

            BFEngineProfile bfEngineProfile = getEngineProfile(engineProfile);
            BFEnergyProfile bfEnergyProfile = getEnergyProfile(energyProfile);

            builder.setAutoConnect(autoConnect)
                    .setEnergyProfile(bfEnergyProfile)
                    .setEngineProfile(bfEngineProfile)
                    .setEncryption(encryption);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    private static BFEnergyProfile getEnergyProfile(String energyProfile) {
        BFEnergyProfile bfEnergyProfile = BFEnergyProfile.HIGH_PERFORMANCE;
        switch (energyProfile) {
            case "ENERGY_SAVER":
                bfEnergyProfile = BFEnergyProfile.ENERGY_SAVER;
                break;
            case "BALANCED":
                bfEnergyProfile = BFEnergyProfile.BALANCED;
                break;
            case "HIGH_PERFORMANCE":
                bfEnergyProfile = BFEnergyProfile.HIGH_PERFORMANCE;
                break;
        }
        return bfEnergyProfile;
    }

    private static BFEngineProfile getEngineProfile(String engineProfileString) {
        BFEngineProfile engineProfile = BFEngineProfile.BFConfigProfileLongReach;

        switch (engineProfileString) {
            case "BFConfigProfileDefault":
                engineProfile = BFEngineProfile.BFConfigProfileDefault;
                break;
            case "BFConfigProfileHighDensityNetwork":
                engineProfile = BFEngineProfile.BFConfigProfileHighDensityNetwork;
                break;
            case "BFConfigProfileLongReach":
                engineProfile = BFEngineProfile.BFConfigProfileLongReach;
                break;
            case "BFConfigProfileNoFowarding":
                engineProfile = BFEngineProfile.BFConfigProfileNoFowarding;
                break;
            case "BFConfigProfileShortReach":
                engineProfile = BFEngineProfile.BFConfigProfileShortReach;
                break;
            case "BFConfigProfileSparseNetwork":
                engineProfile = BFEngineProfile.BFConfigProfileSparseNetwork;
                break;
        }

        return engineProfile;
    }
}
