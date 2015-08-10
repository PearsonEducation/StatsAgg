package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class Helper {
    
    private static final Logger logger = LoggerFactory.getLogger(Helper.class.getName());

    protected final static String pageNumber = "page_number";
    protected final static String pageSize = "page_size";
    protected final static String error = "error";
    protected final static String errorMsg = "Invalid request parameters.";
    protected final static String noResult = "No results found!";
    protected final static String id = "id";
    protected final static String name = "name";
    
    public static JSONObject getRequestData(HttpServletRequest request) {
        BufferedReader bufferedReader = null;
        JSONObject data = null;
        
        try {
            String line = null;
            StringBuilder requestData = new StringBuilder();
            bufferedReader = request.getReader();
            while ((line = bufferedReader.readLine()) != null) requestData.append(line);
            data = (JSONObject) JSONValue.parse(requestData.toString());
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        } 
        finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } 
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
        }
        
        return data;
    }
    
}
