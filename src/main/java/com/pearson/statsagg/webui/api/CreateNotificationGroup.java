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
        processPostRequest(request, response);
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
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("create notification request");
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        JSONObject notificationGroupData = Helper.getRequestData(request);

        JSONObject responseMsg = new JSONObject();
        response.setContentType("application/json");
        PrintWriter out = null;
   
        try {
            com.pearson.statsagg.webui.CreateNotificationGroup createNotificationGroup = new com.pearson.statsagg.webui.CreateNotificationGroup();
            String result = createNotificationGroup.parseAndAlterNotificationGroup(notificationGroupData);
            responseMsg.put("response", result);
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
    
}