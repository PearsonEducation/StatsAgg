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
 * @author prashant kumar(prashant4nov)
 */
@WebServlet(name = "API_CreateNotificationGroup", urlPatterns = {"/api/create-notification-group"})
public class CreateNotificationGroup extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateNotificationGroup.class.getName());
    
    public static final String PAGE_NAME = "API_CreateNotificationGroup";
    
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            String responseMsg = processPostRequest(request, new com.pearson.statsagg.webui.CreateNotificationGroup());
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    /**
     * Returns a string with success message if notification group is 
     * successfully created or error message if the request fails to create one.     
     * 
     * @param request servlet request
     * @param createNotificationGroup CreateNotificationGroup object
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.CreateNotificationGroup createNotificationGroup) {
        logger.debug("create notification request");
        
        String result = null;   
        JSONObject notificationData = Helper.getRequestData(request);
        
        try {
            result = createNotificationGroup.parseAndAlterNotificationGroup(notificationData);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        
        return result;
    }
    
}
