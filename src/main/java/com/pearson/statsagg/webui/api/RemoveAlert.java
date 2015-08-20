package com.pearson.statsagg.webui.api;

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
@WebServlet(name = "API_Remove_Alert", urlPatterns = {"/api/alert-remove"})
public class RemoveAlert extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoveAlert.class.getName());
    
    public static final String PAGE_NAME = "API_Remove_Alert";
 
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("doPost");
        
        try {    
            PrintWriter out = null;
            String returnString = processPostRequest(request, new com.pearson.statsagg.webui.Alerts());       
            JSONObject responseMsg = new JSONObject();
            responseMsg.put("response", returnString);
            response.setContentType("application/json");
            out = response.getWriter();
            out.println(responseMsg);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }  
    }

    /**
     * Returns a string with success message if alert is deleted 
     * successfully or error message if the request fails to delete alert.
     * 
     * @param request servlet request
     * @param alert Alerts object
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.Alerts alert) {
        logger.debug("Remove alert request");
        
        String returnString = null;
        String alertName = null;
        
        try {
            if (request.getParameter(Helper.name) != null) {
                alertName = request.getParameter(Helper.name);
            }
            
            returnString = alert.removeAlert(alertName);
        } 
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return returnString;
    }
    
}
