package com.pearson.statsagg.utilities.json_utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class.getName());

    public static Integer getIntegerFieldFromJsonObject(JsonObject jsonObject, String fieldName) {
        
        if (jsonObject == null) {
            return null;
        }
        
        Integer returnInteger = null;
        
        try {
            JsonElement jsonElement = jsonObject.get(fieldName);
            if (jsonElement != null) returnInteger = jsonElement.getAsInt();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));    
        }

        return returnInteger;
    }
    
    public static String getStringFieldFromJsonObject(JsonObject jsonObject, String fieldName) {
        
        if (jsonObject == null) {
            return null;
        }
        
        String returnString = null;
        
        try {
            JsonElement jsonElement = jsonObject.get(fieldName);
            
            if (jsonElement != null) {
                JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                returnString = jsonPrimitive.getAsString();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));    
        }

        return returnString;
    }
    
    public static Boolean getBooleanFieldFromJsonObject(JsonObject jsonObject, String fieldName) {
        
        if (jsonObject == null) {
            return null;
        }
        
        Boolean returnBoolean = null;
        
        try {
            JsonElement jsonElement = jsonObject.get(fieldName);
            if (jsonElement != null) returnBoolean = jsonElement.getAsBoolean();
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));    
        }

        return returnBoolean;
    }
    
    public static JsonObject getJsonObjectFromRequestBody(String json) {
        
        if (json == null) {
            return null;
        }
        
        JsonObject jsonObject = null;
        
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(json);
            jsonObject = new Gson().toJsonTree(jsonElement).getAsJsonObject();
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        
        return jsonObject;
    }
    
    public static JsonElement getJsonElementFromRequestBody(String json) {
        
        if (json == null) {
            return null;
        }
        
        JsonElement jsonElement = null;
        
        try {
            JsonParser parser = new JsonParser();
            jsonElement = parser.parse(json);
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        
        return jsonElement;
    }
    
}
