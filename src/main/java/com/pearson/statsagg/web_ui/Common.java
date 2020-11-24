package com.pearson.statsagg.web_ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.util.Scanner;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant4nov (Prashant Kumar)
 * @author Jeffrey Schmidt
 */
public class Common {
    
    private static final Logger logger = LoggerFactory.getLogger(Common.class.getName());

    public static String getSingleParameterAsString(Object object, String parameterName) {
        
        if ((object == null) || (parameterName == null)) {
            return null;
        }
        
        try {
            if (object instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) object;
                return (String) httpServletRequest.getParameter(parameterName);
            }
            else if (object instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) object;
                JsonElement jsonElement = jsonObject.get(parameterName);
                
                if (jsonElement == null) return null;
                else return (String) jsonElement.getAsString();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return null;
    }
    
    protected static TreeSet<String> getMultilineParameterValues(Object request, String parameterName) {
        
        if ((request == null) || (parameterName == null)) {
            return null;
        }
        
        boolean didEncounterError = false;
        TreeSet<String> parameterValues = new TreeSet<>();

        try {
            if (request instanceof HttpServletRequest) {
                String parameter = Common.getSingleParameterAsString(request, parameterName);
                
                if (parameter != null) {
                    Scanner scanner = new Scanner(parameter);

                    while (scanner.hasNext()) {
                        parameterValues.add(scanner.nextLine().trim());
                    }
                }
            }
            else if (request instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) request;
                JsonArray jsonArray = jsonObject.getAsJsonArray(parameterName);
                if (jsonArray != null) for (JsonElement jsonElement : jsonArray) parameterValues.add(jsonElement.getAsString());
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (didEncounterError) parameterValues = null;
        
        return parameterValues;
    }
    
}
