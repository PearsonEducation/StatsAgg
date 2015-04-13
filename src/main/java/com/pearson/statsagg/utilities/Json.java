package com.pearson.statsagg.utilities;

/**
 * @author Jeffrey Schmidt
 */
public class Json {

    
    public static String convertBoxedPrimativeNumberToString(Object numberObject) {
        
        if (numberObject == null) return null;
        
        String valueString = null;
        
        if (numberObject instanceof Integer) valueString = Integer.toString((Integer) numberObject);
        else if (numberObject instanceof Long) valueString = Long.toString((Long) numberObject);
        else if (numberObject instanceof Short) valueString = Short.toString((Short) numberObject);
        else if (numberObject instanceof Byte) valueString = Byte.toString((Byte) numberObject);
        else if (numberObject instanceof Boolean) valueString = Boolean.toString((Boolean) numberObject);
        else if (numberObject instanceof Double) valueString = Double.toString((Double) numberObject);
        else if (numberObject instanceof Float) valueString = Float.toString((Float) numberObject);
        
        return valueString;
    }
        
}
