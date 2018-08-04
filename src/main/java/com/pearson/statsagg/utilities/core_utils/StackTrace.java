package com.pearson.statsagg.utilities.core_utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class StackTrace {
    
    private static final Logger logger = LoggerFactory.getLogger(StackTrace.class.getName());
    
    public static String getStringFromStackTrace(Exception exception) {
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            
            exception.printStackTrace(printWriter);
            
            return stringWriter.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + " - Failed to convert stack-trace to string");
            return null;
        }
    }
    
    public static String getStringFromStackTrace(StackTraceElement[] stackTraceElements) {
        try {
            
            StringBuilder stackTrace = new StringBuilder();
            
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                stackTrace.append(stackTraceElement).append(System.lineSeparator());
            }
            
            return stackTrace.toString();
        }
        catch (Exception e) {
            logger.error(e.toString() + " - Failed to convert stack-trace to string");
            return null;
        }
    }
    
}
