package com.pearson.statsagg.webui;

import com.google.gson.JsonObject;
import com.pearson.statsagg.utilities.StackTrace;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant4nov (Prashant Kumar)
 * @author Jeffrey Schmidt
 */
public class Common {
    
    private static final Logger logger = LoggerFactory.getLogger(Common.class.getName());

    public static String getObjectParameter(Object object, String parameterName) {
        
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
                return (String) jsonObject.get(parameterName).getAsString();
            }
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return null;
    }
    
}
