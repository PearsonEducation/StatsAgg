package com.pearson.statsagg.web_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Prashant Kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
public class AlertsList extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AlertsList.class.getName());
    
    public static final String PAGE_NAME = "API_Alerts_List";
 
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
            String json = getAlertsList(request);       
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
     * Returns a json object containing a list of alerts.
     * 
     * @param request servlet request
     * @return json string of alerts
     */ 
    protected static String getAlertsList(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        Connection connection = null;
        
        try {
            connection = DatabaseConnections.getConnection();
            
            List<Alert> alerts = AlertsDao.getAlerts(connection, false);
            if (alerts == null) alerts = new ArrayList<>();
            
            List<JsonObject> alertsJsonObjects = new ArrayList<>();
            for (Alert alert : alerts) {
                if (alert == null) continue;

                MetricGroup metricGroup = null;
                if (alert.getMetricGroupId() != null) metricGroup = MetricGroupsDao.getMetricGroup(connection, false, alert.getMetricGroupId());
                
                NotificationGroup cautionNotificationGroup = null;
                NotificationGroup cautionPositiveNotificationGroup = null;
                NotificationGroup dangerNotificationGroup = null;
                NotificationGroup dangerPositiveNotificationGroup = null;
                if ((alert.getCautionNotificationGroupId() != null)) cautionNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getCautionNotificationGroupId());
                if ((alert.getCautionPositiveNotificationGroupId() != null)) cautionPositiveNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getCautionPositiveNotificationGroupId());
                if ((alert.getDangerNotificationGroupId() != null)) dangerNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getDangerNotificationGroupId());
                if ((alert.getDangerPositiveNotificationGroupId() != null)) dangerPositiveNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getDangerPositiveNotificationGroupId());
                          
                JsonObject alertJsonObject = Alert.getJsonObject_ApiFriendly(alert, metricGroup, cautionNotificationGroup, cautionPositiveNotificationGroup, dangerNotificationGroup, dangerPositiveNotificationGroup);
                if (alertJsonObject != null) alertsJsonObjects.add(alertJsonObject);
            }
            
            DatabaseUtils.cleanup(connection);
                
            Gson alertsGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonElement alerts_JsonElement = alertsGson.toJsonTree(alertsJsonObjects);
            JsonArray jsonArray = new Gson().toJsonTree(alerts_JsonElement).getAsJsonArray();
            String alertsJson = alertsGson.toJson(jsonArray);
            
            return alertsJson;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            return Helper.ERROR_UNKNOWN_JSON;
        }
        finally {  
            DatabaseUtils.cleanup(connection);
        }
        
    }

}
