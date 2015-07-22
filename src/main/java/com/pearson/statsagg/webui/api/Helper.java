package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.utilities.StackTrace;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.LoggerFactory;
/**
 * @author Jeffrey Schmidt
 */
public class Helper {
    
    public final static String pageNumber = "page_number";
    public final static String pageSize = "page_size";
    public final static String error = "error";
    public final static String errorMsg = "Invalid request parameters.";
    public final static String noResult = "No results found!";
    public final static String id = "id";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Helper.class.getName());
    static String name = "name";

    
    public static JSONObject getRequestData(HttpServletRequest request) {

        BufferedReader reader = null;
        JSONObject data = null;
        try {
            String line = null;
            StringBuilder requestData = new StringBuilder();
            reader = request.getReader();
            while ((line = reader.readLine()) != null)
                requestData.append(line);
            data = (JSONObject) JSONValue.parse(requestData.toString());
        } catch (IOException ex) {
              logger.error(ex.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(ex));
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.error(ex.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(ex));
            }
        }
        return data;
    }
}
