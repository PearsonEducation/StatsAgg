package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Helper {
    
    private static final Logger logger = LoggerFactory.getLogger(Helper.class.getName());

    protected final static String pageNumber = "page_number";
    protected final static String pageSize = "page_size";
    protected final static String error = "error";
    protected final static String errorMsg = "Invalid request parameters.";
    protected final static String noResult = "No results found!";
    protected final static String name = "name";
    
    protected final static String ERROR_JSON = "{\"Error\":\"Not Found\"}";
    
    public static JSONObject getRequestData(HttpServletRequest request) {
        BufferedReader bufferedReader = null;
        JSONObject jsonObject = null;
        
        try {
            String line = null;
            StringBuilder requestData = new StringBuilder();
            bufferedReader = request.getReader();
            while ((line = bufferedReader.readLine()) != null) requestData.append(line);
            jsonObject = (JSONObject) JSONValue.parse(requestData.toString());
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return jsonObject;
    }
    
    /*
    Creates a simple JSON string in the format of "{\"Message\":\"myMessage\"}";
    where 'myMessage' is the 'message' parameter
    */
    public static String createSimpleJsonResponse(String message) {
        if (message == null || message.isEmpty()) return "{\"Message\":\"myMessage\"}";
        else return "{\"Message\":\"" + message + "\"}";
    }
    
    public static JsonObject getJsonObjectFromRequetBody(HttpServletRequest request) {
        
        if (request == null) {
            return null;
        }
        
        BufferedReader bufferedReader = null;
        JsonObject jsonObject = null;
        
        try {
            StringBuilder requestData = new StringBuilder();
            bufferedReader = request.getReader();
            String currentLine = null;
            while ((currentLine = bufferedReader.readLine()) != null) requestData.append(currentLine);
            
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(requestData.toString());
            jsonObject = new Gson().toJsonTree(jsonElement).getAsJsonObject();
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return jsonObject;
    }
    
    protected static Integer getIntegerFieldFromJsonObject(JsonObject jsonObject, String fieldName) {
        
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
    
    protected static String getStringFieldFromJsonObject(JsonObject jsonObject, String fieldName) {
        
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
    
    protected static Boolean getBooleanFieldFromJsonObject(JsonObject jsonObject, String fieldName) {
        
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
    
    protected static JsonObject getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(JsonObject jsonObject, String time_FieldName, String timeUnit_FieldName) {
        
        if (jsonObject == null) {
            return null;
        }
        
        try {
            JsonElement time_jsonElement = jsonObject.get(time_FieldName);
            JsonElement timeUnit_JsonElement = jsonObject.get(timeUnit_FieldName);

            if ((time_jsonElement != null) && (timeUnit_JsonElement != null)) {
                long currentField_JsonElement_Long = time_jsonElement.getAsLong();
                int timeUnit_Int = timeUnit_JsonElement.getAsInt();
                BigDecimal time_BigDecimal = DatabaseObjectCommon.getValueForTimeFromMilliseconds(currentField_JsonElement_Long, timeUnit_Int);
                
                jsonObject.remove(time_FieldName);
                JsonBigDecimal time_JsonBigDecimal = new JsonBigDecimal(time_BigDecimal);
                jsonObject.addProperty(time_FieldName, time_JsonBigDecimal);
                
                jsonObject.remove(timeUnit_FieldName);
                jsonObject.addProperty(timeUnit_FieldName, DatabaseObjectCommon.getTimeUnitStringFromCode(timeUnit_Int, false));
            }
            else if (time_jsonElement != null) {
                jsonObject.remove(time_FieldName);
            }
            else if (timeUnit_JsonElement != null) {
                jsonObject.remove(timeUnit_FieldName);
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return jsonObject;
    }
  
}
