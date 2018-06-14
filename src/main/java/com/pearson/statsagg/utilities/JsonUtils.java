package com.pearson.statsagg.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import com.pearson.statsagg.webui.api.JsonBigDecimal;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class.getName());

    public static String convertNumericObjectToString(Object jsonObject, boolean treatBooleanAsNumeric) {
        
        if (jsonObject == null) return null;
        
        String valueString = null;
        
        if (jsonObject instanceof Integer) valueString = Integer.toString((Integer) jsonObject);
        else if (jsonObject instanceof Long) valueString = Long.toString((Long) jsonObject);
        else if (jsonObject instanceof Short) valueString = Short.toString((Short) jsonObject);
        else if (jsonObject instanceof Byte) valueString = Byte.toString((Byte) jsonObject);
        else if (jsonObject instanceof Double) valueString = Double.toString((Double) jsonObject);
        else if (jsonObject instanceof Float) valueString = Float.toString((Float) jsonObject);
        else if (jsonObject instanceof BigDecimal) {
            BigDecimal numberObjecBigDecimal = (BigDecimal) jsonObject;
            valueString = numberObjecBigDecimal.stripTrailingZeros().toPlainString();
        }
        else if (jsonObject instanceof BigInteger) {
            BigInteger numberObjectBigInteger = (BigInteger) jsonObject;
            valueString = numberObjectBigInteger.toString();
        }
        else if ((jsonObject instanceof Boolean) && treatBooleanAsNumeric) {
            Boolean numberObjectBoolean = (Boolean) jsonObject;
            if (numberObjectBoolean) valueString = "1";
            else valueString = "0";
        }
        else if ((jsonObject instanceof Boolean) && !treatBooleanAsNumeric) {
            Boolean numberObjectBoolean = (Boolean) jsonObject;
            if (numberObjectBoolean) valueString = "true";
            else valueString = "false";
        }
        
        return valueString;
    }
        
    public static BigDecimal convertNumericObjectToBigDecimal(Object jsonNumericObject, boolean treatBooleanAsNumeric) {
        
        if (jsonNumericObject == null) return null;
        
        BigDecimal valueBigDecimal = null;
        
        if (jsonNumericObject instanceof BigDecimal) valueBigDecimal = (BigDecimal) jsonNumericObject;
        else if (jsonNumericObject instanceof Integer) valueBigDecimal = new BigDecimal(Integer.toString((Integer) jsonNumericObject));
        else if (jsonNumericObject instanceof Long) valueBigDecimal = new BigDecimal(Long.toString((Long) jsonNumericObject));
        else if (jsonNumericObject instanceof Short) valueBigDecimal = new BigDecimal(Short.toString((Short) jsonNumericObject));
        else if (jsonNumericObject instanceof Byte) valueBigDecimal = new BigDecimal(Byte.toString((Byte) jsonNumericObject));
        else if (jsonNumericObject instanceof Double) valueBigDecimal = new BigDecimal(Double.toString((Double) jsonNumericObject));
        else if (jsonNumericObject instanceof Float) valueBigDecimal = new BigDecimal(Float.toString((Float) jsonNumericObject));
        else if (jsonNumericObject instanceof BigInteger) valueBigDecimal = new BigDecimal((BigInteger) jsonNumericObject);
        else if ((jsonNumericObject instanceof Boolean) && treatBooleanAsNumeric) {
            Boolean numberObjectBoolean = (Boolean) jsonNumericObject;
            if (numberObjectBoolean) valueBigDecimal = BigDecimal.ONE;
            else valueBigDecimal = BigDecimal.ZERO;
        }

        return valueBigDecimal;
    }
    
    public static boolean isObjectNumberic(Object object, boolean treatBooleanAsNumeric) {
        
        if (object == null) return false;
                
        if (object instanceof Integer) return true;
        else if (object instanceof Long) return true;
        else if (object instanceof Short) return true;
        else if (object instanceof Byte) return true;
        else if (object instanceof Double) return true;
        else if (object instanceof Float) return true;
        else if (object instanceof BigDecimal) return true;
        else if (object instanceof BigInteger) return true;
        else if ((object instanceof Boolean) && treatBooleanAsNumeric) return true;
        
        return false;
    }
    
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
    
    public static JsonObject getApiFriendlyJsonObject_CorrectTimesAndTimeUnits(JsonObject jsonObject, String time_FieldName, String timeUnit_FieldName) {
        
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
