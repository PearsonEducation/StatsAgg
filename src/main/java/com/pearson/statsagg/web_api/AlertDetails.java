package com.pearson.statsagg.web_api;

import com.google.gson.JsonObject;
import com.pearson.statsagg.globals.DatabaseConnections;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroup;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupsDao;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTag;
import com.pearson.statsagg.database_objects.metric_groups.MetricGroupTagsDao;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroup;
import com.pearson.statsagg.database_objects.notification_groups.NotificationGroupsDao;
import com.pearson.statsagg.utilities.json_utils.JsonUtils;
import com.pearson.statsagg.utilities.core_utils.StackTrace;
import com.pearson.statsagg.utilities.db_utils.DatabaseUtils;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prashant kumar (prashant4nov)
 * @author Jeffrey Schmidt
 */
public class AlertDetails extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertDetails.class.getName());
    
    public static final String PAGE_NAME = "API_Alert_Details";
    
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
            String json = getAlertDetails(request);
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
     * Returns a json string containing the details of the requested alert.
     * 
     * @param request servlet request
     * @return details of the requested alert
     */
    protected static String getAlertDetails(HttpServletRequest request) {
        
        if (request == null) {
            return Helper.ERROR_UNKNOWN_JSON;
        }
        
        try {
            Integer alertId = null;
            String alertName = null;

            if (request.getParameter("id") != null) alertId = Integer.parseInt(request.getParameter("id"));
            if (request.getParameter("name") != null) alertName = request.getParameter("name");

            if ((alertId == null) && (alertName == null)) {
                JsonObject jsonObject = Helper.getJsonObjectFromRequestBody(request);
                alertId = JsonUtils.getIntegerFieldFromJsonObject(jsonObject, "id");
                alertName = JsonUtils.getStringFieldFromJsonObject(jsonObject, "name");
            }

            Alert alert = null;
            NotificationGroup cautionNotificationGroup = null;
            NotificationGroup cautionPositiveNotificationGroup = null;
            NotificationGroup dangerNotificationGroup = null;
            NotificationGroup dangerPositiveNotificationGroup = null;
            MetricGroup metricGroup = null;
            
            Connection connection = DatabaseConnections.getConnection();
 
            try {
                if (alertId != null) alert = AlertsDao.getAlert(connection, false, alertId);
                else if (alertName != null) alert = AlertsDao.getAlert(connection, false, alertName);

                if ((alert != null) && (alert.getMetricGroupId() != null)) metricGroup = MetricGroupsDao.getMetricGroup(connection, false, alert.getMetricGroupId());
                if ((alert != null) && (alert.getCautionNotificationGroupId() != null)) cautionNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getCautionNotificationGroupId());
                if ((alert != null) && (alert.getCautionPositiveNotificationGroupId() != null)) cautionPositiveNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getCautionPositiveNotificationGroupId());
                if ((alert != null) && (alert.getDangerNotificationGroupId() != null)) dangerNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getDangerNotificationGroupId());
                if ((alert != null) && (alert.getDangerPositiveNotificationGroupId() != null)) dangerPositiveNotificationGroup = NotificationGroupsDao.getNotificationGroup(connection, false, alert.getDangerPositiveNotificationGroupId());
            }
            catch (Exception e) {
                logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
            }
            finally {
                DatabaseUtils.cleanup(connection);
            }
        
            if (alert != null) return Alert.getJsonString_ApiFriendly(alert, metricGroup, cautionNotificationGroup, cautionPositiveNotificationGroup, dangerNotificationGroup, dangerPositiveNotificationGroup);
            else return Helper.ERROR_NOTFOUND_JSON;
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));        
        }
        
        return Helper.ERROR_UNKNOWN_JSON;
    }
    
}
