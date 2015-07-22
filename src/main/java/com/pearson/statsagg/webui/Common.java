package com.pearson.statsagg.webui;

import static com.sun.corba.se.spi.presentation.rmi.StubAdapter.request;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * @author prashant4nov (Prashant Kumar)
 */
public class Common {
    
    public static String getObjectParameter(Object object, String parameterName) {
        if (object instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) object;
            return (String) httpServletRequest.getParameter(parameterName);
        }
        else if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            return (String) jsonObject.get(parameterName);
        }
        return null;
    }
}
