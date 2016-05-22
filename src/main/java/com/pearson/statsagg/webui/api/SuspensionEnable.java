package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "API_Suspension_Enable", urlPatterns = {"/api/suspension-enable"})
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
        try {    
            String responseMsg = processPostRequest(request);       
            PrintWriter out = null;
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(responseMsg);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }  
    }

    /**
     * Returns a string with success message if suspension is enabled/disabled 
     * successfully or error message if the request fails to enable/disable suspension.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
        String returnString = null;

        try {
            JsonObject jsonObject = Helper.getJsonObjectFromRequetBody(request);
            Integer id = Helper.getIntegerFieldFromJsonObject(jsonObject, "id");
            String name = Helper.getStringFieldFromJsonObject(jsonObject, "name");
            Boolean isEnabled = Helper.getBooleanFieldFromJsonObject(jsonObject, "enabled");
            
            if (id != null) {
                SuspensionsDao suspensionsDao = new SuspensionsDao();
                Suspension suspension = suspensionsDao.getSuspension(id);
                name = suspension.getName();
            }
            
            com.pearson.statsagg.webui.Suspensions suspensions = new com.pearson.statsagg.webui.Suspensions();
            returnString = suspensions.changeSuspensionEnabled(name, isEnabled);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        return returnString;
    }

}
