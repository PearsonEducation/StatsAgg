package com.pearson.statsagg.webui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
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
 */
@WebServlet(name="API_NotificationGroupDetails", urlPatterns={"/api/notification-group-details"})
public class NotificationGroupDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupDetails.class.getName());
    
    public static final String PAGE_NAME = "API_NotificationGroupDetails";
    
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
        try {
            String json = getNotificationGroupDetails(request);
            response.setContentType("application/json");
            PrintWriter out;
            out = response.getWriter();
            out.println(json);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
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
            return Helper.ERROR_JSON;
        }
        
        try {
            Integer notificationGroupId = null;
            String notificationGroupName = null;

            if (request.getParameter("id") != null) notificationGroupId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) notificationGroupName = request.getParameter("name");

            if ((notificationGroupId == null) && (notificationGroupName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequetBody(request);
                notificationGroupId = Helper.getIntegerFieldFromJsonObject(jsonObject, "id");
                notificationGroupName = Helper.getStringFieldFromJsonObject(jsonObject, "name");
            }

            NotificationGroup notificationGroup = null;
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            if (notificationGroupId != null) notificationGroup = notificationGroupsDao.getNotificationGroup(notificationGroupId);
            else if (notificationGroupName != null) notificationGroup = notificationGroupsDao.getNotificationGroupByName(notificationGroupName);
            else notificationGroupsDao.close();
            
            if (notificationGroup != null) return getApiFriendlyJsonObject(notificationGroup);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_JSON;
    }
    
    private String getApiFriendlyJsonObject(NotificationGroup notificationGroup) {
        
        if (notificationGroup == null) {
            return null;
        }
        
        Gson notificationGroup_Gson = new GsonBuilder().setFieldNamingStrategy(new JsonOutputFieldNamingStrategy()).setPrettyPrinting().create();   
        JsonElement notificationGroup_JsonElement = notificationGroup_Gson.toJsonTree(notificationGroup);
        JsonObject jsonObject = new Gson().toJsonTree(notificationGroup_JsonElement).getAsJsonObject();
        
        return notificationGroup_Gson.toJson(jsonObject);
    }
    
}
