package com.pearson.statsagg.webui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
            Logger.getLogger(com.pearson.statsagg.webui.Common.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(com.pearson.statsagg.webui.Common.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return data;
    }
}
