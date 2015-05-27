package com.pearson.statsagg.utilities;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Json {

    private static final Logger logger = LoggerFactory.getLogger(Json.class.getName());

    public static String convertBoxedPrimativeNumberToString(Object numberObject) {
        
        if (numberObject == null) return null;
        
        String valueString = null;
        
        if (numberObject instanceof Integer) valueString = Integer.toString((Integer) numberObject);
        else if (numberObject instanceof Long) valueString = Long.toString((Long) numberObject);
        else if (numberObject instanceof Short) valueString = Short.toString((Short) numberObject);
        else if (numberObject instanceof Byte) valueString = Byte.toString((Byte) numberObject);
        else if (numberObject instanceof Double) valueString = Double.toString((Double) numberObject);
        else if (numberObject instanceof Float) valueString = Float.toString((Float) numberObject);
        else if (numberObject instanceof Boolean) {
            Boolean numberObjectBoolean = (Boolean) numberObject;
            if (numberObjectBoolean) valueString = "1";
            else valueString = "0";
        }
        
        return valueString;
    }
        
    public static BigDecimal convertBoxedPrimativeNumberToBigDecimal(Object numberObject) {
        
        if (numberObject == null) return null;
        
        BigDecimal valueBigDecimal = null;
        
        if (numberObject instanceof Integer) valueBigDecimal = new BigDecimal(Integer.toString((Integer) numberObject));
        else if (numberObject instanceof Long) valueBigDecimal = new BigDecimal(Long.toString((Long) numberObject));
        else if (numberObject instanceof Short) valueBigDecimal = new BigDecimal(Short.toString((Short) numberObject));
        else if (numberObject instanceof Byte) valueBigDecimal = new BigDecimal(Byte.toString((Byte) numberObject));
        else if (numberObject instanceof Double) valueBigDecimal = new BigDecimal(Double.toString((Double) numberObject));
        else if (numberObject instanceof Float) valueBigDecimal = new BigDecimal(Float.toString((Float) numberObject));
        else if (numberObject instanceof Boolean) {
            Boolean numberObjectBoolean = (Boolean) numberObject;
            if (numberObjectBoolean) valueBigDecimal = BigDecimal.ONE;
            else valueBigDecimal = BigDecimal.ZERO;
        }
        
        return valueBigDecimal;
    }
    
    public static boolean isObjectNumberic(Object object) {
        
        if (object == null) return false;
                
        if (object instanceof Integer) return true;
        else if (object instanceof Long) return true;
        else if (object instanceof Short) return true;
        else if (object instanceof Byte) return true;
        else if (object instanceof Double) return true;
        else if (object instanceof Float) return true;
        else if (object instanceof Boolean) return true;
        
        return false;
    }
    
}
