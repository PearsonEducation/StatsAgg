package com.pearson.statsagg.utilities;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StringUtilities {
    
    private static final Logger logger = LoggerFactory.getLogger(StringUtilities.class.getName());
    
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
        
        StringBuilder mergedRegexStringBuilder = new StringBuilder("");
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

}
