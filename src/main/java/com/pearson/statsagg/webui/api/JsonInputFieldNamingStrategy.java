package com.pearson.statsagg.webui.api;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class JsonInputFieldNamingStrategy implements FieldNamingStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonInputFieldNamingStrategy.class.getName());

    /*

    */
    @Override
    public String translateName(Field field) {
        if (field == null) {
            return null;
        }
        
        String name = field.getName();
        if (name == null) return null;

        String camelCase_UppercaseFirstLetter = upperCaseFirstLetter(name);
        
        return camelCase_UppercaseFirstLetter;
    }

    // credit to the google gson library
    protected static String upperCaseFirstLetter(String name) {
        
        if (name == null) return null;
        
        StringBuilder fieldNameBuilder = new StringBuilder();
        int index = 0;
        char firstCharacter = name.charAt(index);

        while (index < name.length() - 1) {
            if (Character.isLetter(firstCharacter)) {
                break;
            }

            fieldNameBuilder.append(firstCharacter);
            firstCharacter = name.charAt(++index);
        }

        if (index == name.length()) {
            return fieldNameBuilder.toString();
        }

        if (!Character.isUpperCase(firstCharacter)) {
            String modifiedTarget = modifyString(Character.toUpperCase(firstCharacter), name, ++index);
            return fieldNameBuilder.append(modifiedTarget).toString();
        }
        else {
            return name;
        }
    }
    
    // credit to the google gson library
    private static String modifyString(char firstCharacter, String srcString, int indexOfSubstring) {
        return (indexOfSubstring < srcString.length()) ? firstCharacter + srcString.substring(indexOfSubstring) : String.valueOf(firstCharacter);
    }
    
}
