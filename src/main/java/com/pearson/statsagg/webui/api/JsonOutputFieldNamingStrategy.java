package com.pearson.statsagg.webui.api;

import com.google.gson.FieldNamingStrategy;
import java.lang.reflect.Field;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class JsonOutputFieldNamingStrategy implements FieldNamingStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonOutputFieldNamingStrategy.class.getName());

    /*
    Removes underscores at the end of field names.
    Also removes leading 'is_' or 'is' prefixes on field names, which are commonly found on boolean fields
    
    This largely exists to correct field naming for json output in the event that the fields weren't annocated with field names in the source class
    */
    @Override
    public String translateName(Field field) {
        if (field == null) {
            return null;
        }
        
        String name = field.getName();
        if (name == null) return null;

        String lowerCaseFieldName_SeparatedByUnderscores = separateCamelCase(name, "_").toLowerCase(Locale.ENGLISH);
        String lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore = lowerCaseFieldName_SeparatedByUnderscores;
        
        while (lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore.endsWith("_")) {
            lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore = lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore.substring(0, lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore.length() - 1);
        }
        
        if (lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore.startsWith("is_")) {
            lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore = lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore.substring(3);
        }
        
        return lowerCaseFieldName_SeparatedByUnderscores_NoTrailingUnderscore;
    }
    
    // credit to the google gson library
    protected static String separateCamelCase(String name, String separator) {
        
        if ((name == null) || (separator == null)) {
            return null;
        }
        
        StringBuilder translation = new StringBuilder();
        
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if (Character.isUpperCase(character) && translation.length() != 0) {
                translation.append(separator);
            }
            translation.append(character);
        }
        
        return translation.toString();
    }

}
