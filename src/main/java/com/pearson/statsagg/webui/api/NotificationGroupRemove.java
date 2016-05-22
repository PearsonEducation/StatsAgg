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
 * @author prashant kumar (prashant4nov)
 */
@WebServlet(name = "API_Remove_Notification", urlPatterns = {"/api/notification-remove"})
public class NotificationGroupRemove extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupRemove.class.getName());
    
    public static final String PAGE_NAME = "API_Remove_Notification";
    
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
        try {    
            PrintWriter out = null;
            String returnString = processPostRequest(request, new com.pearson.statsagg.webui.NotificationGroups());       
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
     * Returns a string with success message if notification group is deleted 
     * successfully or error message if the request fails to delete notification group.
     * 
     * @param request servlet request
     * @param notificationGroup NotificationGroups object
     * @return success or error message
     */
    protected String processPostRequest(HttpServletRequest request, com.pearson.statsagg.webui.NotificationGroups notificationGroup) {
        logger.debug("Remove notificationGroup request");
        
        String returnString = null;
        
        try {
            String notificationName = null;
            
            logger.debug(request.getParameter(Helper.name));
            
            if (request.getParameter(Helper.name) != null) {
                notificationName = request.getParameter(Helper.name);
            }
            
            returnString = notificationGroup.removeNotificationGroup(notificationName);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }

        return returnString;
    }
    
}
