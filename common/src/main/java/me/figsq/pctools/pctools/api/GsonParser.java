package me.figsq.pctools.pctools.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonParser {
    public static String parse(JsonElement jsonElement,String path){
        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("首参一定是JsonObject!");
        }

        if (!path.contains(".")) {
            return ((JsonObject) jsonElement).get(path).getAsString();
        }

        String[] split = path.split("\\.");

        for (String s : split) {
            if (jsonElement.isJsonObject()) {
                jsonElement = ((JsonObject) jsonElement).get(s);
                continue;
            }
            if (jsonElement.isJsonArray()) {
                jsonElement = ((JsonArray) jsonElement).get(Integer.parseInt(s));
                continue;
            }
            if (jsonElement.isJsonPrimitive()){
                return "NO DATA";
            }
            if (jsonElement.isJsonNull()) {
                return "NO DATA";
            }
        }

        return jsonElement.getAsString();
    }
}
