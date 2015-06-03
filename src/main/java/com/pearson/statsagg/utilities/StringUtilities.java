package com.pearson.statsagg.utilities;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StringUtilities {
    
    private static final Logger logger = LoggerFactory.getLogger(StringUtilities.class.getName());
    
    private static final Map<String,Charset> charsetCache = new ConcurrentHashMap<>();

    public static Charset getCharsetFromString(String charset) {
        
        if (charset == null) {
            return null;
        } 
        
        Charset charsetToUse;
        
        try {
            charsetToUse = charsetCache.get(charset);
            
            if (charsetToUse == null) {
                charsetToUse = Charset.availableCharsets().get(charset);
                
                if (charsetToUse == null) {
                    logger.warn("Couldn't find Charset \"" + removeNewlinesFromString(charset) + "\". Using default charset.");
                    charsetToUse = Charset.defaultCharset();
                }
                else charsetCache.put(charset, charsetToUse);
            }
        }
        catch (Exception e) {
            logger.error("Error using Charset \"" + removeNewlinesFromString(charset) + "\". Using default charset." + 
                    e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            
            charsetToUse = Charset.defaultCharset();
            if (charsetToUse != null) charsetCache.put(charset, charsetToUse);
        }
        
        return charsetToUse;
    }
    
    public static String removeDecimalPointsFromNumber(String number) {
        
        if (number == null) {
            return null;
        }
        
        try {
            int decimalPointIndex = number.indexOf('.');
            
            if (decimalPointIndex == -1) {
                return number;
            }
            else {
                String numberNoDecimalPoints = number.substring(0, decimalPointIndex);
                return numberNoDecimalPoints;
            }
        }
        catch (Exception e) {
            return null;
        }

    }
    
    public static String createMergedRegex(List<String> regexes) {

        if (regexes == null) {
            return null;
        }
        
        StringBuilder mergedRegexStringBuilder = new StringBuilder();
        int regexCount = regexes.size();

        mergedRegexStringBuilder.append("(");

        for (int i = 0; i < regexCount; i++) {
            if ((i + 1) < regexCount) {
                mergedRegexStringBuilder.append(regexes.get(i)).append("|");
            }
            else {
                mergedRegexStringBuilder.append(regexes.get(i));
            }
        }

        mergedRegexStringBuilder.append(")");

        return mergedRegexStringBuilder.toString();
    }

    public static String removeNewlinesFromString(String inputString) {

        if ((inputString == null) || inputString.isEmpty()) {
            return inputString;
        }
        
        String cleanedString = StringUtils.remove(inputString, '\r');
        cleanedString = StringUtils.remove(cleanedString, '\n');

        return cleanedString;
    }
    
    public static String removeNewlinesFromString(String inputString, char newlineReplacementCharacter) {

        if ((inputString == null) || inputString.isEmpty()) {
            return inputString;
        }
        
        String cleanedString = inputString.replace('\n', newlineReplacementCharacter).replace('\r', newlineReplacementCharacter);
        return cleanedString;
    }
    
}
