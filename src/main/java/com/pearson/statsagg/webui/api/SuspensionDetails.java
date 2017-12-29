package com.pearson.statsagg.webui.api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.suspensions.Suspension;
import com.pearson.statsagg.database_objects.suspensions.SuspensionsDao;
import com.pearson.statsagg.utilities.JsonUtils;
import com.pearson.statsagg.utilities.StackTrace;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
@WebServlet(name="API_Suspension_Details", urlPatterns={"/api/suspension-details"})
public class SuspensionDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(SuspensionDetails.class.getName());
    
    public static final String PAGE_NAME = "API_Suspension_Details";
    
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
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        
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
            String json = getSuspensionDetails(request);
            out = response.getWriter();
            out.println(json);
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
     * Returns a json string containing the details of the requested suspension.
     * 
     * @param request servlet request
     * @return details of the requested suspension
     */
    private String getSuspensionDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Integer suspensionId = null;
            String suspensionName = null;

            if (request.getParameter("id") != null) suspensionId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) suspensionName = request.getParameter("name");

            if ((suspensionId == null) && (suspensionName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                suspensionId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                suspensionName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            }

            Suspension suspension = null;
            SuspensionsDao suspensionsDao = new SuspensionsDao(false);
            if (suspensionId != null) suspension = suspensionsDao.getSuspension(suspensionId);
            else if (suspensionName != null) suspension = suspensionsDao.getSuspensionByName(suspensionName);
            
            Alert alert = null;
            if ((suspension != null) && (suspension.getAlertId() != null)) {
                AlertsDao alertsDao = new AlertsDao(suspensionsDao.getDatabaseInterface());
                alert = alertsDao.getAlert(suspension.getAlertId());
            }
            
            suspensionsDao.close();
            
            if (suspension != null) return Suspension.getJsonString_ApiFriendly(suspension, alert);
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }

}
