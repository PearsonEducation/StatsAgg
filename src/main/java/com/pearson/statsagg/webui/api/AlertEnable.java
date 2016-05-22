package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant4nov (Prashant Kumar)
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "API_Alert_Enable", urlPatterns = {"/api/alert-enable"})
public class AlertEnable extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertEnable.class.getName());
    
    public static final String PAGE_NAME = "API_Alert_Enable";
 
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
     * Returns a string with success message if alert is enabled/disabled 
     * successfully or error message if the request fails to enable/disable alert.
     * 
     * @param request servlet request
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_JSON;
        }
        
        String returnString = Helper.ERROR_JSON;

        try {
            JsonObject jsonObject = Helper.getJsonObjectFromRequetBody(request);
            Integer id = Helper.getIntegerFieldFromJsonObject(jsonObject, "id");
            String name = Helper.getStringFieldFromJsonObject(jsonObject, "name");
            Boolean isEnabled = Helper.getBooleanFieldFromJsonObject(jsonObject, "enabled");
            
            if (id != null) {
                AlertsDao alertsDao = new AlertsDao();
                Alert alert = alertsDao.getAlert(id);
                name = alert.getName();
            }
            
            com.pearson.statsagg.webui.Alerts alerts = new com.pearson.statsagg.webui.Alerts();
            returnString = alerts.changeAlertEnabled(name, isEnabled);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        return returnString;
    }

}
