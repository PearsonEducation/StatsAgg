package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.utilities.json_utils.JsonUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
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
@WebServlet(name="API_NotificationGroup_Details", urlPatterns={"/api/notification-group-details"})
public class NotificationGroupDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "API_NotificationGroup_Details";
    
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
            String json = getNotificationGroupDetails(request);
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
     * Returns a json string containing the details of the requested notification group.
     * 
     * @param request servlet request
     * @return details of the requested notification group
     */
    protected String getNotificationGroupDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Integer notificationGroupId = null;
            String notificationGroupName = null;

            if (request.getParameter("id") != null) notificationGroupId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) notificationGroupName = request.getParameter("name");

            if ((notificationGroupId == null) && (notificationGroupName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                notificationGroupId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                notificationGroupName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            }

            NotificationGroup notificationGroup = null;
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            if (notificationGroupId != null) notificationGroup = notificationGroupsDao.getNotificationGroup(notificationGroupId);
            else if (notificationGroupName != null) notificationGroup = notificationGroupsDao.getNotificationGroupByName(notificationGroupName);
            else notificationGroupsDao.close();
            
            if (notificationGroup != null) return NotificationGroup.getJsonString_ApiFriendly(notificationGroup);
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }

}
