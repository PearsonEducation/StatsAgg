package com.pearson.statsagg.webui.api;

import com.pearson.statsagg.database_objects.alert_suspensions.AlertSuspensionsDao;
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
 * @author prashant kumar (Prashant4nov)
 */
@WebServlet(name="API_AlertSuspensionList", urlPatterns={"/api/AlertsSuspension-list"})
public class AlertSuspensionList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertSuspensionList.class.getName());
    
    public static final String PAGE_NAME = "API_AlertSuspensionList";
    
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
        logger.debug("doGet");
        
        try {    
            JSONObject json = getAlertSuspensionList(request, new AlertSuspensionsDao());       
            
            PrintWriter out = null;
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }     
        
    }
    
    /**
     * Returns a json object containing a list of alert suspensions.
     * 
     * @param request servlet request
     * @param alertSuspensionsDao AlertSuspensionsDao object
     * @return list of the alert suspensions
     */
    protected JSONObject getAlertSuspensionList(HttpServletRequest request, AlertSuspensionsDao alertSuspensionsDao) {
        logger.debug("getAlertSuspensionList");
        
        JSONObject errorMsg = null;
        JSONObject alertSuspensionJson = null;
        int pageNumber = 0, pageSize = 0;
        
        try {
            if (request.getParameter(Helper.pageNumber) != null) {
                pageNumber = Integer.parseInt(request.getParameter(Helper.pageNumber));
            }

            if (request.getParameter(Helper.pageSize) != null) {
                pageSize = Integer.parseInt(request.getParameter(Helper.pageSize));
            }
            
            alertSuspensionJson = alertSuspensionsDao.getSuspension(pageNumber*pageSize, pageSize);
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            errorMsg = new JSONObject();
            errorMsg.put(Helper.error, Helper.errorMsg);
        }
        
        if (alertSuspensionJson != null) return alertSuspensionJson;
        else if (errorMsg != null) return errorMsg;
        else return null;
    }
    
}
