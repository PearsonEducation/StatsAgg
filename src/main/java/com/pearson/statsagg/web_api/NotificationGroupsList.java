package com.pearson.statsagg.web_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
@WebServlet(name="API_NotificationGroups_List", urlPatterns={"/api/notification-groups-list"})
public class NotificationGroupsList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGroupsList.class.getName());
    
    public static final String PAGE_NAME = "API_NotificationGroups_List";
 
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
            String json = getNotificationGroupsList(request);       
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
     * Returns json containing a list of notification groups.
     * 
     * @param request servlet request
     * @return list of notification groups
     */ 
    protected String getNotificationGroupsList(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            List<NotificationGroup> notificationGroups = notificationGroupsDao.getAllDatabaseObjectsInTable();
            if (notificationGroups == null) notificationGroups = new ArrayList<>();
            
            List<JsonObject> notificationGroupsJsonObjects = new ArrayList<>();
            for (NotificationGroup notificationGroup : notificationGroups) {
                JsonObject notificationGroupJsonObject = NotificationGroup.getJsonObject_ApiFriendly(notificationGroup);
                if (notificationGroupJsonObject != null) notificationGroupsJsonObjects.add(notificationGroupJsonObject);
            }
            
            Gson notificationGroupsGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonElement notificationGroups_JsonElement = notificationGroupsGson.toJsonTree(notificationGroupsJsonObjects);
            JsonArray jsonArray = new Gson().toJsonTree(notificationGroups_JsonElement).getAsJsonArray();
            String notificationGroupsJson = notificationGroupsGson.toJson(jsonArray);
            
            return notificationGroupsJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
    }
    
}
