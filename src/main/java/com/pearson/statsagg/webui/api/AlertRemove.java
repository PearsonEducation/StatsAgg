package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant4nov (Prashant Kumar)
 */
@WebServlet(name = "API_Alert_Remove", urlPatterns = {"/api/alert-remove"})
public class AlertRemove extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertRemove.class.getName());
    
    public static final String PAGE_NAME = "API_Alert_Remove";
 
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
            PrintWriter out = null;
            String returnString = processPostRequest(request);       
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(Helper.createSimpleJsonResponse(returnString));
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }   
    }

    /**
     * Returns a string with success message if alert is deleted successfully, 
     * or an error message if the request fails to delete the alert.
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
            
            if (id != null) {
                AlertsDao alertsDao = new AlertsDao();
                Alert alert = alertsDao.getAlert(id);
                name = alert.getName();
            }
            
            com.pearson.statsagg.webui.Alerts alerts = new com.pearson.statsagg.webui.Alerts(); 
            returnString = alerts.removeAlert(name);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        return returnString;
    }
    
}
