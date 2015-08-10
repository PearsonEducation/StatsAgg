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
public class RemoveNotificationGroup extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(RemoveNotificationGroup.class.getName());
    
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
        processPostRequest(request, response);
  
    }

    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Remove notificationGroup request");
        
        try {
            String returnString = null;
            if ((request == null) || (response == null)) {
                return;
            }
            String metricName = null;
            logger.info(request.getParameter(Helper.name).toString());
            if (request.getParameter(Helper.name) != null) {
                metricName = request.getParameter(Helper.name);
            }
            com.pearson.statsagg.webui.NotificationGroups notificationGroup = new com.pearson.statsagg.webui.NotificationGroups();
            returnString = notificationGroup.removeNotificationGroup(metricName);
            JSONObject responseMsg = new JSONObject();
            responseMsg.put("response", returnString);
            response.setContentType("application/json");
            PrintWriter out = null;
            out = response.getWriter();
            out.println(responseMsg);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
    }
    
}
