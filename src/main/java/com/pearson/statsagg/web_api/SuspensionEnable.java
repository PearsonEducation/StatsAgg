package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.json_utils.JsonUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
public class SuspensionEnable extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionEnable.class.getName());
    
    public static final String PAGE_NAME = "API_Suspension_Enable";
 
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
      
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        
        PrintWriter out = null;
        
        try {  
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        try {    
            String responseMsg = processPostRequest(request);       
            out = response.getWriter();
            out.println(responseMsg);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }  
        finally {            
            if (out != null) {
                out.close();
            }
        } 
        
    }

    /**
     * Returns a string with a success message if suspension is enabled/disabled successfully,
     * or an error message if the request fails to enable/disable the suspension.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
      
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
            Integer id = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
            String name = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            Boolean isEnabled = JsonUtils.getBooleanFieldFromJsonObject(jsonObject, "enabled");
            
            if ((id == null) && (name != null)) {
                Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, name);
                id = suspension.getId();
            }
            
            Suspension suspension = SuspensionsDao.getSuspension(DatabaseConnections.getConnection(), true, id);
            if (suspension == null) return Helper.ERROR_NOTFOUND_JSON;
            
            com.pearson.statsagg.web_ui.Suspensions suspensions = new com.pearson.statsagg.web_ui.Suspensions();
            String result = suspensions.changeSuspensionEnabled(id, isEnabled);
            
            return Helper.createSimpleJsonResponse(result);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
    }

}
